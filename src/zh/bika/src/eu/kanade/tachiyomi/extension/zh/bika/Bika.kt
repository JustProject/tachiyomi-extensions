package eu.kanade.tachiyomi.extension.zh.bika

import com.google.gson.Gson
import com.lfkdsk.bika.BikaApi
import com.lfkdsk.bika.request.SignInBody
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response


open class Bika : HttpSource() {
    override val baseUrl: String = "https://picaapi.picacomic.com/"
    override val lang: String = "zh"
    override val name: String = "BiKa"
    override val supportsLatest: Boolean = false

    var token: String? = ""
    var category: String? = null
    val gson: Gson = Gson()

    init {
//        val response = RestWakaClient().apiService.wakaInit.execute()
        val body = SignInBody("lfkdsk", "lfk2014ws")
        BikaApi.getInstance().initClient()
        val signIn = BikaApi.getInstance()
            .api
            .signIn(body).execute()
        token = signIn.body()?.data?.token
        BikaApi.getInstance().initImage(token)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return mutableListOf()
    }

    override fun imageUrlParse(response: Response): String {
        return ""
    }

    override fun mangaDetailsParse(response: Response): SManga {
        return SManga.create()
    }

    override fun pageListParse(response: Response): List<Page> {
        return mutableListOf()
    }

    override fun popularMangaParse(response: Response): MangasPage {
        return Helper.popularMangaParse(response)
    }

    override fun popularMangaRequest(page: Int): Request {
        return BikaApi.getInstance().pageRequest(category, page)
    }

    // useless now.
    override fun searchMangaParse(response: Response): MangasPage {
        return MangasPage(mutableListOf(), false)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return Request.Builder().build()
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        return MangasPage(mutableListOf(), false)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return Request.Builder().build()
    }
}
