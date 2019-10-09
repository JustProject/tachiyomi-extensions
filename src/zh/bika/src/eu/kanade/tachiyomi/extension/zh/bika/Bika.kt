package eu.kanade.tachiyomi.extension.zh.dmzj

import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response

class Bika : HttpSource() {
    override val baseUrl: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val lang: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val name: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val supportsLatest: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun chapterListParse(response: Response): List<SChapter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun imageUrlParse(response: Response): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun latestUpdatesRequest(page: Int): Request {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mangaDetailsParse(response: Response): SManga {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pageListParse(response: Response): List<Page> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun popularMangaParse(response: Response): MangasPage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun popularMangaRequest(page: Int): Request {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchMangaParse(response: Response): MangasPage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
