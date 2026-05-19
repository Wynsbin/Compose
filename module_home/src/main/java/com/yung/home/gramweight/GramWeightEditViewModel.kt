package com.yung.home.gramweight

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import kotlin.text.iterator
import org.orbitmvi.orbit.viewmodel.container as orbitContainer

data class GramWeightEditState(
    val input: String,
)

sealed interface GramWeightEditSideEffect {
    data object Close : GramWeightEditSideEffect
    data class Complete(val gramsText: String) : GramWeightEditSideEffect
    data class InvalidInput(val reason: String) : GramWeightEditSideEffect
}

class GramWeightEditViewModel(
    initialInput: String,
) : ViewModel(), ContainerHost<GramWeightEditState, GramWeightEditSideEffect> {

    override val container: Container<GramWeightEditState, GramWeightEditSideEffect> =
        orbitContainer(
            GramWeightEditState(input = sanitizeInitial(initialInput)),
        )

    fun onDigitOrDot(c: Char) = intent {
        reduce { state.copy(input = appendChar(state.input, c)) }
    }

    fun onBackspace() = intent {
        val s = state.input
        if (s.isNotEmpty()) {
            reduce { state.copy(input = s.dropLast(1)) }
        }
    }

    fun onClear() = intent {
        reduce { state.copy(input = "") }
    }

    fun onClose() = intent {
        postSideEffect(GramWeightEditSideEffect.Close)
    }

    fun onComplete() = intent {
        val text = state.input.trim()
        if (text.isEmpty() || text == "." || text.endsWith(".")) {
            postSideEffect(GramWeightEditSideEffect.InvalidInput("请输入有效克重"))
            return@intent
        }
        val value = text.toDoubleOrNull()
        if (value == null || value <= 0.0) {
            postSideEffect(GramWeightEditSideEffect.InvalidInput("请输入大于 0 的克重"))
            return@intent
        }
        val normalized = trimTrailingZeros(text)
        postSideEffect(GramWeightEditSideEffect.Complete(normalized))
    }

    companion object {
        private const val MAX_FRACTION_DIGITS = 2

        fun sanitizeInitial(raw: String): String {
            var s = ""
            for (ch in raw.trim()) {
                if (ch.isDigit() || ch == '.') s = appendChar(s, ch)
            }
            return s
        }

        private fun trimTrailingZeros(text: String): String {
            if (!text.contains('.')) return text
            return text.trimEnd('0').trimEnd('.').ifEmpty { "0" }
        }

        fun appendChar(current: String, ch: Char): String {
            if (ch == '.') {
                if (current.contains('.')) return current
                return if (current.isEmpty()) "0." else "$current."
            }
            if (!ch.isDigit()) return current

            val dot = current.indexOf('.')
            if (dot >= 0) {
                val fracLen = current.length - dot - 1
                if (fracLen >= MAX_FRACTION_DIGITS) return current
            }

            if (current == "0") return ch.toString()
            if (current.startsWith("0.")) return current + ch
            if (dot == -1 && current.startsWith('0')) return ch.toString()
            return current + ch
        }
    }
}
