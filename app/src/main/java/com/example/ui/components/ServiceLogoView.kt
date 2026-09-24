package com.example.ui.components

import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ========================================================
// Official Coptic Orthodox Deacons Service Emblem Colors
// مستوحى بدقة 100% من الشعار الرسمي لخدمة الشمامسة:
// نبيتي داكن ملكي + ذهب أثري وقاد + أحمر طقسي قبطي + عاجي
// ========================================================
private val EmblemBurgundyDark = Color(0xFF26040A)
private val EmblemBurgundy = Color(0xFF3B0A12)
private val EmblemBurgundyMedium = Color(0xFF4E0E18)
private val EmblemBurgundyLight = Color(0xFF6B1522)

private val EmblemGoldBright = Color(0xFFFDE047)
private val EmblemGold = Color(0xFFF59E0B)
private val EmblemGoldAmber = Color(0xFFD97706)
private val EmblemGoldDark = Color(0xFF92400E)

private val EmblemRedCrimson = Color(0xFF991B1B)
private val EmblemRedVibrant = Color(0xFFB91C1C)
private val EmblemRedRuby = Color(0xFF7F1D1D)

private val EmblemIvory = Color(0xFFFFFBEB)
private val EmblemWhite = Color(0xFFFFFFFF)

/**
 * Official Coptic Orthodox Deacons Service Emblem
 * شعار إدارة خدمة الشمامسة - الكنيسة القبطية الأرثوذكسية
 * ⲠⲈⲚⲞⲒⲤ ⲚⲈⲘ ⲚⲈⲚⲈϤⲆⲒⲀⲔⲞⲚⲞⲤ
 * COPTIC ORTHODOX DEACONS SERVICE MANAGEMENT
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
            .aspectRatio(1f)
            .shadow(if (showOuterRing) 8.dp else 2.dp, CircleShape)
            .clip(CircleShape)
            .background(EmblemBurgundyDark),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .aspectRatio(1f)
        ) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val radius = canvasWidth / 2f

            // 1. Base Dark Burgundy Gradient Disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(EmblemBurgundyLight, EmblemBurgundy, EmblemBurgundyDark),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Outer Braided Golden Rope Rim
            val ropeRadius = radius - (canvasWidth * 0.024f)
            drawCircle(
                color = EmblemGoldAmber,
                radius = ropeRadius,
                center = center,
                style = Stroke(width = canvasWidth * 0.026f)
            )
            // Rope notches for braided metallic texture
            val ropeSegments = 64
            for (i in 0 until ropeSegments) {
                val angle = (i * 360f / ropeSegments) * (Math.PI / 180f)
                val rx = center.x + ropeRadius * cos(angle).toFloat()
                val ry = center.y + ropeRadius * sin(angle).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) EmblemGoldBright else EmblemGoldDark,
                    radius = canvasWidth * 0.011f,
                    center = Offset(rx, ry)
                )
            }

            // 3. Concentric Gold Accent Rings
            val outerRingRadius = ropeRadius - (canvasWidth * 0.024f)
            drawCircle(
                color = EmblemGoldBright,
                radius = outerRingRadius,
                center = center,
                style = Stroke(width = canvasWidth * 0.008f)
            )

            val innerRingRadius = radius * 0.68f
            drawCircle(
                color = EmblemGold,
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = canvasWidth * 0.010f)
            )
            drawCircle(
                color = EmblemGoldDark,
                radius = innerRingRadius - (canvasWidth * 0.012f),
                center = center,
                style = Stroke(width = canvasWidth * 0.006f)
            )

            // 4. Circular Inscriptions on Burgundy Band
            drawCircularEmblemTexts(center, radius, canvasWidth)

            // 5. Four Decorative Coptic Star Crosses on the band (at 90°, 270°, etc.)
            drawSideDecorativeCrosses(center, (outerRingRadius + innerRingRadius) / 2f, canvasWidth)

            // 6. Central Sacred Disc - Sunburst Radiance
            val centerDiscRadius = innerRingRadius - (canvasWidth * 0.014f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(EmblemBurgundyMedium, EmblemBurgundy, EmblemBurgundyDark),
                    center = center,
                    radius = centerDiscRadius
                ),
                radius = centerDiscRadius,
                center = center
            )

            // Sunburst Light Rays behind Cross
            drawSunburstRays(center, centerDiscRadius * 0.85f, canvasWidth)

            // 7. Draped Liturgical Red Vestment Ribbon behind Cross
            drawDrapedVestmentRibbon(center, centerDiscRadius, canvasWidth)

            // 8. Sacred Golden Coptic Censer (Shorya) on Left with Incense Smoke & Chains
            drawGoldenCenser(center, canvasWidth)

            // 9. Sacred Liturgical Deacon Stole (Batrasheel) on Right with Crosses & Tassels
            drawDeaconStole(center, canvasWidth)

            // 10. Grand Ornate Gilded Coptic Cross in Center
            drawCentralCopticCross(center, canvasWidth)

            // 11. Open Holy Gospel Book at the foot of the Cross
            drawOpenHolyGospel(center, canvasWidth)

            // 12. Golden Coptic Cymbals (الدف والصاجات) at Lower Right
            drawCopticCymbals(center, canvasWidth)
        }
    }
}

/**
 * Circular Texts:
 * Top Arc: ⲠⲈⲚⲞⲒⲤ ⲚⲈⲘ ⲚⲈⲚⲈϤⲆⲒⲀⲔⲞⲚⲞⲤ (Coptic)
 * Bottom Outer Arc: COPTIC ORTHODOX DEACONS SERVICE MANAGEMENT (English)
 * Bottom Inner Arc: إدارة خدمة الشمامسة - الكنيسة القبطية الأرثوذكسية (Arabic)
 */
private fun DrawScope.drawCircularEmblemTexts(center: Offset, radius: Float, canvasWidth: Float) {
    drawContext.canvas.nativeCanvas.apply {
        // Upper Coptic Text
        val copticPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FDE047")
            textSize = canvasWidth * 0.060f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.parseColor("#8026040A"))
        }

        val topPath = AndroidPath().apply {
            val r = radius * 0.82f
            addArc(
                center.x - r,
                center.y - r,
                center.x + r,
                center.y + r,
                200f,
                140f
            )
        }
        drawTextOnPath("ⲠⲈⲚⲞⲒⲤ ⲚⲈⲘ ⲚⲈⲚⲈϤⲆⲒⲀⲔⲞⲚⲞⲤ", topPath, 0f, 0f, copticPaint)

        // Lower Outer English Text
        val englishPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F59E0B")
            textSize = canvasWidth * 0.034f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
        }

        val bottomOuterPath = AndroidPath().apply {
            val r = radius * 0.82f
            addArc(
                center.x - r,
                center.y - r,
                center.x + r,
                center.y + r,
                175f,
                -170f
            )
        }
        drawTextOnPath("COPTIC ORTHODOX DEACONS SERVICE MANAGEMENT", bottomOuterPath, 0f, 0f, englishPaint)

        // Lower Inner Arabic Text
        val arabicPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFFBEB")
            textSize = canvasWidth * 0.034f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val bottomInnerPath = AndroidPath().apply {
            val r = radius * 0.70f
            addArc(
                center.x - r,
                center.y - r,
                center.x + r,
                center.y + r,
                165f,
                -150f
            )
        }
        drawTextOnPath("إدارة خدمة الشمامسة - الكنيسة القبطية الأرثوذكسية", bottomInnerPath, 0f, 0f, arabicPaint)
    }
}

/**
 * Four decorative Coptic crosses / stars along the outer ring
 */
private fun DrawScope.drawSideDecorativeCrosses(center: Offset, bandRadius: Float, canvasWidth: Float) {
    val crossSize = canvasWidth * 0.028f
    val angles = listOf(175f, 5f)
    for (ang in angles) {
        val rad = Math.toRadians(ang.toDouble())
        val cx = center.x + (bandRadius * cos(rad)).toFloat()
        val cy = center.y + (bandRadius * sin(rad)).toFloat()

        // Diamond cross shape
        val path = Path().apply {
            moveTo(cx, cy - crossSize)
            lineTo(cx + crossSize * 0.4f, cy - crossSize * 0.4f)
            lineTo(cx + crossSize, cy)
            lineTo(cx + crossSize * 0.4f, cy + crossSize * 0.4f)
            lineTo(cx, cy + crossSize)
            lineTo(cx - crossSize * 0.4f, cy + crossSize * 0.4f)
            lineTo(cx - crossSize, cy)
            lineTo(cx - crossSize * 0.4f, cy - crossSize * 0.4f)
            close()
        }
        drawPath(path, color = EmblemGoldBright)
        drawCircle(color = EmblemBurgundyDark, radius = crossSize * 0.25f, center = Offset(cx, cy))
    }
}

/**
 * Radiant Sunburst Light Rays behind Cross
 */
private fun DrawScope.drawSunburstRays(center: Offset, maxRayRadius: Float, canvasWidth: Float) {
    val rayCount = 28
    val innerR = canvasWidth * 0.08f
    for (i in 0 until rayCount) {
        val angleDeg = i * (360f / rayCount)
        val rad = Math.toRadians(angleDeg.toDouble())
        val isLong = i % 2 == 0
        val outerR = if (isLong) maxRayRadius else maxRayRadius * 0.72f
        val strokeW = if (isLong) canvasWidth * 0.007f else canvasWidth * 0.004f

        val start = Offset(
            center.x + (innerR * cos(rad)).toFloat(),
            center.y + (innerR * sin(rad)).toFloat()
        )
        val end = Offset(
            center.x + (outerR * cos(rad)).toFloat(),
            center.y + (outerR * sin(rad)).toFloat()
        )
        drawLine(
            color = EmblemGold.copy(alpha = if (isLong) 0.55f else 0.35f),
            start = start,
            end = end,
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Flowing Liturgical Crimson Ribbon draped behind central cross
 */
private fun DrawScope.drawDrapedVestmentRibbon(center: Offset, centerRadius: Float, canvasWidth: Float) {
    val ribbonPath = Path().apply {
        val yOffset = center.y - (canvasWidth * 0.06f)
        moveTo(center.x - (canvasWidth * 0.24f), yOffset - (canvasWidth * 0.02f))
        quadraticTo(
            center.x, yOffset + (canvasWidth * 0.16f),
            center.x + (canvasWidth * 0.24f), yOffset - (canvasWidth * 0.02f)
        )
        lineTo(center.x + (canvasWidth * 0.21f), yOffset + (canvasWidth * 0.06f))
        quadraticTo(
            center.x, yOffset + (canvasWidth * 0.22f),
            center.x - (canvasWidth * 0.21f), yOffset + (canvasWidth * 0.06f)
        )
        close()
    }
    drawPath(ribbonPath, color = EmblemRedCrimson)
    drawPath(
        ribbonPath,
        color = EmblemGoldAmber,
        style = Stroke(width = canvasWidth * 0.006f)
    )
}

/**
 * Grand Ornate Gilded Coptic Cross in Center
 */
private fun DrawScope.drawCentralCopticCross(center: Offset, canvasWidth: Float) {
    val crossCenterY = center.y - (canvasWidth * 0.12f)
    val crossCenterX = center.x
    val verticalHalf = canvasWidth * 0.19f
    val horizontalHalf = canvasWidth * 0.16f
    val barWidth = canvasWidth * 0.040f

    // 1. Cross Bars with Gold Fill and Amber Outline
    // Vertical Bar
    drawRect(
        color = EmblemGoldBright,
        topLeft = Offset(crossCenterX - (barWidth / 2f), crossCenterY - verticalHalf),
        size = Size(barWidth, verticalHalf * 2f)
    )
    drawRect(
        color = EmblemGoldDark,
        topLeft = Offset(crossCenterX - (barWidth / 2f), crossCenterY - verticalHalf),
        size = Size(barWidth, verticalHalf * 2f),
        style = Stroke(width = canvasWidth * 0.005f)
    )

    // Horizontal Bar
    drawRect(
        color = EmblemGoldBright,
        topLeft = Offset(crossCenterX - horizontalHalf, crossCenterY - (barWidth / 2f)),
        size = Size(horizontalHalf * 2f, barWidth)
    )
    drawRect(
        color = EmblemGoldDark,
        topLeft = Offset(crossCenterX - horizontalHalf, crossCenterY - (barWidth / 2f)),
        size = Size(horizontalHalf * 2f, barWidth),
        style = Stroke(width = canvasWidth * 0.005f)
    )

    // 2. Trefoil Finials at the 4 extremities
    val finialRadius = canvasWidth * 0.024f
    val tips = listOf(
        Offset(crossCenterX, crossCenterY - verticalHalf), // Top
        Offset(crossCenterX, crossCenterY + verticalHalf), // Bottom
        Offset(crossCenterX - horizontalHalf, crossCenterY), // Left
        Offset(crossCenterX + horizontalHalf, crossCenterY)  // Right
    )

    for (tip in tips) {
        // Main central bulb
        drawCircle(color = EmblemGoldBright, radius = finialRadius, center = tip)
        drawCircle(color = EmblemGoldDark, radius = finialRadius, center = tip, style = Stroke(canvasWidth * 0.004f))

        // Side small pearls
        if (tip.x == crossCenterX) {
            // Top/Bottom finials side pearls
            drawCircle(color = EmblemGold, radius = finialRadius * 0.65f, center = Offset(tip.x - finialRadius * 0.9f, tip.y))
            drawCircle(color = EmblemGold, radius = finialRadius * 0.65f, center = Offset(tip.x + finialRadius * 0.9f, tip.y))
        } else {
            // Left/Right finials side pearls
            drawCircle(color = EmblemGold, radius = finialRadius * 0.65f, center = Offset(tip.x, tip.y - finialRadius * 0.9f))
            drawCircle(color = EmblemGold, radius = finialRadius * 0.65f, center = Offset(tip.x, tip.y + finialRadius * 0.9f))
        }
    }

    // 3. Central Medallion with Ruby Gemstone
    val centerCircleRadius = canvasWidth * 0.046f
    drawCircle(
        color = EmblemGoldBright,
        radius = centerCircleRadius,
        center = Offset(crossCenterX, crossCenterY)
    )
    drawCircle(
        color = EmblemGoldDark,
        radius = centerCircleRadius,
        center = Offset(crossCenterX, crossCenterY),
        style = Stroke(width = canvasWidth * 0.007f)
    )
    // Inner Ruby Jewel
    drawCircle(
        color = EmblemRedRuby,
        radius = centerCircleRadius * 0.60f,
        center = Offset(crossCenterX, crossCenterY)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = centerCircleRadius * 0.22f,
        center = Offset(crossCenterX - (centerCircleRadius * 0.2f), crossCenterY - (centerCircleRadius * 0.2f))
    )
}

/**
 * Sacred Golden Coptic Censer (Shorya / المبخرة) on Left
 * Three chains with bells, dome lid with perforations, round bowl, and gentle incense smoke.
 */
private fun DrawScope.drawGoldenCenser(center: Offset, canvasWidth: Float) {
    val censerX = center.x - (canvasWidth * 0.26f)
    val censerY = center.y + (canvasWidth * 0.06f)

    // 1. Incense Smoke Curling Upward
    val smokePath = Path().apply {
        moveTo(censerX, censerY - (canvasWidth * 0.10f))
        cubicTo(
            censerX - (canvasWidth * 0.03f), censerY - (canvasWidth * 0.16f),
            censerX + (canvasWidth * 0.04f), censerY - (canvasWidth * 0.22f),
            censerX - (canvasWidth * 0.01f), censerY - (canvasWidth * 0.28f)
        )
    }
    drawPath(
        smokePath,
        color = Color.White.copy(alpha = 0.65f),
        style = Stroke(width = canvasWidth * 0.010f, cap = StrokeCap.Round)
    )

    // 2. Three Chains Hanging Down
    val topHanger = Offset(censerX, censerY - (canvasWidth * 0.20f))
    val chainWidth = canvasWidth * 0.005f
    // Center chain
    drawLine(
        color = EmblemGold,
        start = topHanger,
        end = Offset(censerX, censerY - (canvasWidth * 0.06f)),
        strokeWidth = chainWidth
    )
    // Left chain
    drawLine(
        color = EmblemGold,
        start = topHanger,
        end = Offset(censerX - (canvasWidth * 0.038f), censerY - (canvasWidth * 0.06f)),
        strokeWidth = chainWidth
    )
    // Right chain
    drawLine(
        color = EmblemGold,
        start = topHanger,
        end = Offset(censerX + (canvasWidth * 0.038f), censerY - (canvasWidth * 0.06f)),
        strokeWidth = chainWidth
    )

    // Little Bells along the chains
    val bellPositions = listOf(
        Offset(censerX - (canvasWidth * 0.02f), censerY - (canvasWidth * 0.13f)),
        Offset(censerX + (canvasWidth * 0.02f), censerY - (canvasWidth * 0.13f)),
        Offset(censerX, censerY - (canvasWidth * 0.10f))
    )
    for (bell in bellPositions) {
        drawCircle(color = EmblemGoldBright, radius = canvasWidth * 0.008f, center = bell)
    }

    // 3. Censer Dome & Pierced Holes
    val domePath = Path().apply {
        moveTo(censerX - (canvasWidth * 0.042f), censerY - (canvasWidth * 0.04f))
        cubicTo(
            censerX - (canvasWidth * 0.035f), censerY - (canvasWidth * 0.09f),
            censerX + (canvasWidth * 0.035f), censerY - (canvasWidth * 0.09f),
            censerX + (canvasWidth * 0.042f), censerY - (canvasWidth * 0.04f)
        )
        close()
    }
    drawPath(domePath, color = EmblemGoldBright)
    drawPath(domePath, color = EmblemGoldDark, style = Stroke(canvasWidth * 0.004f))

    // 4. Censer Bowl
    val bowlPath = Path().apply {
        moveTo(censerX - (canvasWidth * 0.045f), censerY - (canvasWidth * 0.04f))
        cubicTo(
            censerX - (canvasWidth * 0.040f), censerY + (canvasWidth * 0.03f),
            censerX + (canvasWidth * 0.040f), censerY + (canvasWidth * 0.03f),
            censerX + (canvasWidth * 0.045f), censerY - (canvasWidth * 0.04f)
        )
        close()
    }
    drawPath(bowlPath, color = EmblemGold)
    drawPath(bowlPath, color = EmblemGoldDark, style = Stroke(canvasWidth * 0.004f))

    // 5. Censer Stand/Base
    drawRect(
        color = EmblemGoldDark,
        topLeft = Offset(censerX - (canvasWidth * 0.025f), censerY + (canvasWidth * 0.028f)),
        size = Size(canvasWidth * 0.050f, canvasWidth * 0.012f)
    )
}

/**
 * Sacred Liturgical Deacon Stole (Batrasheel) on Right
 * Rich crimson red velvet with embroidered golden Coptic crosses and bottom fringes.
 */
private fun DrawScope.drawDeaconStole(center: Offset, canvasWidth: Float) {
    val stoleX = center.x + (canvasWidth * 0.22f)
    val stoleY = center.y - (canvasWidth * 0.16f)
    val stoleWidth = canvasWidth * 0.075f
    val stoleHeight = canvasWidth * 0.32f

    // 1. Red Fabric Body
    val stoleRect = Rect(
        offset = Offset(stoleX, stoleY),
        size = Size(stoleWidth, stoleHeight)
    )
    drawRect(color = EmblemRedCrimson, topLeft = stoleRect.topLeft, size = stoleRect.size)

    // 2. Gold Borders
    drawRect(
        color = EmblemGoldBright,
        topLeft = stoleRect.topLeft,
        size = stoleRect.size,
        style = Stroke(width = canvasWidth * 0.006f)
    )

    // 3. Embroidered Gold Coptic Crosses along the Stole
    val crossYPositions = listOf(
        stoleY + (stoleHeight * 0.22f),
        stoleY + (stoleHeight * 0.50f),
        stoleY + (stoleHeight * 0.78f)
    )
    val crossArm = canvasWidth * 0.018f
    val crossMidX = stoleX + (stoleWidth / 2f)

    for (cy in crossYPositions) {
        // Vertical
        drawLine(
            color = EmblemGoldBright,
            start = Offset(crossMidX, cy - crossArm),
            end = Offset(crossMidX, cy + crossArm),
            strokeWidth = canvasWidth * 0.006f,
            cap = StrokeCap.Round
        )
        // Horizontal
        drawLine(
            color = EmblemGoldBright,
            start = Offset(crossMidX - (crossArm * 0.75f), cy),
            end = Offset(crossMidX + (crossArm * 0.75f), cy),
            strokeWidth = canvasWidth * 0.006f,
            cap = StrokeCap.Round
        )
        // Center pearl
        drawCircle(color = EmblemGoldBright, radius = canvasWidth * 0.004f, center = Offset(crossMidX, cy))
    }

    // 4. Golden Fringe Tassels at Bottom
    val fringeCount = 7
    val fringeY = stoleY + stoleHeight
    val step = stoleWidth / (fringeCount - 1)
    for (i in 0 until fringeCount) {
        val fx = stoleX + (i * step)
        drawLine(
            color = EmblemGold,
            start = Offset(fx, fringeY),
            end = Offset(fx, fringeY + (canvasWidth * 0.024f)),
            strokeWidth = canvasWidth * 0.004f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Open Holy Gospel Book at Center Base
 * Left Page: Coptic Cross
 * Right Page: AGIO / ΔΙΑΚ / ONOC
 */
private fun DrawScope.drawOpenHolyGospel(center: Offset, canvasWidth: Float) {
    val bookCenter = Offset(center.x, center.y + (canvasWidth * 0.16f))
    val bookWidth = canvasWidth * 0.36f
    val bookHeight = canvasWidth * 0.20f
    val halfW = bookWidth / 2f

    // 1. Gilded Book Cover Base Shadow
    val coverPath = Path().apply {
        moveTo(bookCenter.x - halfW - (canvasWidth * 0.015f), bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.02f))
        quadraticTo(bookCenter.x, bookCenter.y + (canvasWidth * 0.02f), bookCenter.x + halfW + (canvasWidth * 0.015f), bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.02f))
        lineTo(bookCenter.x + halfW + (canvasWidth * 0.015f), bookCenter.y + (bookHeight / 2f) + (canvasWidth * 0.015f))
        quadraticTo(bookCenter.x, bookCenter.y + (bookHeight / 2f) + (canvasWidth * 0.045f), bookCenter.x - halfW - (canvasWidth * 0.015f), bookCenter.y + (bookHeight / 2f) + (canvasWidth * 0.015f))
        close()
    }
    drawPath(coverPath, color = EmblemGoldDark)

    // 2. Open Pages: Left Page and Right Page
    val leftPage = Path().apply {
        moveTo(bookCenter.x - halfW, bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.01f))
        quadraticTo(bookCenter.x - (halfW / 2f), bookCenter.y - (bookHeight / 2f) - (canvasWidth * 0.012f), bookCenter.x, bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.015f))
        lineTo(bookCenter.x, bookCenter.y + (bookHeight / 2f))
        quadraticTo(bookCenter.x - (halfW / 2f), bookCenter.y + (bookHeight / 2f) - (canvasWidth * 0.02f), bookCenter.x - halfW, bookCenter.y + (bookHeight / 2f) + (canvasWidth * 0.01f))
        close()
    }
    drawPath(leftPage, color = EmblemIvory)
    drawPath(leftPage, color = EmblemGoldAmber, style = Stroke(canvasWidth * 0.005f))

    val rightPage = Path().apply {
        moveTo(bookCenter.x, bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.015f))
        quadraticTo(bookCenter.x + (halfW / 2f), bookCenter.y - (bookHeight / 2f) - (canvasWidth * 0.012f), bookCenter.x + halfW, bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.01f))
        lineTo(bookCenter.x + halfW, bookCenter.y + (bookHeight / 2f) + (canvasWidth * 0.01f))
        quadraticTo(bookCenter.x + (halfW / 2f), bookCenter.y + (bookHeight / 2f) - (canvasWidth * 0.02f), bookCenter.x, bookCenter.y + (bookHeight / 2f))
        close()
    }
    drawPath(rightPage, color = EmblemIvory)
    drawPath(rightPage, color = EmblemGoldAmber, style = Stroke(canvasWidth * 0.005f))

    // 3. Central Spine Line
    drawLine(
        color = EmblemGoldDark,
        start = Offset(bookCenter.x, bookCenter.y - (bookHeight / 2f) + (canvasWidth * 0.015f)),
        end = Offset(bookCenter.x, bookCenter.y + (bookHeight / 2f)),
        strokeWidth = canvasWidth * 0.007f
    )

    // 4. Left Page: Gilded Red Coptic Cross
    val leftCrossCenterX = bookCenter.x - (halfW * 0.52f)
    val leftCrossCenterY = bookCenter.y
    val lCrossArm = canvasWidth * 0.038f
    drawLine(
        color = EmblemRedCrimson,
        start = Offset(leftCrossCenterX, leftCrossCenterY - lCrossArm),
        end = Offset(leftCrossCenterX, leftCrossCenterY + lCrossArm),
        strokeWidth = canvasWidth * 0.008f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = EmblemRedCrimson,
        start = Offset(leftCrossCenterX - (lCrossArm * 0.65f), leftCrossCenterY),
        end = Offset(leftCrossCenterX + (lCrossArm * 0.65f), leftCrossCenterY),
        strokeWidth = canvasWidth * 0.008f,
        cap = StrokeCap.Round
    )
    drawCircle(color = EmblemGoldBright, radius = canvasWidth * 0.006f, center = Offset(leftCrossCenterX, leftCrossCenterY))

    // 5. Right Page: Text "AGIO / ΔΙΑΚ / ONOC"
    drawContext.canvas.nativeCanvas.apply {
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#4A0E17")
            textSize = canvasWidth * 0.032f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val rightTextCenterX = bookCenter.x + (halfW * 0.52f)
        drawText("AGIO", rightTextCenterX, bookCenter.y - (canvasWidth * 0.035f), textPaint)
        drawText("ΔΙΑΚ", rightTextCenterX, bookCenter.y + (canvasWidth * 0.005f), textPaint)
        drawText("ONOC", rightTextCenterX, bookCenter.y + (canvasWidth * 0.045f), textPaint)
    }
}

/**
 * Golden Coptic Cymbals (الدف والصاجات) at Lower Right
 */
private fun DrawScope.drawCopticCymbals(center: Offset, canvasWidth: Float) {
    val cymbalCenter = Offset(center.x + (canvasWidth * 0.23f), center.y + (canvasWidth * 0.16f))
    val cymbalRadius = canvasWidth * 0.052f

    // Upper cymbal disc
    drawCircle(color = EmblemGoldBright, radius = cymbalRadius, center = cymbalCenter)
    drawCircle(color = EmblemGoldDark, radius = cymbalRadius, center = cymbalCenter, style = Stroke(canvasWidth * 0.005f))
    drawCircle(color = EmblemGoldAmber, radius = cymbalRadius * 0.45f, center = cymbalCenter)

    // Lower paired cymbal behind
    val cymbal2Center = Offset(cymbalCenter.x - (canvasWidth * 0.024f), cymbalCenter.y + (canvasWidth * 0.018f))
    drawCircle(color = EmblemGold, radius = cymbalRadius * 0.85f, center = cymbal2Center)
    drawCircle(color = EmblemGoldDark, radius = cymbalRadius * 0.85f, center = cymbal2Center, style = Stroke(canvasWidth * 0.004f))

    // Wooden grip handles
    val handleStart = cymbalCenter
    val handleEnd = Offset(cymbalCenter.x + (canvasWidth * 0.045f), cymbalCenter.y - (canvasWidth * 0.032f))
    drawLine(
        color = EmblemRedRuby,
        start = handleStart,
        end = handleEnd,
        strokeWidth = canvasWidth * 0.010f,
        cap = StrokeCap.Round
    )
}

/**
 * Complete Service Brand Header with the Official Emblem & Official Inscriptions
 */
@Composable
fun ServiceBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 104.dp,
    textColor: Color = Color.White
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SaintStephenLogoEmblem(size = logoSize)

        Spacer(modifier = Modifier.height(12.dp))

        // Coptic Inscription Banner from Logo
        Text(
            text = "ⲠⲈⲚⲞⲒⲤ ⲚⲈⲘ ⲚⲈⲚⲈϤⲆⲒⲀⲔⲞⲚⲞⲤ",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmblemGoldBright,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Primary Arabic Service Title
        Text(
            text = "إدارة خدمة الشمامسة",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Secondary Arabic & English Subtitle
        Text(
            text = "الكنيسة القبطية الأرثوذكسية",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = EmblemGold,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Coptic Orthodox Deacons Service Management",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Normal,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
