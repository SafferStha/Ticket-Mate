package com.example.individual_project.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.example.individual_project.ui.theme.Spacing

@Composable
fun PrimaryButton(
    text      : String,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
    isLoading : Boolean  = false,
    enabled   : Boolean  = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick  = { if (!isLoading) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.buttonHeight),
        enabled = enabled && !isLoading,
        shape   = MaterialTheme.shapes.medium,
        colors  = ButtonDefaults.buttonColors(
            containerColor         = MaterialTheme.colorScheme.primary,
            contentColor           = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor   = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(20.dp),
                    color     = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector       = leadingIcon,
                            contentDescription = null,
                            modifier          = Modifier.size(Spacing.iconMd)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                    }
                    Text(
                        text  = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
