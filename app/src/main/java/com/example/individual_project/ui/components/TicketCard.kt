package com.example.individual_project.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmError

@Composable
fun TicketCard(
    emoji          : String,
    title          : String,
    details        : String,
    isUpcoming     : Boolean,
    onActionClick  : () -> Unit = {},
    onCancelClick  : (() -> Unit)? = null,
    isCancelling   : Boolean = false,
    modifier       : Modifier   = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Gradient header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isUpcoming)
                            Brush.horizontalGradient(listOf(TmDarkBlue, TmBlue))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFF374151), Color(0xFF6B7280)))
                    )
                    .padding(Spacing.cardPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(
                            text     = title,
                            style    = MaterialTheme.typography.titleSmall,
                            color    = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isUpcoming) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(TmSuccess.copy(alpha = 0.25f))
                                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                            ) {
                                Text(
                                    text       = "UPCOMING",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = TmSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Perforation divider
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    thickness = 1.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // Ticket body
            Column(modifier = Modifier.padding(Spacing.cardPadding)) {
                details.split("\n").forEach { line ->
                    Text(
                        text  = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                if (isUpcoming) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Button(
                            onClick  = onActionClick,
                            modifier = Modifier.weight(1f),
                            shape    = MaterialTheme.shapes.medium,
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.ConfirmationNumber, null,
                                modifier = Modifier.size(Spacing.iconMd)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text("View Ticket", style = MaterialTheme.typography.labelLarge)
                        }
                        if (onCancelClick != null) {
                            if (isCancelling) {
                                OutlinedButton(
                                    onClick  = {},
                                    enabled  = false,
                                    modifier = Modifier.weight(1f),
                                    shape    = MaterialTheme.shapes.medium,
                                    border   = BorderStroke(1.dp, TmError.copy(alpha = 0.5f)),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        contentColor = TmError.copy(alpha = 0.5f),
                                        disabledContentColor = TmError.copy(alpha = 0.5f)
                                    )
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color       = TmError.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text("Cancelling…", style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                OutlinedButton(
                                    onClick  = onCancelClick,
                                    modifier = Modifier.weight(1f),
                                    shape    = MaterialTheme.shapes.medium,
                                    border   = BorderStroke(1.dp, TmError),
                                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TmError)
                                ) {
                                    Text("Cancel Booking", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick  = onActionClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = MaterialTheme.shapes.medium,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            "Buy Again",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
