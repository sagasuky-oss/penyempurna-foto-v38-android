
package id.termax.penyempurnafoto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Base64
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import android.view.WindowManager

class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val pickFiles = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val data = result.data

        val out: Array<Uri>? =
            if (result.resultCode == RESULT_OK && data != null) {
                val clip = data.clipData

                if (clip != null) {
                    Array(clip.itemCount) { index ->
                        clip.getItemAt(index).uri
                    }
                } else {
                    data.data?.let { arrayOf(it) }
                }
            } else {
                null
            }

        fileCallback?.onReceiveValue(out)
        fileCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            0
        )

        web = WebView(this)
        setContentView(web)

        WebView.setWebContentsDebuggingEnabled(false)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = true
        }

        web.addJavascriptInterface(
            DownloadBridge(),
            "AndroidDownloads"
        )

        web.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                view: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {

                fileCallback?.onReceiveValue(null)
                fileCallback = callback

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)

                    type = params.acceptTypes
                        .firstOrNull()
                        ?.takeIf { it.isNotBlank() && it != "*/*" }
                        ?: "image/*"

                    putExtra(
                        Intent.EXTRA_ALLOW_MULTIPLE,
                        params.mode == FileChooserParams.MODE_OPEN_MULTIPLE
                    )
                }

                pickFiles.launch(intent)
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                request.deny()
            }
        }

        web.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {

                val u = request.url

                if (u.scheme == "https" &&
                    u.host != "appassets.androidplatform.net"
                ) {
                    startActivity(Intent(Intent.ACTION_VIEW, u))
                    return true
                }

                return false
            }
        }

        web.loadUrl("file:///android_asset/index.html")
    }

    inner class DownloadBridge {

        @JavascriptInterface
        fun saveDataUrl(dataUrl: String, filename: String) {
            try {
                val split = dataUrl.split(",", limit = 2)
                if (split.size != 2) return

                val bytes = Base64.decode(
                    split[1],
                    Base64.DEFAULT
                )

                val safeName = filename
                    .replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .ifBlank { "hasil-foto.jpg" }

                val mime = split[0]
                    .substringAfter("data:")
                    .substringBefore(";")
                    .ifBlank { "application/octet-stream" }

                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                    put(MediaStore.Downloads.MIME_TYPE, mime)
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )
                }

                val uri = contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                ) ?: throw Exception("Gagal membuat file")

                contentResolver.openOutputStream(uri)?.use {
                    it.write(bytes)
                }

                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Disimpan di folder Download",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal menyimpan: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
