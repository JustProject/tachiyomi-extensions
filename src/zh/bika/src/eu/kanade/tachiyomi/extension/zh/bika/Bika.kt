package eu.kanade.tachiyomi.extension.zh.bika

import com.google.gson.Gson
import com.lfkdsk.bika.BikaApi
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Bika source
 */
class Bika : HttpSource() {
    override val lang = "zh"
    override val supportsLatest = true
    override val name = "Bika"
    override val baseUrl = "https://picaapi.picacomic.com"

    var category: String? = null

    override val client: OkHttpClient
        get() {
            BikaApi.getInstance().initClient()
            return BikaApi.getInstance().client
        }

    override fun popularMangaRequest(page: Int): Request {
        return BikaApi.getInstance().pageRequest(category, page)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        return ExtHelper.popularMangaParse(response)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        return ExtHelper.mangaDetailsParse(response)
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        val id = manga.url.substringAfterLast("/")
        return BikaApi.getInstance().detailRequest(id)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return ExtHelper.chapterListParse(response)
    }

    override fun chapterListRequest(manga: SManga): Request {
        val id = manga.url.substringAfterLast("/")
        return chapterListRequestPaginated(id, 1)
    }

    override fun latestUpdatesRequest(page: Int) = popularMangaRequest(page)

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return if (query == "") {
            val params = filters.map {
                if (it is GenreGroup) {
                    it.name()
                } else ""
            }.first { it != "" }
            category = params
            BikaApi.getInstance().pageRequest(category, page)
        } else {
            BikaApi.getInstance().searchRequest(query, page)
        }
    }

    override fun searchMangaParse(response: Response): MangasPage {
        return ExtHelper.popularMangaParse(response)
    }

    private fun chapterListRequestPaginated(id: String, page: Int): Request {
        return BikaApi.getInstance().chapterRequest(id, page)
    }

    override fun pageListParse(response: Response): List<Page> {
        return ExtHelper.pageListParse(response)
    }

    override fun pageListRequest(chapter: SChapter): Request {
        val params = chapter.url.split("/")
        val id = params[1]
        val order: Int = params[3].toInt()
        return BikaApi.getInstance().graphRequest(id, order)
    }

    // Unused, we can get image urls directly from the chapter page
    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException("This method should not be called!")

    override fun getFilterList() = FilterList(
        GenreGroup()
    )

    private class GenreGroup : UriPartFilter("分类", ExtHelper.categories())

    private open class UriPartFilter(displayName: String, val vals: Array<Pair<String, String>>,
                                     defaultValue: Int = 0) :
        Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray(), defaultValue) {
        open fun toUriPart() = vals[state].second
        open fun name() = vals[state].first
    }
}
