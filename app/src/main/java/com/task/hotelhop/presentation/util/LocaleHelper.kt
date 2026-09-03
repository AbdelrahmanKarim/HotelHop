package com.task.hotelhop.presentation.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("Activity not found from context")
}

fun Context.findActivityOrNull(): Activity? = runCatching { findActivity() }.getOrNull()

fun Activity.applyAppLanguage(languageCode: String) {
    val requested = LocaleListCompat.forLanguageTags(languageCode)
    if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != requested.toLanguageTags()) {
        AppCompatDelegate.setApplicationLocales(requested)
    }
}
