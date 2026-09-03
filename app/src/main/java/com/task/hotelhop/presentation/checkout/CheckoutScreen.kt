package com.task.hotelhop.presentation.checkout

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.task.hotelhop.R
import com.task.hotelhop.presentation.design_system.component.HotelHopButton
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import com.task.hotelhop.presentation.util.DAY_IN_MILLIS
import com.task.hotelhop.presentation.util.startOfTodayUtc
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckoutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    val paymobLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val success = result.data?.getBooleanExtra(PaymobCheckoutActivity.EXTRA_SUCCESS, false) == true
        viewModel.onEvent(CheckoutUiEvent.CardPaymentFinished(success && result.resultCode == android.app.Activity.RESULT_OK))
    }

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            CheckoutUiEffect.NavigateBack -> onNavigateBack()
            is CheckoutUiEffect.LaunchPaymob -> {
                paymobLauncher.launch(
                    Intent(context, PaymobCheckoutActivity::class.java)
                        .putExtra(PaymobCheckoutActivity.EXTRA_CHECKOUT_URL, effect.checkoutUrl)
                )
            }
            is CheckoutUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        if (uiState.bookingReference != null) {
            BookingSuccessContent(
                reference = uiState.bookingReference!!,
                hotelName = uiState.hotel?.name.orEmpty(),
                onDone = { viewModel.onEvent(CheckoutUiEvent.DismissSuccess) }
            )
        } else if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            val hotel = uiState.hotel
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onEvent(CheckoutUiEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = colors.textTitle
                        )
                    }
                    Text(
                        text = stringResource(R.string.checkout_title),
                        style = HotelHopTheme.typography.headlineSmall,
                        color = colors.textTitle
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    if (hotel != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(hotel.mainImage.ifBlank { null })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(R.string.content_desc_hotel_image),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            Column {
                                Text(hotel.name, style = HotelHopTheme.typography.titleSmall, color = colors.textTitle)
                                Text(hotel.city, style = HotelHopTheme.typography.bodySmall, color = colors.textBody)
                                Text(
                                    text = stringResource(R.string.price_per_night, hotel.pricePerNight),
                                    style = HotelHopTheme.typography.labelLarge,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    DateField(
                        label = stringResource(R.string.checkout_check_in),
                        value = uiState.checkInMillis.toDisplayDate(),
                        onClick = { showCheckInPicker = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DateField(
                        label = stringResource(R.string.checkout_check_out),
                        value = uiState.checkOutMillis.toDisplayDate(),
                        onClick = { showCheckOutPicker = true }
                    )
                    uiState.dateError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it.asString(), color = colors.error, style = HotelHopTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.checkout_rooms),
                        style = HotelHopTheme.typography.titleSmall,
                        color = colors.textTitle
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = colors.primary.copy(alpha = 0.12f)) {
                            IconButton(onClick = { viewModel.onEvent(CheckoutUiEvent.DecrementRooms) }) {
                                Icon(
                                    Icons.Outlined.Remove,
                                    contentDescription = stringResource(R.string.content_desc_decrease_rooms),
                                    tint = colors.primary
                                )
                            }
                        }
                        Text(
                            text = uiState.roomCount.toString(),
                            style = HotelHopTheme.typography.headlineSmall,
                            color = colors.textTitle,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Surface(shape = CircleShape, color = colors.primary.copy(alpha = 0.12f)) {
                            IconButton(onClick = { viewModel.onEvent(CheckoutUiEvent.IncrementRooms) }) {
                                Icon(
                                    Icons.Outlined.Add,
                                    contentDescription = stringResource(R.string.content_desc_increase_rooms),
                                    tint = colors.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    PriceRow(stringResource(R.string.checkout_nights, uiState.nights), "")
                    PriceRow(stringResource(R.string.checkout_subtotal), stringResource(R.string.price_value, uiState.subtotal))
                    PriceRow(stringResource(R.string.checkout_vat), stringResource(R.string.price_value, uiState.vat))
                    PriceRow(stringResource(R.string.checkout_total), stringResource(R.string.price_value, uiState.total), emphasize = true)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.checkout_payment_method),
                        style = HotelHopTheme.typography.titleSmall,
                        color = colors.textTitle
                    )
                    Column(modifier = Modifier.selectableGroup()) {
                        PaymentOption(
                            title = stringResource(R.string.checkout_pay_cash),
                            selected = uiState.paymentMethod == PaymentMethod.CASH,
                            onClick = { viewModel.onEvent(CheckoutUiEvent.PaymentMethodSelected(PaymentMethod.CASH)) }
                        )
                        PaymentOption(
                            title = stringResource(R.string.checkout_pay_card),
                            selected = uiState.paymentMethod == PaymentMethod.CARD,
                            onClick = { viewModel.onEvent(CheckoutUiEvent.PaymentMethodSelected(PaymentMethod.CARD)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Surface(color = colors.surfaceLow, shadowElevation = 8.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        HotelHopButton(
                            text = stringResource(
                                if (uiState.paymentMethod == PaymentMethod.CARD) R.string.checkout_pay_card
                                else R.string.checkout_confirm
                            ),
                            onClick = { viewModel.onEvent(CheckoutUiEvent.ConfirmBooking) },
                            loading = uiState.isSubmitting
                        )
                    }
                }
            }
        }
        HotelHopSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showCheckInPicker) {
        CheckoutDatePicker(
            minDateMillis = startOfTodayUtc(),
            onDismiss = { showCheckInPicker = false },
            onConfirm = {
                viewModel.onEvent(CheckoutUiEvent.CheckInSelected(it))
                showCheckInPicker = false
            }
        )
    }
    if (showCheckOutPicker) {
        val minCheckOut = (uiState.checkInMillis?.plus(DAY_IN_MILLIS)) ?: (startOfTodayUtc() + DAY_IN_MILLIS)
        CheckoutDatePicker(
            minDateMillis = minCheckOut,
            onDismiss = { showCheckOutPicker = false },
            onConfirm = {
                viewModel.onEvent(CheckoutUiEvent.CheckOutSelected(it))
                showCheckOutPicker = false
            }
        )
    }
}

@Composable
private fun PaymentOption(title: String, selected: Boolean, onClick: () -> Unit) {
    val colors = HotelHopTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
        )
        Text(
            text = title,
            style = HotelHopTheme.typography.bodyMedium,
            color = colors.textTitle,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun DateField(label: String, value: String, onClick: () -> Unit) {
    val colors = HotelHopTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = HotelHopTheme.typography.labelSmall, color = colors.textHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value.ifBlank { stringResource(R.string.checkout_pick_date) },
                style = HotelHopTheme.typography.bodyLarge,
                color = colors.textTitle
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, emphasize: Boolean = false) {
    val colors = HotelHopTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (emphasize) HotelHopTheme.typography.titleSmall else HotelHopTheme.typography.bodyMedium,
            color = if (emphasize) colors.textTitle else colors.textBody
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = if (emphasize) HotelHopTheme.typography.titleSmall else HotelHopTheme.typography.bodyMedium,
                color = if (emphasize) colors.primary else colors.textTitle
            )
        }
    }
}

@Composable
private fun BookingSuccessContent(
    reference: String,
    hotelName: String,
    onDone: () -> Unit
) {
    val colors = HotelHopTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(88.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.checkout_success_title),
            style = HotelHopTheme.typography.headlineMedium,
            color = colors.textTitle,
            textAlign = TextAlign.Center
        )
        if (hotelName.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hotelName,
                style = HotelHopTheme.typography.titleSmall,
                color = colors.textBody,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.checkout_success_body, reference),
            style = HotelHopTheme.typography.bodyMedium,
            color = colors.textBody,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        HotelHopButton(
            text = stringResource(R.string.checkout_success_done),
            onClick = onDone
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutDatePicker(
    minDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val colors = HotelHopTheme.colors
    val selectableDates = remember(minDateMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minDateMillis
            override fun isSelectableYear(year: Int): Boolean {
                return year >= java.time.LocalDate.now().year
            }
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = minDateMillis,
        selectableDates = selectableDates
    )
    val dateColors = DatePickerDefaults.colors(
        containerColor = colors.surfaceLow,
        titleContentColor = colors.textTitle,
        headlineContentColor = colors.textTitle,
        weekdayContentColor = colors.textBody,
        navigationContentColor = colors.primary,
        yearContentColor = colors.textTitle,
        currentYearContentColor = colors.primary,
        selectedYearContainerColor = colors.primary,
        selectedYearContentColor = colors.onPrimary,
        selectedDayContainerColor = colors.primary,
        selectedDayContentColor = colors.onPrimary,
        todayContentColor = colors.primary,
        todayDateBorderColor = colors.primary,
        dayContentColor = colors.textTitle,
        disabledDayContentColor = colors.textHint
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { pickerState.selectedDateMillis?.let(onConfirm) ?: onDismiss() }) {
                Text(stringResource(R.string.ok), color = colors.primary, style = HotelHopTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.textBody, style = HotelHopTheme.typography.labelLarge)
            }
        },
        colors = dateColors
    ) {
        DatePicker(state = pickerState, colors = dateColors)
    }
}

private fun Long?.toDisplayDate(): String {
    if (this == null) return ""
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
}
