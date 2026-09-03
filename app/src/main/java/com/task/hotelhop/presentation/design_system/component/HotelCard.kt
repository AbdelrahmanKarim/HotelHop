package com.task.hotelhop.presentation.design_system.component

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.task.hotelhop.R
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme

@Composable
fun HotelCard(
    hotel: Hotel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val colors = HotelHopTheme.colors
    val imageHeight = if (compact) 120.dp else 168.dp
    Card(
        modifier = modifier
            .then(if (compact) Modifier else Modifier.fillMaxWidth())
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(hotel.mainImage.ifBlank { null })
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.content_desc_hotel_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = colors.surfaceLow.copy(alpha = 0.92f)
                ) {
                    IconButton(onClick = onFavoriteClick, modifier = Modifier.size(40.dp)) {
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
            Column(modifier = Modifier.padding(if (compact) 10.dp else 16.dp)) {
                Text(
                    text = hotel.name,
                    style = HotelHopTheme.typography.titleSmall,
                    color = colors.textTitle,
                    maxLines = if (compact) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hotel.city,
                    style = HotelHopTheme.typography.bodySmall,
                    color = colors.textBody,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))
                if (compact) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.star_rating, hotel.rating),
                            style = HotelHopTheme.typography.labelSmall,
                            color = colors.textTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.price_per_night, hotel.pricePerNight),
                        style = HotelHopTheme.typography.labelMedium,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.star_rating, hotel.rating),
                                style = HotelHopTheme.typography.labelMedium,
                                color = colors.textTitle
                            )
                        }
                        Text(
                            text = stringResource(R.string.price_per_night, hotel.pricePerNight),
                            style = HotelHopTheme.typography.labelLarge,
                            color = colors.primary
                        )
                    }
                }
            }
        }
    }
}
