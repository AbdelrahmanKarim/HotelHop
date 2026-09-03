package com.task.hotelhop.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> CollectEffect(flow: Flow<T>, onEffect: suspend (T) -> Unit) {
    LaunchedEffect(flow) {
        flow.collect { onEffect(it) }
    }
}
