package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * مولد باركود Code 128 مخصص للرسم المباشر في Compose
 */
object BarcodeUtil {

    // أنماط قضبان Code 128 لتمثيل الرموز (تتابع عرض الخطوط والمسافات)
    private val patterns = arrayOf(
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312",
        "132212", "221213", "221312", "231212", "112232", "122132", "122231", "113222",
        "123122", "123221", "223211", "221132", "221231", "213212", "223112", "312131",
        "311222", "321122", "321221", "312212", "322112", "322211", "212123", "212321",
        "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121",
        "313121", "211331", "231131", "213113", "213311", "213131", "311123", "311321",
        "331121", "312113", "312311", "332111", "314111", "221411", "431111", "111224",
        "111422", "121124", "121421", "141122", "141221", "112214", "112412", "122114",
        "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112",
        "421211", "212141", "214121", "412121", "111143", "111341", "131141", "114113",
        "114311", "411113", "411311", "113141", "114131", "311141", "411131", "211412",
        "211214", "211232", "2331112"
    )

    fun encodeToBars(input: String): String {
        val safeInput = if (input.isBlank()) "DK-1000" else input.uppercase()
        val builder = StringBuilder()
        
        // Start Code B
        builder.append(patterns[104])
        
        for (ch in safeInput) {
            val code = (ch.code - 32).coerceIn(0, 95)
            builder.append(patterns[code])
        }
        
        // Stop Code
        builder.append(patterns[106])
        return builder.toString()
    }
}

@Composable
fun Code128Barcode(
    code: String,
    modifier: Modifier = Modifier,
    barHeight: Dp = 48.dp,
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val barPattern = BarcodeUtil.encodeToBars(code)

    Column(
        modifier = modifier
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val totalUnits = barPattern.sumOf { it.digitToInt() }
            val unitWidth = size.width / totalUnits.toFloat()
            
            var currentX = 0f
            var isBar = true

            for (char in barPattern) {
                val count = char.digitToInt()
                val segmentWidth = count * unitWidth
                if (isBar) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(currentX, 0f),
                        size = Size(segmentWidth, size.height)
                    )
                }
                currentX += segmentWidth
                isBar = !isBar
            }
        }

        Text(
            text = code,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = barColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
