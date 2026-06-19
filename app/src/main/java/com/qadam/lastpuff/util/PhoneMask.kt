package com.qadam.lastpuff.util

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

object PhoneMask {
    const val PLACEHOLDER = "+7 (___) ___-__-__"

    fun format(input: String): String {
        var digits = input.filter { it.isDigit() }
        if (digits.startsWith("8")) {
            digits = "7" + digits.drop(1)
        }
        if (digits.startsWith("7")) {
            digits = digits.drop(1)
        }
        digits = digits.take(10)

        if (digits.isEmpty()) return "+7"

        val builder = StringBuilder("+7 (")
        builder.append(digits.take(3))
        val rest = digits.drop(3)
        if (digits.length >= 3 && rest.isNotEmpty()) {
            builder.append(") ")
            builder.append(rest.take(3))
            val tail = rest.drop(3)
            if (rest.length >= 3 && tail.isNotEmpty()) {
                builder.append('-')
                builder.append(tail.take(2))
                val last = tail.drop(2)
                if (tail.length >= 2 && last.isNotEmpty()) {
                    builder.append('-')
                    builder.append(last.take(2))
                }
            }
        }
        return builder.toString()
    }

    fun isComplete(input: String): Boolean {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length == 11 && (digits.startsWith("7") || digits.startsWith("8")) -> true
            digits.length == 10 -> true
            else -> false
        }
    }
}

@Composable
fun PhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = PhoneMask.format(value),
        onValueChange = { onValueChange(PhoneMask.format(it)) },
        label = { Text(label) },
        placeholder = { Text(PhoneMask.PLACEHOLDER) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier
    )
}
