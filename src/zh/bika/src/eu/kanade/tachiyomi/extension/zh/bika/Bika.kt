package eu.kanade.tachiyomi.extension.zh.bika

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
    override val supportsLatest = false
    override val name = "Bika"
    override val baseUrl = "https://picaapi.picacomic.com"
    var category: String? = null

    override fun popularMangaRequest(page: Int): Request {
        return BikaApi.getInstance().pageRequest(category, page)
    }

    override val client: OkHttpClient
        get() {
            BikaApi.getInstance().initClient()
            return BikaApi.getInstance().client
        }

    override fun popularMangaParse(response: Response): MangasPage {
        return ExtHelper.popularMangaParse(response)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        return ExtHelper.mangaDetailsParse(response)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return ExtHelper.chapterListParse(response)
    }

    override fun chapterListRequest(manga: SManga): Request {
        val id = manga.url.substringAfterLast("/")
        return chapterListRequestPaginated(id, 1)
    }

    override fun latestUpdatesRequest(page: Int) = throw UnsupportedOperationException("Not used")

    override fun latestUpdatesParse(response: Response): MangasPage = throw UnsupportedOperationException("Not used")

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        throw UnsupportedOperationException("Not used")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        throw UnsupportedOperationException("Not used")
    }

    private fun chapterListRequestPaginated(id: String, page: Int): Request {
        return BikaApi.getInstance().chapterRequest(id, page)
    }

    override fun pageListParse(response: Response): List<Page> {
        return ExtHelper.pageListParse(response)
    }

    // Unused, we can get image urls directly from the chapter page
    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException("This method should not be called!")

    override fun getFilterList() = FilterList(
        SortFilter(),
        GenreGroup(),
        StatusFilter(),
        TypeFilter(),
        ReaderFilter()
    )

    private class GenreGroup : UriPartFilter("分类", arrayOf(
        Pair("全部", ""),
        Pair("冒险", "4"),
        Pair("百合", "3243"),
        Pair("生活", "3242"),
        Pair("四格", "17"),
        Pair("伪娘", "3244"),
        Pair("悬疑", "3245"),
        Pair("后宫", "3249"),
        Pair("热血", "3248"),
        Pair("耽美", "3246"),
        Pair("其他", "16"),
        Pair("恐怖", "14"),
        Pair("科幻", "7"),
        Pair("格斗", "6"),
        Pair("欢乐向", "5"),
        Pair("爱情", "8"),
        Pair("侦探", "9"),
        Pair("校园", "13"),
        Pair("神鬼", "12"),
        Pair("魔法", "11"),
        Pair("竞技", "10"),
        Pair("历史", "3250"),
        Pair("战争", "3251"),
        Pair("魔幻", "5806"),
        Pair("扶她", "5345"),
        Pair("东方", "5077"),
        Pair("奇幻", "5848"),
        Pair("轻小说", "6316"),
        Pair("仙侠", "7900"),
        Pair("搞笑", "7568"),
        Pair("颜艺", "6437"),
        Pair("性转换", "4518"),
        Pair("高清单行", "4459"),
        Pair("治愈", "3254"),
        Pair("宅系", "3253"),
        Pair("萌系", "3252"),
        Pair("励志", "3255"),
        Pair("节操", "6219"),
        Pair("职场", "3328"),
        Pair("西方魔幻", "3365"),
        Pair("音乐舞蹈", "3326"),
        Pair("机战", "3325")
    ))

    private class StatusFilter : UriPartFilter("连载状态", arrayOf(
        Pair("全部", ""),
        Pair("连载", "2309"),
        Pair("完结", "2310")
    ))

    private class TypeFilter : UriPartFilter("地区", arrayOf(
        Pair("全部", ""),
        Pair("日本", "2304"),
        Pair("韩国", "2305"),
        Pair("欧美", "2306"),
        Pair("港台", "2307"),
        Pair("内地", "2308"),
        Pair("其他", "8453")
    ))

    private class SortFilter : UriPartFilter("排序", arrayOf(
        Pair("人气", "0"),
        Pair("更新", "1")
    ))

    private class ReaderFilter : UriPartFilter("读者", arrayOf(
        Pair("全部", ""),
        Pair("少年", "3262"),
        Pair("少女", "3263"),
        Pair("青年", "3264")
    ))

    private open class UriPartFilter(displayName: String, val vals: Array<Pair<String, String>>,
                                     defaultValue: Int = 0) :
        Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray(), defaultValue) {
        open fun toUriPart() = vals[state].second
    }
}
