package io.yz.yzhttp.parser

import java.nio.ByteBuffer

class StatusCodeParser {
    companion object {
        private enum class Phase {
            P_DIGIT_1, P_DIGIT_2, P_DIGIT_3, P_END,
        }
    }

    private val statusCode: ByteArray = ByteArray(3)
    private var main: Phase = Phase.P_DIGIT_1

    private inline fun translateToWhenThen(
        buffer: ByteBuffer, newPhase: Phase, condition: (b: Byte) -> Boolean, action: (b: Byte) -> Unit
    ) {
        val b = buffer.get()

        if (!condition(b)) {
            throw ParseException()
        }

        action(b)
        main = newPhase
    }

    val result: Int
        get() {
            check(end())
            return statusCode[0] * 100 + statusCode[1] * 10 + statusCode[2]
        }

    fun feed(buffer: ByteBuffer): Int {
        val initValue = buffer.remaining()

        if (initValue == 0) {
            return 0
        }

        buffer.mark()

        L@ do {
            when (main) {
                Phase.P_DIGIT_1 -> translateToWhenThen(buffer, Phase.P_DIGIT_2, { b -> b.isDigit() }) { b ->
                    statusCode[0] = (b - 0x30).toByte()
                }
                Phase.P_DIGIT_2 -> translateToWhenThen(buffer, Phase.P_DIGIT_3, { b -> b.isDigit() }) { b ->
                    statusCode[1] = (b - 0x30).toByte()
                }
                Phase.P_DIGIT_3 -> translateToWhenThen(buffer, Phase.P_END, { b -> b.isDigit() }) { b ->
                    statusCode[2] = (b - 0x30).toByte()
                }
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
