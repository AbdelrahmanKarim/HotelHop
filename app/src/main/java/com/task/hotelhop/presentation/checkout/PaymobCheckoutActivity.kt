package com.task.hotelhop.presentation.checkout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class PaymobCheckoutActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var resultDelivered = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val checkoutUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL).orEmpty()
        if (checkoutUrl.isBlank()) {
            finishWithResult(success = false)
            return
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return handleRedirect(request?.url?.toString().orEmpty())
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return handleRedirect(url.orEmpty())
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    handleRedirect(url.orEmpty())
                }
            }
            loadUrl(checkoutUrl)
        }
        setContentView(webView)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finishWithResult(success = false)
            }
        })
    }

    private fun handleRedirect(url: String): Boolean {
        if (resultDelivered || url.isBlank()) return false
        val isAppReturn = url.startsWith("hotelhop://") ||
            url.contains("hotelhop.app/paymob/result", ignoreCase = true)
        if (!isAppReturn) return false
        val success = url.contains("success=true", ignoreCase = true) ||
            url.contains("txn_response_code=APPROVED", ignoreCase = true)
        finishWithResult(success)
        return true
    }

    private fun finishWithResult(success: Boolean) {
        if (resultDelivered) return
        resultDelivered = true
        setResult(
            if (success) RESULT_OK else RESULT_CANCELED,
            Intent().putExtra(EXTRA_SUCCESS, success)
        )
        finish()
    }

    companion object {
        const val EXTRA_CHECKOUT_URL = "checkout_url"
        const val EXTRA_SUCCESS = "payment_success"
    }
}
