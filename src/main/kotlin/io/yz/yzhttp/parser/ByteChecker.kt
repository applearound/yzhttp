package io.yz.yzhttp.parser

private const val TAB: Byte = 0x09
private const val TAB_UNSIGNED: UByte = 0x09u

private const val SP: Byte = 0x20
private const val SP_UNSIGNED: UByte = 0x20u

private const val EXCLAMATION_MARK: Byte = 0x21
private const val EXCLAMATION_MARK_UNSIGNED: UByte = 0x21u

private const val HASHTAG: Byte = 0x23
private const val HASHTAG_UNSIGNED: UByte = 0x23u

private const val APOSTROPHE: Byte = 0x27
private const val APOSTROPHE_UNSIGNED: UByte = 0x27u

private const val ASTERISK: Byte = 0x2A
private const val ASTERISK_UNSIGNED: UByte = 0x2Au

private const val PLUS: Byte = 0x2B
private const val PLUS_UNSIGNED: UByte = 0x2Bu

private const val MINUS: Byte = 0x2D
private const val MINUS_UNSIGNED: UByte = 0x2Du

private const val DOT: Byte = 0x2E
private const val DOT_SIGNED: UByte = 0x2Eu

private const val N0: Byte = 0x30
private const val N9: Byte = 0x39
private const val N0_UNSIGNED: UByte = 0x30u
private const val N9_UNSIGNED: UByte = 0x39u

private const val A: Byte = 0x41
private const val Z: Byte = 0x5A
private const val A_UNSIGNED: UByte = 0x41u
private const val Z_UNSIGNED: UByte = 0x5Au

private const val CARET: Byte = 0x5E
private const val CARET_UNSIGNED: UByte = 0x5Eu

private const val a: Byte = 0x61
private const val z: Byte = 0x7A
private const val a_unsigned: UByte = 0x61u
private const val z_unsigned: UByte = 0x7Au

private const val VERTICAL_BAR: Byte = 0x7C
private const val VERTICAL_BAR_UNSIGNED: UByte = 0x7Cu
private const val TILDE: Byte = 0x7E
private const val TILDE_UNSIGNED: UByte = 0x7Eu

/**
 * ALPHA = 0x41-0x5A / 0x61-0x7A; A-Z / a-z
 *
 * @return true or false
 */
fun Byte.isAlpha(): Boolean {
    return when (this) {
        in A..Z -> true
        in a..z -> true
        else -> false
    }
}

fun UByte.isAlpha(): Boolean {
    return when (this) {
        in A_UNSIGNED..Z_UNSIGNED -> true
        in a_unsigned..z_unsigned -> true
        else -> false
    }
}

/**
 * DIGIT = 0x30-0x39; 0-9
 *
 * @return true or false
 */
fun Byte.isDigit(): Boolean {
    return this in N0..N9
}

fun UByte.isDigit(): Boolean {
    return this in N0_UNSIGNED..N9_UNSIGNED
}

/**
 * VCHAR = 0x21-0x7E; visible (printing) characters
 *
 * @return true or false
 */
fun Byte.isVChar(): Boolean {
    return this in EXCLAMATION_MARK..TILDE
}

fun UByte.isVChar(): Boolean {
    return this in EXCLAMATION_MARK_UNSIGNED..TILDE_UNSIGNED
}

/**
 * obs-text = 0x80-0xFF
 *
 * @return true or false
 */
fun Byte.isObsText(): Boolean {
    return this < 0
}

fun UByte.isObsText(): Boolean {
    return this >= 0x80u
}

/**
 * tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA; any VCHAR, except delimiters
 *
 * @return true or false
 */
fun Byte.isTChar(): Boolean {
    return when {
        this == EXCLAMATION_MARK -> true
        this in HASHTAG..APOSTROPHE -> true
        this == ASTERISK -> true
        this == PLUS -> true
        this == MINUS -> true
        this == DOT -> true
        this.isDigit() -> true
        this in A..Z -> true
        this in CARET..z -> true
        this == VERTICAL_BAR -> true
        this == TILDE -> true
        else -> false
    }
}

fun UByte.isTChar(): Boolean {
    return when {
        this == EXCLAMATION_MARK_UNSIGNED -> true
        this in HASHTAG_UNSIGNED..APOSTROPHE_UNSIGNED -> true
        this == ASTERISK_UNSIGNED -> true
        this == PLUS_UNSIGNED -> true
        this == MINUS_UNSIGNED -> true
        this == DOT_SIGNED -> true
        isDigit() -> true
        this in A_UNSIGNED..Z_UNSIGNED -> true
        this in CARET_UNSIGNED..z_unsigned -> true
        this == VERTICAL_BAR_UNSIGNED -> true
        this == TILDE_UNSIGNED -> true
        else -> false
    }
}

/**
 * reason-phrase = *( HTAB / SP / VCHAR / obs-text )
 *
 * @return true or false
 */
fun Byte.isReasonPhraseByte(): Boolean {
    return when {
        this < 0 -> true
        this == TAB -> true
        this == SP -> true
        this in EXCLAMATION_MARK..TILDE -> true
        else -> false
    }
}

fun UByte.isReasonPhraseByte(): Boolean {
    return when {
        this == TAB_UNSIGNED -> true
        this == SP_UNSIGNED -> true
        this in EXCLAMATION_MARK_UNSIGNED..TILDE_UNSIGNED -> true
        this >= 0x80u -> true
        else -> false
    }
}

fun UByte.isFieldVarChar(): Boolean {
    return this.isVChar() || this.isObsText()
}
