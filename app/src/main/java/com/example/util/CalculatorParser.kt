package com.example.util

import kotlin.math.round

object CalculatorParser {
    /**
     * Evaluates a mathematical expression and returns the result as a Double.
     * Throws an exception if the expression is invalid.
     */
    fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return 0.0
        val parser = Parser(tokens)
        return parser.parse()
    }

    /**
     * Safely cleans trailing operators and unclosed parentheses for a live preview.
     */
    fun cleanForLivePreview(expression: String): String? {
        if (expression.isBlank()) return null
        
        var clean = expression.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        if (clean.isEmpty()) return null

        // Remove trailing operators and decimals
        val operators = setOf('+', '-', '*', '/', '%', '.')
        while (clean.isNotEmpty() && clean.last() in operators) {
            clean = clean.dropLast(1)
        }

        if (clean.isEmpty()) return null

        // Balance parentheses for evaluation
        var openCount = clean.count { it == '(' }
        var closeCount = clean.count { it == ')' }
        while (openCount > closeCount) {
            clean += ")"
            closeCount++
        }

        // If there's an uneven number of closing parentheses, we'll try to let parser throw or handle it
        return if (clean.isNotEmpty()) clean else null
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        sb.append(expression[i])
                        i++
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == '%' -> {
                    tokens.add(c.toString())
                }
            }
            i++
        }
        return tokens
    }

    private class Parser(private val tokens: List<String>) {
        private var index = 0

        fun parse(): Double {
            val result = parseExpression()
            if (index < tokens.size) {
                throw IllegalArgumentException("Unexpected token")
            }
            return result
        }

        private fun parseExpression(): Double {
            var result = parseTerm()
            while (index < tokens.size) {
                val token = tokens[index]
                if (token == "+" || token == "-") {
                    index++
                    val right = parseTerm()
                    if (token == "+") result += right else result -= right
                } else {
                    break
                }
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (index < tokens.size) {
                val token = tokens[index]
                if (token == "*" || token == "/") {
                    index++
                    val right = parseFactor()
                    if (token == "*") {
                        result *= right
                    } else {
                        if (right == 0.0) throw ArithmeticException("Division by zero")
                        result /= right
                    }
                } else {
                    break
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            if (index >= tokens.size) throw IllegalArgumentException("Unexpected end")
            var token = tokens[index]
            
            // Handle unary plus/minus
            var multiplier = 1.0
            while (token == "+" || token == "-") {
                if (token == "-") multiplier *= -1.0
                index++
                if (index >= tokens.size) throw IllegalArgumentException("Unexpected end")
                token = tokens[index]
            }

            var result: Double
            if (token == "(") {
                index++ // consume '('
                result = parseExpression()
                if (index >= tokens.size || tokens[index] != ")") {
                    throw IllegalArgumentException("Missing closed parenthesis")
                }
                index++ // consume ')'
            } else {
                result = token.toDoubleOrNull() ?: 0.0
                index++
            }

            // Handle percentage modifier immediately following a factor or parenthesized expression
            if (index < tokens.size && tokens[index] == "%") {
                result /= 100.0
                index++
            }

            return result * multiplier
        }
    }
}
