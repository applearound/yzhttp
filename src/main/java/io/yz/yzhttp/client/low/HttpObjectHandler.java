package io.yz.yzhttp.client.low;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.yz.yzhttp.client.HttpHeader;
import io.yz.yzhttp.client.ProtocolParseException;
import io.yz.yzhttp.constant.HttpVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * token = 1*tchar
 * obs-fold = CRLF 1*( SP / HTAB ); obsolete line folding
 * <p>
 * HTTP-message = start-line *( header-field CRLF ) CRLF [ message-body ]
 * start-line = request-line / status-line
 * status-line = HTTP-version SP status-code SP reason-phrase CRLF
 * HTTP-name = %x48.54.54.50; "HTTP", case-sensitive
 * HTTP-version = HTTP-name "/" DIGIT "." DIGIT
 * status-code = 3DIGIT
 * reason-phrase = *( HTAB / SP / VCHAR / obs-text )
 * <p>
 * header-field = field-name ":" OWS field-value OWS
 * field-name = token
 * field-value = *( field-content / obs-fold )
 * field-content = field-vchar [ 1*( SP / HTAB ) field-vchar ]
 * field-vchar = VCHAR / obs-text
 * <p>
 * header-field = 1*tchar ":" OWS *( field-content / obs-fold ) OWS
 * ( VCHAR / obs-text ) [ 1*( SP / HTAB ) ( VCHAR / obs-text ) ]
 */
public class HttpObjectHandler extends ChannelInboundHandlerAdapter {
    private static final PooledByteBufAllocator allocator = new PooledByteBufAllocator();

    private static final byte CR = 0x0D;
    private static final byte LF = 0x0A;

    private static final byte SP = 0x20;
    private static final byte HTAB = 0x09;

    private static final byte A = 0x41;
    private static final byte Z = 0x5A;

    private static final byte a = 0x61;
    private static final byte z = 0x7A;

    private static final byte _0 = 0x30;
    private static final byte _9 = 0x39;

    private static final byte H = 0x48;
    private static final byte T = 0x54;
    private static final byte P = 0x50;

    private static final byte DOT = 0x2E;
    private static final byte SLASH = 0x2F;
    private static final byte COLON = 0x3A;

    /**
     * ALPHA = %x41-5A / %x61-7A; A-Z / a-z
     *
     * @param b byte
     * @return true or false
     */
    private static boolean isAlpha(final byte b) {
        return (A <= b && b <= Z) || (a <= b && b <= z);
    }

    /**
     * DIGIT = %x30-39; 0-9
     *
     * @param b byte
     * @return true or false
     */
    private static boolean isDigit(final byte b) {
        return _0 <= b && b <= _9;
    }

    /**
     * VCHAR = %x21-7E; visible (printing) characters
     *
     * @param b byte
     * @return true or false
     */
    private static boolean isVChar(final byte b) {
        return 0x21 <= b && b <= 0x7E;
    }

    /**
     * obs-text = %x80-FF
     *
     * @param b byte
     * @return true or false
     */
    private static boolean isObsText(final byte b) {
        return (b & 0xFF) >= 0x80;
    }

    /**
     * tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA; any VCHAR, except delimiters
     *
     * @param b byte
     * @return true or false
     */
    private static boolean isTChar(final byte b) {
        return b == '!' || ('#' <= b && b <= '\'') || b == '*' || b == '+' || b == '-' || b == '.'
                || isDigit(b) || ('A' <= b && b <= 'Z') || ('^' <= b && b <= 'z') || b == '|' || b == '~';
    }

    /**
     * reason-phrase = *( HTAB / SP / VCHAR / obs-text )
     *
     * @param b byte
     * @return true or false
     */
    private static boolean isReasonPhraseByte(final byte b) {
        return b == HTAB || b == SP || isVChar(b) || isObsText(b);
    }

    private static boolean isFieldVarChar(final byte b) {
        return isVChar(b) || isObsText(b);
    }

    /*
     HTTP/1.1 200 OK
     Date: Mon, 27 Jul 2009 12:28:53 GMT
     Server: Apache
     Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT
     ETag: "34aa387-d-1568eb00"
     Accept-Ranges: bytes
     Content-Length: 51
     Vary: Accept-Encoding
     Content-Type: text/plain

     Hello World! My payload includes a trailing CRLF.
    */
    private enum State {
        INIT,

        HTTP_VERSION_H,
        HTTP_VERSION_T_FIRST,
        HTTP_VERSION_T_SECOND,
        HTTP_VERSION_P,
        HTTP_VERSION_SLASH,
        HTTP_VERSION_DIGIT_FIRST,
        HTTP_VERSION_DOT,
        HTTP_VERSION_DIGIT_SECOND,

        STATUS_CODE_START_SP,
        STATUS_CODE_DIGIT_FIRST,
        STATUS_CODE_DIGIT_SECOND,
        STATUS_CODE_DIGIT_THIRD,

        REASON_PHRASE_START_SP,

        HEADER_FIELD_START_CR,
        HEADER_FIELD_START_LF,

        FIELD_NAME,
        FILED_NAME_COLON,

        HEADER_FIELD_MID_CR,
        HEADER_FIELD_MID_LF,

        MESSAGE_BODY_START_CR,
        MESSAGE_BODY_START_LF,
    }

    private final ByteBuf cache;
    private final long maxSize;

    private State state;

    private HttpVersion version;
    private StringBuilder httpVersionStringBuilder = new StringBuilder();
    private StringBuilder statueCodeStringBuilder = new StringBuilder();
    private StringBuilder reasonPharseBuilder = new StringBuilder();
    private StringBuilder lastKeyBuilder;
    private String lastKey;
    private StringBuilder lastValueBuilder;
    private HttpHeader.Builder httpHeaderBulder = HttpHeader.builder();

    public HttpObjectHandler() {
        this.maxSize = Integer.MAX_VALUE;

        this.cache = allocator.buffer();
        state = State.INIT;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        final ByteBuf msgByteBuf = (ByteBuf) msg;

        while (msgByteBuf.isReadable()) {
            final byte b = msgByteBuf.readByte();
            switch (state) {
                case INIT:
                    if (b == H) {
                        httpVersionStringBuilder.append('H');
                        state = State.HTTP_VERSION_H;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_H:
                    if (b == T) {
                        httpVersionStringBuilder.append('T');
                        state = State.HTTP_VERSION_T_FIRST;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_T_FIRST:
                    if (b == T) {
                        httpVersionStringBuilder.append('T');
                        state = State.HTTP_VERSION_T_SECOND;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_T_SECOND:
                    if (b == P) {
                        httpVersionStringBuilder.append('P');
                        state = State.HTTP_VERSION_P;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_P:
                    if (b == SLASH) {
                        httpVersionStringBuilder.append('/');
                        state = State.HTTP_VERSION_SLASH;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_SLASH:
                    if (isDigit(b)) {
                        httpVersionStringBuilder.append(b - _0);
                        state = State.HTTP_VERSION_DIGIT_FIRST;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_DIGIT_FIRST:
                    if (b == DOT) {
                        httpVersionStringBuilder.append('.');
                        state = State.HTTP_VERSION_DOT;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_DOT:
                    if (isDigit(b)) {
                        httpVersionStringBuilder.append(b - _0);
                        state = State.HTTP_VERSION_DIGIT_SECOND;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HTTP_VERSION_DIGIT_SECOND:
                    if (b == SP) {
                        state = State.STATUS_CODE_START_SP;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case STATUS_CODE_START_SP:
                    if (isDigit(b)) {
                        statueCodeStringBuilder.append(b - _0);
                        state = State.STATUS_CODE_DIGIT_FIRST;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case STATUS_CODE_DIGIT_FIRST:
                    if (isDigit(b)) {
                        statueCodeStringBuilder.append(b - _0);
                        state = State.STATUS_CODE_DIGIT_SECOND;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case STATUS_CODE_DIGIT_SECOND:
                    if (isDigit(b)) {
                        statueCodeStringBuilder.append(b - _0);
                        state = State.STATUS_CODE_DIGIT_THIRD;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case STATUS_CODE_DIGIT_THIRD:
                    if (b == SP) {
                        state = State.REASON_PHRASE_START_SP;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case REASON_PHRASE_START_SP:
                    if (isReasonPhraseByte(b)) {
                        reasonPharseBuilder.append((char) b);
                        state = State.REASON_PHRASE_START_SP;
                    } else if (b == CR) {
                        state = State.HEADER_FIELD_START_CR;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HEADER_FIELD_START_CR:
                    if (b == LF) {
                        state = State.HEADER_FIELD_START_LF;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HEADER_FIELD_START_LF:
                    if (isTChar(b)) {
                        lastKeyBuilder = new StringBuilder();
                        lastKeyBuilder.append((char) b);
                        state = State.FIELD_NAME;
                    } else if (b == CR) {
                        state = State.MESSAGE_BODY_START_CR;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case FIELD_NAME:
                    if (isTChar(b)) {
                        lastKeyBuilder.append((char) b);
                        state = State.FIELD_NAME;
                    } else if (b == COLON) {
                        lastKey = lastKeyBuilder.toString();
                        lastValueBuilder = new StringBuilder();
                        state = State.FILED_NAME_COLON;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case FILED_NAME_COLON:
                    if (b == SP || b == HTAB || isFieldVarChar(b)) {
                        lastValueBuilder.append((char) b);
                        state = State.FILED_NAME_COLON;
                    } else if (b == CR) {
                        lastValueBuilder.append((char) b);
                        state = State.HEADER_FIELD_MID_CR;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HEADER_FIELD_MID_CR:
                    if (b == LF) {
                        lastValueBuilder.append((char) b);
                        state = State.HEADER_FIELD_MID_LF;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case HEADER_FIELD_MID_LF:
                    if (b == SP || b == HTAB) {
                        lastValueBuilder.append((char) b);
                        state = State.FILED_NAME_COLON;
                    } else if (isTChar(b)) {
                        int i;
                        for (i = 0; i < lastValueBuilder.length(); i++) {
                            final char c = lastValueBuilder.charAt(i);
                            if (c != SP && c != HTAB) {
                                break;
                            }
                        }
                        lastValueBuilder.delete(0, i);
                        for (i = lastValueBuilder.length() - 1; i >= 0; i--) {
                            final char c = lastValueBuilder.charAt(i);
                            if (c != SP && c != HTAB && c != CR && c != LF) {
                                break;
                            }
                        }
                        lastValueBuilder.delete(i + 1, lastValueBuilder.length());
                        httpHeaderBulder.addField(lastKey, lastValueBuilder.toString());
                        lastKeyBuilder = new StringBuilder();
                        lastKeyBuilder.append((char) b);
                        state = State.FIELD_NAME;
                    } else if (b == CR) {
                        int i;
                        for (i = 0; i < lastValueBuilder.length(); i++) {
                            final char c = lastValueBuilder.charAt(i);
                            if (c != SP && c != HTAB) {
                                break;
                            }
                        }
                        lastValueBuilder.delete(0, i);
                        for (i = lastValueBuilder.length() - 1; i >= 0; i--) {
                            final char c = lastValueBuilder.charAt(i);
                            if (c != SP && c != HTAB && c != CR && c != LF) {
                                break;
                            }
                        }
                        lastValueBuilder.delete(i + 1, lastValueBuilder.length());
                        httpHeaderBulder.addField(lastKey, lastValueBuilder.toString());
                        state = State.MESSAGE_BODY_START_CR;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case MESSAGE_BODY_START_CR:
                    if (b == LF) {
                        state = State.MESSAGE_BODY_START_LF;
                    } else {
                        throw new ProtocolParseException();
                    }
                    break;
                case MESSAGE_BODY_START_LF:
                    break;
            }
        }

        System.out.println(state);
        System.out.println(httpVersionStringBuilder);
        System.out.println(statueCodeStringBuilder);
        System.out.println(reasonPharseBuilder);
        httpHeaderBulder.build().forEach(System.out::println);

        msgByteBuf.release();
    }
}
