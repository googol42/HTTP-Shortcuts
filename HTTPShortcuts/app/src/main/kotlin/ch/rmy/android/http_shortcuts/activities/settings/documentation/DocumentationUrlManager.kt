package ch.rmy.android.http_shortcuts.activities.settings.documentation

import android.net.Uri
import ch.rmy.android.framework.extensions.isWebUrl

object DocumentationUrlManager {

    private const val DOCUMENTATION_HOST = "http-shortcuts.rmy.ch"

    private val SUPPORTED_PATHS = setOf(
        "categories",
        "documentation",
        "execution-flow",
        "faq",
        "import-export",
        "introduction",
        "privacy-policy",
        "scripting",
        "shortcuts",
        "variables",
    )

    fun toInternalUrl(url: Uri): Uri? {
        if (!canHandle(url)) {
            return null
        }
        return url
            .buildUpon()
            .scheme("file")
            .authority("")
            .path("/android_asset/docs" + url.path.orEmpty() + ".html")
            .build()
    }

    fun toExternal(url: Uri): Uri {
        if (url.scheme == "file" && url.authority.isNullOrEmpty() && url.path?.startsWith("/android_asset/docs/") == true) {
            return url
                .buildUpon()
                .scheme("https")
                .authority(DOCUMENTATION_HOST)
                .path("/" + url.path!!.removePrefix("/android_asset/docs/").removeSuffix(".md").removeSuffix(".html"))
                .build()
        }
        return url
    }

    fun canHandle(url: Uri): Boolean {
        if (!url.isWebUrl || url.host != DOCUMENTATION_HOST) {
            return false
        }
        return url.path.orEmpty().trimStart('/') in SUPPORTED_PATHS
    }
}
