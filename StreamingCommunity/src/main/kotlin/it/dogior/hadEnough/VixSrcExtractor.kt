package it.dogior.hadEnough

import android.util.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

class VixSrcExtractor : ExtractorApi() {
    override val mainUrl = "vixsrc.to"
    override val name = "VixSrc"
    override val requiresReferer = true  // 🔧 CAMBIATO: richiede referer!
    val TAG = "VixSrcExtractor"
    private var referer: String? = null

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        this.referer = referer
        Log.d(TAG, "REFERER: $referer  URL: $url")
        
        // 🔧 CONTROLLO REFERER
        if (referer == null) {
            Log.e(TAG, "Referer è null! VixSrc richiede referer.")
            return
        }
        
        try {
            val playlistUrl = getPlaylistLink(url, referer)
            Log.w(TAG, "FINAL URL: $playlistUrl")

            // 🔧 DETERMINA TIPO LINK
            val linkType = if (playlistUrl.contains(".m3u8")) {
                ExtractorLinkType.M3U8
            } else {
                ExtractorLinkType.VIDEO
            }

            callback.invoke(
                newExtractorLink(
                    source = "VixSrc",
                    name = "Streaming Community - VixSrc",
                    url = playlistUrl,
                    type = linkType
                ) {
                    this.referer = referer
                    // 🔧 IMPORTANTE per download M3U8
                    this.isM3u8 = playlistUrl.contains(".m3u8")
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Errore VixSrcExtractor: ${e.message}", e)
        }
    }

    private suspend fun getPlaylistLink(url: String, referer: String): String {
        Log.d(TAG, "Item url: $url")

        val script = getScript(url, referer)
        val masterPlaylist = script.getJSONObject("masterPlaylist")
        val masterPlaylistParams = masterPlaylist.getJSONObject("params")
        val token = masterPlaylistParams.getString("token")
        val expires = masterPlaylistParams.getString("expires")
        val playlistUrl = masterPlaylist.getString("url")

        var masterPlaylistUrl: String
        val params = "token=${token}&expires=${expires}"
        masterPlaylistUrl = if ("?b" in playlistUrl) {
            "${playlistUrl.replace("?b:1", "?b=1")}&$params"
        } else {
            "${playlistUrl}?$params"
        }
        Log.d(TAG, "masterPlaylistUrl: $masterPlaylistUrl")

        if (script.getBoolean("canPlayFHD")) {
            masterPlaylistUrl += "&h=1"
        }

        Log.d(TAG, "Master Playlist URL: $masterPlaylistUrl")
        return masterPlaylistUrl
    }

    private suspend fun getScript(url: String, referer: String): JSONObject {
        Log.d(TAG, "Item url: $url")
        
        val headers = mutableMapOf(
            "Accept" to "*/*",
            "Alt-Used" to url.toHttpUrl().host,
            "Connection" to "keep-alive",
            "Host" to url.toHttpUrl().host,
            "Referer" to referer,  // 🔧 ORA SICURO NON NULL
            "Sec-Fetch-Dest" to "iframe",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-Site" to "cross-site",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/133.0",
        )

        val resp = app.get(url, headers = headers).document

        val scripts = resp.select("script")
        val script =
            scripts.find { it.data().contains("masterPlaylist") }!!.data().replace("\n", "\t")

        val scriptJson = getSanitisedScript(script)
        Log.d(TAG, "Script Json: $scriptJson")
        return JSONObject(scriptJson)
    }

    private fun getSanitisedScript(script: String): String {
        // Split by top-level assignments like window.xxx =
        val parts = Regex("""window\.(\w+)\s*=""")
            .split(script)
            .drop(1) // first split part is empty before first assignment

        val keys = Regex("""window\.(\w+)\s*=""")
            .findAll(script)
            .map { it.groupValues[1] }
            .toList()

        val jsonObjects = keys.zip(parts).map { (key, value) ->
            // Clean up the value
            val cleaned = value
                .replace(";", "")
                // Quote keys only inside objects
                .replace(Regex("""(\{|\[|,)\s*(\w+)\s*:"""), "$1 \"$2\":")
                // Remove trailing commas before } or ]
                .replace(Regex(""",(\s*[}\]])"""), "$1")
                .trim()

            "\"$key\": $cleaned"
        }
        val finalObject =
            "{\n${jsonObjects.joinToString(",\n")}\n}"
                .replace("'", "\"")

        return finalObject
    }
}
