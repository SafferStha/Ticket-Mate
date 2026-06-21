package com.example.individual_project.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.viewmodel.PaymentMethod
import com.example.individual_project.ui.viewmodel.PaymentViewModel
import com.example.individual_project.utils.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController : NavController,
    viewModel     : PaymentViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    // Navigate to PaymentSuccess on successful payment
    LaunchedEffect(uiState.paymentId) {
        uiState.paymentId?.let { paymentId ->
            navController.navigate(Screen.PaymentSuccess.createRoute(paymentId)) {
                popUpTo(Screen.Checkout.route) { inclusive = true }
            }
            viewModel.resetPaymentResult()
        }
    }

    // Navigate to PaymentFailure on failure
    LaunchedEffect(uiState.paymentFailed) {
        if (uiState.paymentFailed) {
            val bookingId = uiState.booking?.id ?: ""
            navController.navigate(Screen.PaymentFailure.createRoute(bookingId)) {
                popUpTo(Screen.Checkout.route) { inclusive = false }
            }
            viewModel.resetPaymentResult()
        }
    }

    // Show errors in snackbar (non-fatal)
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = TmNavyBlue,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            if (uiState.booking != null && uiState.breakdown != null) {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text  = "Total",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text       = PriceFormatter.format(uiState.breakdown!!.total),
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = TmGold
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Button(
                            onClick  = { viewModel.processPayment() },
                            enabled  = !uiState.isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape  = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                        ) {
                            if (uiState.isProcessing) {
                                CircularProgressIndicator(
                                    modifier    = Modifier
                                        .size(Spacing.iconMd)
                                        .padding(end = Spacing.xs),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text("Processing…", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Text("Pay Now", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoadingBooking -> LoadingView()
                uiState.bookingError != null -> ErrorView(
                    message = uiState.bookingError!!,
                    onRetry = { viewModel.loadBooking() }
                )
                uiState.booking != null && uiState.breakdown != null -> {
                    val booking   = uiState.booking!!
                    val breakdown = uiState.breakdown!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // ── Event summary ──────────────────────────────────────────
                        CheckoutSection(title = "Event Summary") {
                            CheckoutRow("Event",    booking.eventTitle)
                            CheckoutRow("Venue",    booking.venue)
                            CheckoutRow("Date",     booking.date)
                            CheckoutRow("Quantity", "${booking.quantity} ticket${if (booking.quantity > 1) "s" else ""}")
                            CheckoutRow("Per ticket", PriceFormatter.format(booking.pricePerTicket))
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // ── Price breakdown ────────────────────────────────────────
                        CheckoutSection(title = "Price Breakdown") {
                            CheckoutRow("Subtotal",    PriceFormatter.format(breakdown.subtotal))
                            CheckoutRow("Tax (13%)",   PriceFormatter.format(breakdown.tax))
                            CheckoutRow("Service Fee", PriceFormatter.format(breakdown.serviceFee))
                            if (breakdown.discount > 0) {
                                CheckoutRow(
                                    label      = "Discount",
                                    value      = "-${PriceFormatter.format(breakdown.discount)}",
                                    valueColor = TmSuccess
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                            CheckoutRow(
                                label      = "Grand Total",
                                value      = PriceFormatter.format(breakdown.total),
                                valueColor = TmGold,
                                bold       = true
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // ── Payment method ─────────────────────────────────────────
                        CheckoutSection(title = "Payment Method") {
                            PaymentMethod.entries.forEach { method ->
                                PaymentMethodCard(
                                    method     = method,
                                    isSelected = uiState.selectedMethod == method.key,
                                    onClick    = { viewModel.selectPaymentMethod(method) }
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutSection(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier      = Modifier.fillMaxWidth(),
        color         = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            content()
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun CheckoutRow(
    label      : String,
    value      : String,
    valueColor : Color   = Color.Unspecified,
    bold       : Boolean = false
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            color      = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PaymentMethodCard(
    method     : PaymentMethod,
    isSelected : Boolean,
    onClick    : () -> Unit
) {
    val borderColor = if (isSelected) TmBlue else MaterialTheme.colorScheme.outlineVariant
    val bgColor     = if (isSelected) TmBlue.copy(alpha = 0.06f) else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(bgColor),
        color         = Color.Transparent,
        shape         = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            Box(
                modifier         = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 0.dp else 2.dp,
                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .background(if (isSelected) TmBlue else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Text(
                text       = method.label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (isSelected) TmBlue else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
