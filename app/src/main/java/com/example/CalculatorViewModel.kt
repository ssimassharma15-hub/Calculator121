package com.example

import androidx.lifecycle.ViewModel
import com.example.util.CalculatorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String
)

class CalculatorViewModel : ViewModel() {

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val operators = setOf('+', '-', '×', '÷')

    fun onKeyPress(key: String) {
        when (key) {
            "AC" -> {
                _expression.value = ""
                _previewResult.value = ""
            }
            "C" -> {
                _expression.value = ""
                _previewResult.value = ""
            }
            "⌫" -> {
                val current = _expression.value
                if (current.isNotEmpty()) {
                    _expression.value = current.dropLast(1)
                    updateLivePreview()
                }
            }
            "=" -> {
                val expr = _expression.value
                if (expr.isNotBlank()) {
                    try {
                        val cleaned = CalculatorParser.cleanForLivePreview(expr)
                        if (cleaned != null) {
                            val rawResult = CalculatorParser.evaluate(cleaned)
                            val formattedResult = formatResult(rawResult)
                            
                            // Add to history
                            val newHistory = listOf(HistoryItem(expression = expr, result = formattedResult)) + _history.value
                            _history.value = newHistory.take(50) // limit history size to last 50 entries
                            
                            // Set main expression to result
                            _expression.value = formattedResult
                            _previewResult.value = ""
                        }
                    } catch (e: Exception) {
                        _previewResult.value = "Error"
                    }
                }
            }
            "+/-" -> {
                _expression.value = toggleLastNumberSign(_expression.value)
                updateLivePreview()
            }
            "%" -> {
                val current = _expression.value
                if (current.isNotEmpty() && current.last().isDigit()) {
                    _expression.value = current + "%"
                    updateLivePreview()
                }
            }
            "(", ")" -> {
                handleParenthesis(key)
                updateLivePreview()
            }
            else -> {
                // Number or operator
                if (key in listOf("+", "-", "×", "÷")) {
                    handleOperator(key)
                } else if (key == ".") {
                    handleDecimal()
                } else {
                    // Standard digit
                    _expression.value += key
                }
                updateLivePreview()
            }
        }
    }

    private fun handleOperator(op: String) {
        val current = _expression.value
        if (current.isEmpty()) {
            if (op == "-") {
                _expression.value = "-"
            }
            return
        }

        val lastChar = current.last()
        if (lastChar in operators || lastChar == '.') {
            // Replace the last operator/decimal with the new operator
            _expression.value = current.dropLast(1) + op
        } else {
            _expression.value = current + op
        }
    }

    private fun handleDecimal() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "0."
            return
        }

        // Find the last segment (digits and decimals)
        val lastSegment = current.split('+', '-', '×', '÷', '(', ')').lastOrNull() ?: ""
        if (!lastSegment.contains('.')) {
            if (lastSegment.isEmpty() || current.last() == '(') {
                _expression.value = current + "0."
            } else {
                _expression.value = current + "."
            }
        }
    }

    private fun handleParenthesis(paren: String) {
        val current = _expression.value
        if (paren == "(") {
            // If empty or last char is operator or opening parenthesis, append directly
            if (current.isEmpty() || current.last() in operators || current.last() == '(') {
                _expression.value += "("
            } else {
                // Implicit multiplication (e.g., 5( => 5×()
                _expression.value += "×("
            }
        } else if (paren == ")") {
            // Only add closing parenthesis if we have open ones to match
            val openCount = current.count { it == '(' }
            val closeCount = current.count { it == ')' }
            if (openCount > closeCount) {
                val lastChar = current.last()
                if (lastChar.isDigit() || lastChar == ')' || lastChar == '%') {
                    _expression.value += ")"
                }
            }
        }
    }

    private fun updateLivePreview() {
        val expr = _expression.value
        if (expr.isBlank()) {
            _previewResult.value = ""
            return
        }

        try {
            val cleaned = CalculatorParser.cleanForLivePreview(expr)
            if (cleaned != null) {
                val rawResult = CalculatorParser.evaluate(cleaned)
                // If it's the exact same number as currently entered, no need for preview
                val cleanExprNoSpaces = expr.replace(" ", "")
                val formatted = formatResult(rawResult)
                if (cleanExprNoSpaces == formatted) {
                    _previewResult.value = ""
                } else {
                    _previewResult.value = formatted
                }
            } else {
                _previewResult.value = ""
            }
        } catch (e: Exception) {
            _previewResult.value = "" // Suppress errors in live preview
        }
    }

    private fun toggleLastNumberSign(expr: String): String {
        if (expr.isEmpty()) return "-"

        // Find index where the last token starts
        // We're looking for the last continuous block of digits and decimals
        var i = expr.length - 1
        while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) {
            i--
        }

        // If the entire string is a number, we just toggle it
        if (i < 0) {
            return "-$expr"
        }

        // Check if there is a '-' preceding it
        if (expr[i] == '-') {
            // Check if there's parenthesis before it, e.g., (-5
            if (i > 0 && expr[i - 1] == '(') {
                // If we have something like "10 + (-5", we want to toggle back to "10 + 5"
                // Drop the "(-" and let's check if there is a closing parenthesis or we just strip it
                val prefix = expr.substring(0, i - 1)
                val suffix = expr.substring(i + 1)
                return prefix + suffix
            } else {
                // Just toggle the minus sign directly
                val prefix = expr.substring(0, i)
                val suffix = expr.substring(i + 1)
                return prefix + suffix
            }
        }

        // Otherwise, insert minus
        // If there's an operator before, we can insert (-number
        val lastChar = expr[i]
        return if (lastChar in operators || lastChar == '(') {
            expr.substring(0, i + 1) + "(-" + expr.substring(i + 1)
        } else {
            // E.g. "5" -> "-5"
            expr + "×(-"
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "Error"
        
        // Round extremely close values to avoid floating point precision issues (e.g. 0.1 + 0.2 = 0.30000000000000004)
        val precision = 1e12
        val roundedValue = kotlin.math.round(value * precision) / precision

        // Check if it can be represented as a clean integer
        if (roundedValue == roundedValue.toLong().toDouble()) {
            return roundedValue.toLong().toString()
        }
        
        // Otherwise, format with up to 10 decimal places and strip trailing zeros
        val str = String.format(java.util.Locale.US, "%.10f", roundedValue)
        var trimmed = str.dropLastWhile { it == '0' }
        if (trimmed.endsWith(".")) {
            trimmed = trimmed.dropLast(1)
        }
        return trimmed
    }
}
