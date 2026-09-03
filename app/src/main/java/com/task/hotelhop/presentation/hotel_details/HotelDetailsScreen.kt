package com.task.hotelhop.presentation.hotel_details

import android.content.Intent
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.task.hotelhop.R
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.design_system.component.HotelHopButton
import com.task.hotelhop.presentation.design_system.component.HotelHopEmptyState
import com.task.hotelhop.presentation.design_system.component.HotelHopOutlinedButton
import com.task.hotelhop.presentation.design_system.component.HotelHopSnackbarHost
import com.task.hotelhop.presentation.design_system.component.LoginRequiredDialog
import com.task.hotelhop.presentation.design_system.component.UnfavoriteConfirmDialog
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun HotelDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: HotelDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val colors = HotelHopTheme.colors

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            HotelDetailsUiEffect.NavigateBack -> onNavigateBack()
            is HotelDetailsUiEffect.NavigateToCheckout -> onNavigateToCheckout(effect.hotelId)
            HotelDetailsUiEffect.NavigateToLogin -> onNavigateToLogin()
            is HotelDetailsUiEffect.OpenMap -> {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, effect.locationUrl.toUri()))
                }.onFailure {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_map_unavailable))
                }
            }
            is HotelDetailsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            uiState.hotel != null -> {
                HotelDetailsContent(
                    hotel = uiState.hotel!!,
                    onBack = { viewModel.onEvent(HotelDetailsUiEvent.BackClicked) },
                    onFavorite = { viewModel.onEvent(HotelDetailsUiEvent.FavoriteToggled) },
                    onViewMap = { viewModel.onEvent(HotelDetailsUiEvent.ViewOnMapClicked) },
                    onBook = { viewModel.onEvent(HotelDetailsUiEvent.BookClicked) }
                )
            }
            uiState.isOfflineEmpty -> {
                HotelHopEmptyState(
                    icon = Icons.Outlined.WifiOff,
                    title = stringResource(R.string.home_offline_title),
                    body = stringResource(R.string.home_offline_body),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { viewModel.onEvent(HotelDetailsUiEvent.Retry) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        HotelHopSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    LoginRequiredDialog(
        visible = uiState.showLoginRequired,
        onLogin = { viewModel.onEvent(HotelDetailsUiEvent.LoginRequiredConfirmed) },
        onDismiss = { viewModel.onEvent(HotelDetailsUiEvent.LoginRequiredDismissed) }
    )
    UnfavoriteConfirmDialog(
        visible = uiState.showUnfavoriteConfirm,
        onConfirm = { viewModel.onEvent(HotelDetailsUiEvent.UnfavoriteConfirmed) },
        onDismiss = { viewModel.onEvent(HotelDetailsUiEvent.UnfavoriteDismissed) }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HotelDetailsContent(
    hotel: Hotel,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onViewMap: () -> Unit,
    onBook: () -> Unit
) {
    val colors = HotelHopTheme.colors
    val gallery = (listOf(hotel.mainImage) + hotel.images).filter { it.isNotBlank() }.distinct()
    val pagerState = rememberPagerState(pageCount = { gallery.size.coerceAtLeast(1) })
    val description = remember(hotel.description) {
        Html.fromHtml(hotel.description, Html.FROM_HTML_MODE_COMPACT).toString().trim()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(gallery.getOrNull(page))
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(
                                R.string.details_image_pager,
                                page + 1,
                                gallery.size.coerceAtLeast(1)
                            ),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 12.dp, end = 12.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(shape = CircleShape, color = colors.surfaceLow.copy(alpha = 0.9f)) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.content_desc_back),
                                    tint = colors.textTitle
                                )
                            }
                        }
                        Surface(shape = CircleShape, color = colors.surfaceLow.copy(alpha = 0.9f)) {
                            IconButton(onClick = onFavorite) {
                                Icon(
                                    imageVector = if (hotel.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(
                                        if (hotel.isFavorite) R.string.content_desc_unfavorite else R.string.content_desc_favorite
                                    ),
                                    tint = if (hotel.isFavorite) colors.error else colors.textBody
                                )
                            }
                        }
                    }
                    if (gallery.size > 1) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colors.textTitle.copy(alpha = 0.62f)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.details_image_counter,
                                        pagerState.currentPage + 1,
                                        gallery.size
                                    ),
                                    style = HotelHopTheme.typography.labelMedium,
                                    color = colors.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                repeat(gallery.size.coerceAtMost(8)) { index ->
                                    val selected = index == pagerState.currentPage
                                    Surface(
                                        modifier = Modifier.size(if (selected) 8.dp else 6.dp),
                                        shape = CircleShape,
                                        color = if (selected) colors.onPrimary else colors.onPrimary.copy(alpha = 0.45f)
                                    ) {}
                                }
                            }
                        }
                    }
                }
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = hotel.name,
                        style = HotelHopTheme.typography.headlineSmall,
                        color = colors.textTitle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hotel.city,
                        style = HotelHopTheme.typography.bodyMedium,
                        color = colors.textBody
                    )
                    if (hotel.locationDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hotel.locationDetails,
                            style = HotelHopTheme.typography.bodySmall,
                            color = colors.textHint
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.star_rating, hotel.rating),
                            style = HotelHopTheme.typography.labelLarge,
                            color = colors.textTitle
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.price_per_night, hotel.pricePerNight),
                            style = HotelHopTheme.typography.titleSmall,
                            color = colors.primary
                        )
                    }
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.details_about),
                            style = HotelHopTheme.typography.titleSmall,
                            color = colors.textTitle
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = HotelHopTheme.typography.bodyMedium,
                            color = colors.textBody
                        )
                    }
                    if (hotel.amenities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.details_amenities),
                            style = HotelHopTheme.typography.titleSmall,
                            color = colors.textTitle
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            hotel.amenities.forEach { amenity ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = colors.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = amenity,
                                        style = HotelHopTheme.typography.labelSmall,
                                        color = colors.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HotelHopOutlinedButton(
                        text = stringResource(R.string.details_view_on_map),
                        onClick = onViewMap
                    )
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
        Surface(
            color = colors.surfaceLow,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                HotelHopButton(
                    text = stringResource(R.string.details_book),
                    onClick = onBook
                )
            }
        }
    }
}
