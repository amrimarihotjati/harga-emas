package us.goldprice.hargaemas.presentation.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.replace(Regex("[^0-9]"), "")
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = try {
            val formatRp = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
            formatRp.format(originalText.toLong())
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val validOffset = offset.coerceAtMost(originalText.length)
                var formattedOffset = 0
                var originalCount = 0
                while (originalCount < validOffset && formattedOffset < formattedText.length) {
                    if (formattedText[formattedOffset] == '.') {
                        formattedOffset++
                    } else {
                        originalCount++
                        formattedOffset++
                    }
                }
                return formattedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                val validOffset = offset.coerceAtMost(formattedText.length)
                var originalOffset = 0
                for (i in 0 until validOffset) {
                    if (formattedText[i] != '.') {
                        originalOffset++
                    }
                }
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
