package eu.kanade.tachiyomi.extension.zh.bika;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.lfkdsk.bika.BikaApi;
import com.lfkdsk.bika.response.ComicDetail;
import com.lfkdsk.bika.response.ComicDetailResponse;
import com.lfkdsk.bika.response.ComicEpisodeResponse;
import com.lfkdsk.bika.response.ComicListResponse;
import com.lfkdsk.bika.response.ComicPage;
import com.lfkdsk.bika.response.GeneralResponse;
import com.lfkdsk.bika.response.ThumbnailObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import eu.kanade.tachiyomi.source.model.MangasPage;
import eu.kanade.tachiyomi.source.model.SChapter;
import eu.kanade.tachiyomi.source.model.SManga;
import okhttp3.Response;

import static java.util.stream.Collectors.toList;

public final class Helper {
    public final static Type comiclist = getBaseResponseType(ComicListResponse.class);
    public final static Type comicdetail = getBaseResponseType(ComicDetailResponse.class);
    public final static Type chapterdetail = getBaseResponseType(ComicEpisodeResponse.class);
    public final static Gson gson = new Gson();

    public static MangasPage popularMangaParse(Response response) throws IOException {
        GeneralResponse<ComicListResponse> castedResponse = parseGen(response.body() != null ? response.body().string() : "", comiclist);
        if (castedResponse == null) {
            return null;
        }

        ComicListResponse body = castedResponse.data;
        if (body == null || body.getComics() == null) {
            return null;
        }

        ComicPage data = body.getComics();
        List<SManga> sMangas = data.getDocs().stream().map(comic -> {
            SManga sManga = SManga.Companion.create();
            sManga.setAuthor(comic.author);
            sManga.setThumbnail_url(getThumbnailImagePath(comic.thumb));
            sManga.setStatus(comic.finished ? SManga.COMPLETED : SManga.ONGOING);
            sManga.setGenre(String.join(", ", comic.categories));
            sManga.setUrl("comics/" + comic.comicId);
            sManga.setTitle(comic.title);
            return sManga;
        }).collect(toList());

        return new MangasPage(sMangas, data.getPage() < data.getPages());
    }

    public static SManga mangaDetailsParse(Response response) throws IOException {
        GeneralResponse<ComicDetailResponse> castedResponse = parseGen(response.body() != null ? response.body().string() : "", comicdetail);
        if (castedResponse == null) {
            return null;
        }

        ComicDetailResponse body = castedResponse.data;
        if (body == null || body.getComic() == null) {
            return null;
        }

        ComicDetail detail = body.getComic();
        SManga sManga = SManga.Companion.create();
        sManga.setTitle(detail.getTitle());
//        sManga.setUrl(); detail no need
        sManga.setGenre(String.join(", ", detail.getCategories()));
        sManga.setStatus(detail.isFinished() ? SManga.COMPLETED : SManga.ONGOING);
        sManga.setThumbnail_url(getThumbnailImagePath(detail.getThumb()));
        sManga.setAuthor(detail.getAuthor());
        sManga.setDescription(detail.getDescription());
        sManga.setInitialized(true);
        return sManga;
    }

    public static List<SChapter> chapterListParse(Response response) throws IOException {
        GeneralResponse<ComicEpisodeResponse> castedResponse = parseGen(response.body() != null ? response.body().string() : "", chapterdetail);
        if (castedResponse == null) {
            return null;
        }

        ComicEpisodeResponse body = castedResponse.data;
        if (body == null || body.getEps() == null) {
            return null;
        }


    }

    private static <T> T parseGen(String json, Type type) {
        return gson.fromJson(json, type);
    }

    private static <T> Type getBaseResponseType(Class<T> innerType) {
        return getParameterized(GeneralResponse.class, innerType);
    }

    private static Type getParameterized(Type rawType, Type... typeArguments) {
        return $Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments);
    }


    public static String getThumbnailImagePath(ThumbnailObject thumbnailObject) {
        if (thumbnailObject == null) {
            return null;
        }
        if (thumbnailObject.fileServer.equalsIgnoreCase("http://lorempixel.com")) {
            return thumbnailObject.fileServer + thumbnailObject.path;
        }
        if (BikaApi.getInstance().getImageServer() != null) {
            return thumbnailObject.fileServer + "/static/" + thumbnailObject.path;
        }
        return BikaApi.getInstance().getImageServer() + thumbnailObject.path;
    }
}
