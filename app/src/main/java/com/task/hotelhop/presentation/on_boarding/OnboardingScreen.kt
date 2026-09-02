package com.task.hotelhop.presentation.on_boarding

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.hotelhop.R
import com.task.hotelhop.presentation.design_system.component.HotelHopButton
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.util.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            OnboardingUiEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(OnboardingUiEvent.PageChanged(pagerState.currentPage))
    }

    val colors = HotelHopTheme.colors
    val isLastPage = uiState.currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val item = onboardingPages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(item.titleRes),
                    style = HotelHopTheme.typography.headlineMedium,
                    color = colors.textTitle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(item.bodyRes),
                    style = HotelHopTheme.typography.bodyMedium,
                    color = colors.textBody,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            onboardingPages.forEachIndexed { index, _ ->
                val indicatorColor by animateColorAsState(
                    targetValue = if (index == uiState.currentPage) colors.primary else colors.disable,
                    label = "onboardingIndicator"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == uiState.currentPage) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }

        HotelHopButton(
            text = stringResource(if (isLastPage) R.string.onboarding_get_started else R.string.next),
            onClick = {
                if (isLastPage) {
                    viewModel.onEvent(OnboardingUiEvent.GetStartedClicked)
                } else {
                    viewModel.onEvent(OnboardingUiEvent.NextClicked)
                }
            }
        )
    }
}
