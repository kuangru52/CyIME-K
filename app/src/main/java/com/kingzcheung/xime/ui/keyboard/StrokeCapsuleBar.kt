package com.kingzcheung.xime.ui.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.data.FirstStrokeManager
import com.kingzcheung.xime.ui.theme.LocalKeyboardPalette
import com.kingzcheung.xime.ui.theme.LocalMaterialPalette

@Composable
fun StrokeCapsuleBar(
    isComposing: Boolean,
    hasCandidates: Boolean,
    selectedStroke: Int?,
    onStrokeSelect: (Int) -> Unit,
    onPunctuationClick: (String) -> Unit,
    visuals: CandidateBarVisuals,
    modifier: Modifier = Modifier
) {
    val showStrokes = isComposing || hasCandidates
    val items = if (showStrokes) FirstStrokeManager.STROKE_SYMBOLS else FirstStrokeManager.DEFAULT_PUNCTUATIONS

    val palette = LocalKeyboardPalette.current
    val materialPalette = LocalMaterialPalette.current

    val defaultBg = palette?.function
        ?: palette?.key
        ?: materialPalette?.surface
        ?: if (visuals.isDarkTheme) Color(0xFF333333) else Color(0xFFC9CED1)

    val activeBg = visuals.accentColor.takeUnless { it == Color.Unspecified }
        ?: palette?.accent
        ?: Color(0xFF00A884)

    val defaultTextColor = palette?.functionText
        ?: palette?.text
        ?: if (visuals.isDarkTheme) Color(0xFFF5D061) else Color(0xFF37474F)

    val activeTextColor = if (visuals.isDarkTheme) Color.Black else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, label ->
            val strokeCode = index + 1
            val isSelected = showStrokes && selectedStroke == strokeCode

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) activeBg else defaultBg,
                animationSpec = tween(150),
                label = "capsuleBg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) activeTextColor else defaultTextColor,
                animationSpec = tween(150),
                label = "capsuleText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .then(
                        if (!isSelected) {
                            Modifier.border(
                                width = 0.5.dp,
                                color = defaultTextColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50)
                            )
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (showStrokes) {
                            onStrokeSelect(strokeCode)
                        } else {
                            onPunctuationClick(label)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}
