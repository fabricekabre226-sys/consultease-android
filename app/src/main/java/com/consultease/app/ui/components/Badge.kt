package com.consultease.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.consultease.app.ui.theme.AccentTint
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.Danger
import com.consultease.app.ui.theme.DangerTint
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.Sand
import com.consultease.app.ui.theme.Success
import com.consultease.app.ui.theme.SuccessTint

enum class BadgeStyle { SUCCESS, WARN, DANGER, MUTED }

@Composable
fun StatusBadge(text: String, style: BadgeStyle, modifier: Modifier = Modifier) {
    val (bg, fg) = when (style) {
        BadgeStyle.SUCCESS -> SuccessTint to Success
        BadgeStyle.WARN -> AccentTint to Accent
        BadgeStyle.DANGER -> DangerTint to Danger
        BadgeStyle.MUTED -> Sand to InkSoft
    }
    Text(
        text = text,
        color = fg,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}
