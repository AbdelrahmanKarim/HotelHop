package com.task.hotelhop.testutil

import com.task.hotelhop.presentation.util.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

fun UiText?.requireStringResId(): Int {
    assertTrue("Expected a string resource, was $this", this is UiText.StringResource)
    return (this as UiText.StringResource).resId
}

fun UiText?.assertStringRes(resId: Int) {
    assertEquals(resId, requireStringResId())
}
