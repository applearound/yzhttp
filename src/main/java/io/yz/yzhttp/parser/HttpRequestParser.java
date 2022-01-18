package io.yz.yzhttp.parser;


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
public class HttpRequestParser {
    private Phase main;

    public HttpRequestParser() {
        this.main = Phase.P_STATUS_LINE;
    }

    public void feed() {

    }

    private enum Phase {
        P_STATUS_LINE,
        P_HEADER_FILED,
        P_MESSAGE_BODY,
    }
}
