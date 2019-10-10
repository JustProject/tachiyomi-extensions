package eu.kanade.tachiyomi.extension.zh.bika;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.lfkdsk.bika.BikaApi;
import com.lfkdsk.bika.response.ComicDetail;
import com.lfkdsk.bika.response.ComicDetailResponse;
import com.lfkdsk.bika.response.ComicEpisode;
import com.lfkdsk.bika.response.ComicEpisodeData;
import com.lfkdsk.bika.response.ComicEpisodeResponse;
import com.lfkdsk.bika.response.ComicListResponse;
import com.lfkdsk.bika.response.ComicPage;
import com.lfkdsk.bika.response.ComicPageGraph;
import com.lfkdsk.bika.response.ComicPagesResponse;
import com.lfkdsk.bika.response.GeneralResponse;
import com.lfkdsk.bika.response.ThumbnailObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import eu.kanade.tachiyomi.source.model.MangasPage;
import eu.kanade.tachiyomi.source.model.Page;
import eu.kanade.tachiyomi.source.model.SChapter;
import eu.kanade.tachiyomi.source.model.SManga;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toList;

public final class ExtHelper {
    private final static Type comiclist = getBaseResponseType(ComicListResponse.class);
    private final static Type comicdetail = getBaseResponseType(ComicDetailResponse.class);
    private final static Type chapterdetail = getBaseResponseType(ComicEpisodeResponse.class);
    private final static Type pages = getBaseResponseType(ComicPagesResponse.class);
    private final static Gson gson = new Gson();

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

        final Request request = response.request();
        final String id = request.url().pathSegments().get(1);
        final List<SChapter> chapters = new ArrayList<>();

        int page = 1;
        List<ComicEpisode> result = body.getEps().getDocs();
        ComicEpisodeData epsRes = body.getEps();

        while (epsRes != null && epsRes.getPage() <= epsRes.getPages()) {
            List<SChapter> chaptersPart = result
                    .stream()
                    .map(eps -> {
                        SChapter chapter = SChapter.Companion.create();
                        chapter.setName(eps.getTitle());
                        chapter.setChapter_number(eps.getOrder());
                        chapter.setDate_upload(timeStamp2Date(eps.getUpdatedAt()));
                        chapter.setUrl("comics/" + id + "/order/" + eps.getOrder() + "/pages");
                        return chapter;
                    }).collect(Collectors.toList());
            chapters.addAll(chaptersPart);

            if (epsRes.getPage() == epsRes.getPages()) {
                epsRes = null;
            } else {
                epsRes = BikaApi.getInstance().eps(id, ++page);
            }
        }

        return chapters;
    }

    public static List<Page> pageListParse(Response response) throws IOException {
        GeneralResponse<ComicPagesResponse> castedResponse = parseGen(response.body() != null ? response.body().string() : "", pages);
        if (castedResponse == null) {
            return null;
        }

        ComicPagesResponse body = castedResponse.data;
        if (body == null || body.getPages() == null) {
            return null;
        }

        final List<ComicPageGraph> data = body.getPages().getDocs();
        return zipIndex(data.stream(), data.size())
                .map(pair -> new Page(pair.first, "", getThumbnailImagePath(pair.second.getMedia()), null))
                .collect(Collectors.toList());
    }

    private static long timeStamp2Date(String timestampString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date;
        try {
            date = format.parse(timestampString);
            return date.getTime();
        } catch (ParseException ignored) {
        }
        return 0;
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

    private static String getThumbnailImagePath(ThumbnailObject thumbnailObject) {
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

    public static <A, B, C> Stream<C> zip(Stream<? extends A> a,
                                          Stream<? extends B> b,
                                          BiFunction<? super A, ? super B, ? extends C> zipper) {
        Objects.requireNonNull(zipper);
        Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
        Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
                ~(Spliterator.DISTINCT | Spliterator.SORTED);

        long zipSize = ((characteristics & Spliterator.SIZED) != 0)
                ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
                : -1;

        Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
        Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<C> cIterator = new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(aIterator.next(), bIterator.next());
            }
        };

        Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
        return (a.isParallel() || b.isParallel())
                ? StreamSupport.stream(split, true)
                : StreamSupport.stream(split, false);
    }

    private static <T> Stream<Pair<Integer, T>> zipIndex(Stream<T> stream, int size) {
        return zip(IntStream.range(0, size).boxed(), stream, Pair::new);
    }
}
