package io.yz.yzhttp.parser

import java.nio.ByteBuffer

class ReasonPhraseParser {
    companion object {
        private enum class Phase {
            P_DIGIT_1, P_DIGIT_2, P_DIGIT_3, P_END,
        }
    }

    private val reasonPhrase: MutableList<Byte> = mutableListOf()
    private var main: Phase = Phase.P_DIGIT_1

    private inline fun translateToWhenThen(
        buffer: ByteBuffer, newPhase: Phase, condition: (b: Byte) -> Boolean, action: (b: Byte) -> Unit
    ) {
        val b: Byte = buffer.get()

        if (!condition(b)) {
            throw ParseException()
        }

        action(b)
        main = newPhase
    }

    val result: UInt
        get() {
            check(end())
            throw NotImplementedError()
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
                    reasonPhrase.add(b)
                }
                Phase.P_DIGIT_2 -> translateToWhenThen(buffer, Phase.P_DIGIT_3, { b -> b.isDigit() }) { b ->
                    reasonPhrase.add(b)
                }
                Phase.P_DIGIT_3 -> translateToWhenThen(buffer, Phase.P_END, { b -> b.isDigit() }) { b ->
                    reasonPhrase.add(b)
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
