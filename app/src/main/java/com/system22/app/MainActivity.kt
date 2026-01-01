package com.system22.app

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * النشاط الرئيسي لتطبيق SYSTEM 22
 * يعرض محتوى الويب باستخدام WebView مع تأثير شاشة البداية
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var progressDialog: ProgressDialog? = null
    private val splashDuration = 2000L // مدة شاشة البداية: ثانيتان

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تأخير قصير لإظهار شاشة البداية
        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.activity_main)
            initializeWebView()
        }, splashDuration)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        // تهيئة WebView
        webView = findViewById(R.id.webView)

        // إعدادات WebView المتقدمة
        val webSettings: WebSettings = webView.settings

        // تفعيل JavaScript للتفاعل مع React
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        
        // إعدادات العرض والتحميل
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.loadsImagesAutomatically = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        // إعدادات التكبير
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // إعدادات الأمان والوصول
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true

        // تعيين عميل WebView مخصص
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()

                // إذا كان الرابط خارجي، افتحه في المتصفح الخارجي
                if (url.startsWith("http://") || url.startsWith("https://") &&
                    !url.contains("unpkg.com") &&
                    !url.contains("cdn.tailwindcss.com")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }

                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressDialog?.dismiss()
            }
        }

        // عميل Chrome لتحميل الملفات بشكل أفضل
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    if (progressDialog == null) {
                        progressDialog = ProgressDialog(this@MainActivity).apply {
                            setMessage("جاري تحميل النظام...")
                            setCancelable(false)
                            show()
                        }
                    }
                    progressDialog?.progress = newProgress
                } else {
                    progressDialog?.dismiss()
                    progressDialog = null
                }
            }
        }

        // تحميل ملف HTML
        webView.loadUrl("file:///android_asset/index.html")
    }

    /**
     * معالجة زر الرجوع في Android
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * حفظ حالة WebView عند تدوير الشاشة
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::webView.isInitialized) {
            webView.saveState(outState)
        }
    }
}
