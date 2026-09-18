package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ModernGold
import com.example.ui.theme.ModernOrange
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryDark
import com.example.ui.theme.ModernSilver

/**
 * High-fidelity representation of the Saint Stephen Deacons Service Logo
 * شعار خدمة الشهيد استفانوس للشمامسة - كنيستا العزب وأبي سيفين ودير الملاك بمير
 */
@Composable
fun SaintStephenLogoEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    showOuterRing: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(if (showOuterRing) 8.dp else 2.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val radius = canvasWidth / 2f

            // 1. Outer Metallic Silver Rim
            drawCircle(
                color = ModernSilver,
                radius = radius - 1f,
                center = center,
                style = Stroke(width = canvasWidth * 0.035f)
            )

            // 2. Wide Deep Midnight Navy Ring
            val navyRingThickness = canvasWidth * 0.19f
            val navyRingRadius = radius - (canvasWidth * 0.035f) - (navyRingThickness / 2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ModernPrimaryLightColor, ModernPrimaryDark),
                    center = center,
                    radius = radius
                ),
                radius = navyRingRadius,
                center = center,
                style = Stroke(width = navyRingThickness)
            )

            // 3. Fine Golden Inner Separator Line
            val innerGoldRadius = radius - (canvasWidth * 0.035f) - navyRingThickness
            drawCircle(
                color = ModernGold,
                radius = innerGoldRadius,
                center = center,
                style = Stroke(width = canvasWidth * 0.015f)
            )

            // 4. White Center Disc
            drawCircle(
                color = Color.White,
                radius = innerGoldRadius - 1f,
                center = center
            )

            // 5. Golden-Amber Halo Arch
            val haloRadius = innerGoldRadius * 0.78f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ModernOrange,
                        ModernGold,
                        Color(0xFFFDE047),
                        ModernGold,
                        ModernOrange
                    ),
                    center = center
                ),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(center.x - haloRadius, center.y - haloRadius - (innerGoldRadius * 0.05f)),
                size = Size(haloRadius * 2, haloRadius * 2),
                style = Stroke(width = canvasWidth * 0.08f, cap = StrokeCap.Round)
            )

            // 6. Draw Top Ornate Coptic Cross
            drawCopticCross(
                center = Offset(center.x, center.y - innerGoldRadius * 0.60f),
                size = canvasWidth * 0.18f
            )

            // 7. Draw Alpha (Ⲁ) and Omega (Ⲱ)
            drawAlphaOmegaSymbols(
                center = center,
                innerRadius = innerGoldRadius,
                scale = canvasWidth / 100f
            )

            // 8. Draw Saint Stephen Figure
            drawSaintStephenFigure(
                center = Offset(center.x, center.y + innerGoldRadius * 0.16f),
                width = innerGoldRadius * 1.15f,
                height = innerGoldRadius * 1.35f
            )

            // 9. Side Golden Crosses on the Navy Band (Separators)
            val crossSeparatorRadius = navyRingRadius
            val leftCrossOffset = Offset(
                center.x - crossSeparatorRadius * 0.94f,
                center.y + crossSeparatorRadius * 0.35f
            )
            val rightCrossOffset = Offset(
                center.x + crossSeparatorRadius * 0.94f,
                center.y + crossSeparatorRadius * 0.35f
            )
            drawMiniGoldCross(leftCrossOffset, canvasWidth * 0.06f)
            drawMiniGoldCross(rightCrossOffset, canvasWidth * 0.06f)
        }
    }
}

private val ModernPrimaryLightColor = Color(0xFF002A72)

/**
 * Ornate Coptic Cross at the top of the emblem
 */
private fun DrawScope.drawCopticCross(center: Offset, size: Float) {
    val halfSize = size / 2f
    val strokeWidth = size * 0.12f

    // Vertical beam
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(center.x, center.y - halfSize),
        end = Offset(center.x, center.y + halfSize),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    // Horizontal beam
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(center.x - halfSize, center.y),
        end = Offset(center.x + halfSize, center.y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Center rosette circle
    drawCircle(
        color = Color.White,
        radius = size * 0.22f,
        center = center
    )
    drawCircle(
        color = Color(0xFF1E293B),
        radius = size * 0.22f,
        center = center,
        style = Stroke(width = strokeWidth * 0.7f)
    )
    drawCircle(
        color = ModernGold,
        radius = size * 0.10f,
        center = center
    )

    // Ray finials on the 4 ends
    val finialRadius = size * 0.07f
    val offsets = listOf(
        Offset(center.x, center.y - halfSize),
        Offset(center.x, center.y + halfSize),
        Offset(center.x - halfSize, center.y),
        Offset(center.x + halfSize, center.y)
    )
    for (off in offsets) {
        drawCircle(color = Color(0xFF1E293B), radius = finialRadius, center = off)
    }
}

/**
 * Alpha & Omega in Coptic style (Ⲁ Ⲱ)
 */
private fun DrawScope.drawAlphaOmegaSymbols(center: Offset, innerRadius: Float, scale: Float) {
    val alphaCenter = Offset(center.x - innerRadius * 0.58f, center.y - innerRadius * 0.48f)
    val omegaCenter = Offset(center.x + innerRadius * 0.58f, center.y - innerRadius * 0.48f)

    // Alpha Ⲁ (sacred coptic loop)
    val alphaPath = Path().apply {
        moveTo(alphaCenter.x + 8f * scale, alphaCenter.y + 10f * scale)
        cubicTo(
            alphaCenter.x - 10f * scale, alphaCenter.y + 10f * scale,
            alphaCenter.x - 10f * scale, alphaCenter.y - 8f * scale,
            alphaCenter.x, alphaCenter.y - 8f * scale
        )
        cubicTo(
            alphaCenter.x + 8f * scale, alphaCenter.y - 8f * scale,
            alphaCenter.x + 10f * scale, alphaCenter.y,
            alphaCenter.x + 4f * scale, alphaCenter.y + 6f * scale
        )
        lineTo(alphaCenter.x + 10f * scale, alphaCenter.y + 12f * scale)
    }
    drawPath(
        path = alphaPath,
        color = Color(0xFF1E293B),
        style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round)
    )

    // Omega Ⲱ (coptic broad curve)
    val omegaPath = Path().apply {
        moveTo(omegaCenter.x - 9f * scale, omegaCenter.y + 10f * scale)
        lineTo(omegaCenter.x - 5f * scale, omegaCenter.y + 10f * scale)
        cubicTo(
            omegaCenter.x - 9f * scale, omegaCenter.y - 6f * scale,
            omegaCenter.x - 1f * scale, omegaCenter.y - 6f * scale,
            omegaCenter.x, omegaCenter.y + 6f * scale
        )
        cubicTo(
            omegaCenter.x + 1f * scale, omegaCenter.y - 6f * scale,
            omegaCenter.x + 9f * scale, omegaCenter.y - 6f * scale,
            omegaCenter.x + 5f * scale, omegaCenter.y + 10f * scale
        )
        lineTo(omegaCenter.x + 9f * scale, omegaCenter.y + 10f * scale)
    }
    drawPath(
        path = omegaPath,
        color = Color(0xFF1E293B),
        style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round)
    )
}

/**
 * Saint Stephen kneeling with outstretched hands and deacon orarion stole
 */
private fun DrawScope.drawSaintStephenFigure(center: Offset, width: Float, height: Float) {
    val strokeColor = Color(0xFF0F172A)
    val lineWidth = width * 0.024f

    // 1. Saint's Halo behind head
    val headCenter = Offset(center.x, center.y - height * 0.32f)
    val headRadius = width * 0.16f
    drawCircle(
        color = Color.White,
        radius = headRadius * 1.45f,
        center = headCenter
    )
    drawCircle(
        color = strokeColor,
        radius = headRadius * 1.45f,
        center = headCenter,
        style = Stroke(width = lineWidth)
    )

    // 2. Head contour & Coptic curls
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = headCenter
    )
    drawCircle(
        color = strokeColor,
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = lineWidth * 0.9f)
    )

    // 3. Deacon White Tunic (Tonia) body
    val tunicPath = Path().apply {
        // Neckline
        moveTo(headCenter.x - headRadius * 0.6f, headCenter.y + headRadius * 0.9f)
        // Left shoulder & sleeve
        lineTo(headCenter.x - width * 0.42f, headCenter.y + height * 0.22f)
        // Left forearm outstretched in prayer
        lineTo(headCenter.x - width * 0.52f, headCenter.y + height * 0.18f)
        // Left hand
        lineTo(headCenter.x - width * 0.42f, headCenter.y + height * 0.30f)
        // Left torso
        lineTo(headCenter.x - width * 0.28f, headCenter.y + height * 0.36f)
        // Kneeling tunic drape at base
        cubicTo(
            headCenter.x - width * 0.34f, headCenter.y + height * 0.70f,
            headCenter.x - width * 0.15f, headCenter.y + height * 0.76f,
            headCenter.x, headCenter.y + height * 0.75f
        )
        cubicTo(
            headCenter.x + width * 0.15f, headCenter.y + height * 0.76f,
            headCenter.x + width * 0.28f, headCenter.y + height * 0.70f,
            headCenter.x + width * 0.28f, headCenter.y + height * 0.36f
        )
        // Right hand & arm outstretched
        lineTo(headCenter.x + width * 0.42f, headCenter.y + height * 0.30f)
        lineTo(headCenter.x + width * 0.52f, headCenter.y + height * 0.18f)
        lineTo(headCenter.x + width * 0.42f, headCenter.y + height * 0.22f)
        // Right shoulder
        lineTo(headCenter.x + headRadius * 0.6f, headCenter.y + headRadius * 0.9f)
        close()
    }
    drawPath(path = tunicPath, color = Color.White)
    drawPath(path = tunicPath, color = strokeColor, style = Stroke(width = lineWidth))

    // 4. Deacon Embroidered Orarion / Batrasheel Stole (البطرشيل الشماسي)
    val stolePath = Path().apply {
        moveTo(headCenter.x - headRadius * 0.38f, headCenter.y + headRadius * 1.0f)
        lineTo(headCenter.x - headRadius * 0.38f, headCenter.y + height * 0.73f)
        lineTo(headCenter.x + headRadius * 0.38f, headCenter.y + height * 0.73f)
        lineTo(headCenter.x + headRadius * 0.38f, headCenter.y + headRadius * 1.0f)
        close()
    }
    drawPath(path = stolePath, color = Color(0xFFFFFBEB))
    drawPath(path = stolePath, color = strokeColor, style = Stroke(width = lineWidth * 0.8f))

    // Crosses on deacon stole
    val stoleYStart = headCenter.y + headRadius * 1.3f
    val stoleStep = (height * 0.55f) / 4f
    for (i in 0..3) {
        val y = stoleYStart + i * stoleStep
        drawLine(
            color = strokeColor,
            start = Offset(headCenter.x, y - width * 0.035f),
            end = Offset(headCenter.x, y + width * 0.035f),
            strokeWidth = lineWidth * 0.7f
        )
        drawLine(
            color = strokeColor,
            start = Offset(headCenter.x - width * 0.035f, y),
            end = Offset(headCenter.x + width * 0.035f, y),
            strokeWidth = lineWidth * 0.7f
        )
    }

    // Belt buckle medallion at waist
    val waistY = headCenter.y + height * 0.33f
    drawCircle(color = ModernGold, radius = width * 0.075f, center = Offset(headCenter.x, waistY))
    drawCircle(color = strokeColor, radius = width * 0.075f, center = Offset(headCenter.x, waistY), style = Stroke(width = lineWidth * 0.7f))
}

/**
 * Mini Golden Coptic Cross separators on the Navy rim
 */
private fun DrawScope.drawMiniGoldCross(center: Offset, size: Float) {
    val half = size / 2f
    val strokeWidth = size * 0.22f

    drawLine(
        color = ModernGold,
        start = Offset(center.x, center.y - half),
        end = Offset(center.x, center.y + half),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = ModernGold,
        start = Offset(center.x - half, center.y),
        end = Offset(center.x + half, center.y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = Color.White,
        radius = size * 0.16f,
        center = center
    )
}

/**
 * Complete Service Emblem Header with Church and Service Inscriptions
 */
@Composable
fun ServiceBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 100.dp,
    textColor: Color = Color.White
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SaintStephenLogoEmblem(size = logoSize)

        Spacer(modifier = Modifier.height(12.dp))

        // Lower text from logo
        Text(
            text = "خدمة الشهيد استفانوس للشمامسة",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 20.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Upper text from logo
        Text(
            text = "كنيستا العزب وأبي سيفين ودير الملاك بمير",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = ModernGold,
                fontSize = 13.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
