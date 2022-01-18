package io.yz.yzhttp.parser

import io.yz.yzhttp.constant.HttpVersion
import java.nio.ByteBuffer

class HttpVersionParser {
    companion object {
        private const val H: Byte = 0x48
        private const val P: Byte = 0x50
        private const val T: Byte = 0x54

        private const val DOT: Byte = 0x2E
        private const val SLASH: Byte = 0x2F

        private enum class Phase {
            P_H, P_T_1, P_T_2, P_P, P_SLASH, P_DIGIT_1, P_DOT, P_DIGIT_2, P_END,
        }
    }

    private val version: ByteArray = ByteArray(2)
    private var main: Phase = Phase.P_H

    private inline fun translateToWhen(buffer: ByteBuffer, newPhase: Phase, condition: (b: Byte) -> Boolean) {
        val b = buffer.get()

        if (!condition(b)) {
            throw ParseException()
        }

        main = newPhase
    }

    private inline fun translateToWhenThen(
        buffer: ByteBuffer,
        newPhase: Phase,
        condition: (b: Byte) -> Boolean,
        action: (b: Byte) -> Unit
    ) {
        val b = buffer.get()

        if (!condition(b)) {
            throw ParseException()
        }

        action(b)
        main = newPhase
    }

    val result: HttpVersion
        get() {
            check(end())
            return if (version[0] == '1'.code.toByte()) {
                if (version[1] == '0'.code.toByte()) {
                    HttpVersion.HTTP_1_0
                } else if (version[1] == '1'.code.toByte()) {
                    HttpVersion.HTTP_1_1
                } else {
                    throw NoSuchElementException()
                }
            } else {
                throw NoSuchElementException()
            }
        }

    fun feed(buffer: ByteBuffer): Int {
        val initValue = buffer.remaining()

        if (initValue == 0) {
            return 0
        }

        buffer.mark()

        L@ do {
            when (main) {
                Phase.P_H -> translateToWhen(buffer, Phase.P_T_1) { b -> b == H }
                Phase.P_T_1 -> translateToWhen(buffer, Phase.P_T_2) { b -> b == T }
                Phase.P_T_2 -> translateToWhen(buffer, Phase.P_P) { b -> b == T }
                Phase.P_P -> translateToWhen(buffer, Phase.P_SLASH) { b -> b == P }
                Phase.P_SLASH -> translateToWhen(buffer, Phase.P_DIGIT_1) { b -> b == SLASH }
                Phase.P_DIGIT_1 -> translateToWhenThen(buffer, Phase.P_DOT, { b -> b.isDigit() }) { b -> version[0] = b }
                Phase.P_DOT -> translateToWhen(buffer, Phase.P_DIGIT_2) { b -> b == DOT }
                Phase.P_DIGIT_2 -> translateToWhenThen(buffer, Phase.P_END, { b -> b.isDigit() }) { b -> version[1] = b }
                // 循环结束
                Phase.P_END -> break@L
            }
        } while (buffer.remaining() > 0)

        val finalValue = buffer.remaining()
        buffer.reset()

        return initValue - finalValue
    }

    fun end(): Boolean {
        return main == Phase.P_END
    }
}
