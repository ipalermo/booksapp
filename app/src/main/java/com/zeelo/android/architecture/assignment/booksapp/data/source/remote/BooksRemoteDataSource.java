
package com.zeelo.android.architecture.assignment.booksapp.data.source.remote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class BooksRemoteDataSource implements BooksDataSource {

    private static BooksRemoteDataSource INSTANCE;

    private static final Map<String, BookListItem> BOOKS_LIST_SERVICE_DATA = new LinkedHashMap<>();
    private static final Map<String, Book> BOOK_SERVICE_DATA = new LinkedHashMap<>();

    public static final String BOOK_DETAILS_API_PATH = "/api/v1/items/";

    // Prevent direct instantiation.
    private BooksRemoteDataSource(Context context) {
        String booksJson = getJSONString(context, "books.json");
        Type itemListType = new TypeToken<ArrayList<BookListItem>>(){}.getType();
        List<BookListItem> bookListItems = new Gson().fromJson(booksJson, itemListType);

        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        List<Book> books = new Gson().fromJson(booksJson, bookListType);

        for (BookListItem item : bookListItems) {
            BOOKS_LIST_SERVICE_DATA.put(item.getId(), item);
        }
        for (Book book : books) {
            BOOK_SERVICE_DATA.put(book.getId(), book);
        }
    }

    public static BooksRemoteDataSource getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new BooksRemoteDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public void getBooks(@NonNull LoadBooksListCallback callback) {
        callback.onBooksListLoaded(Lists.newArrayList(BOOKS_LIST_SERVICE_DATA.values()));
    }

    @Override
    public void getBookDetails(@NonNull String bookId, @NonNull GetBookDetailsCallback callback) {
        Book book = BOOK_SERVICE_DATA.get(bookId);
        callback.onBookDetailsLoaded(book);
    }

    @Override
    public void saveBook(@NonNull Book book) {
        BOOK_SERVICE_DATA.put(book.getId(), book);
        BOOKS_LIST_SERVICE_DATA.put(book.getId(), new BookListItem(book.getTitle(), book.getId()));
    }

    @Override
    public void saveBooksListItems(@NonNull List<BookListItem> booksListItems) {
        for (BookListItem bookListItem : booksListItems) {
            BOOKS_LIST_SERVICE_DATA.put(bookListItem.getId(), bookListItem);
        }
    }

    @Override
    public void favoriteBook(@NonNull Book book) {
        Book favoriteBook = new Book(book.getTitle(), book.getVolumeInfo().getDescription(), book.getId(), true);
        BOOK_SERVICE_DATA.put(book.getId(), favoriteBook);
    }

    @Override
    public void favoriteBook(@NonNull String bookId) {
        // Not required for the remote data source because the {@link BooksRepository} handles
        // converting from a {@code bookId} to a {@link book} using its cached data.
    }

    @Override
    public void unFavoriteBook(@NonNull Book book) {
        Book notFavoriteBook = new Book(book.getTitle(), book.getId(), book.getVolumeInfo().getDescription(), false);
        BOOK_SERVICE_DATA.put(book.getId(), notFavoriteBook);
    }

    @Override
    public void unFavoriteBook(@NonNull String bookId) {
        // Not required for the remote data source because the {@link BooksRepository} handles
        // converting from a {@code bookId} to a {@link book} using its cached data.
    }

    public void refreshBooks() {
        // Not required because the {@link BooksRepository} handles the logic of refreshing the
        // books from all the available data sources.
    }

    @Override
    public void deleteBook(@NonNull String bookId) {
        BOOK_SERVICE_DATA.remove(bookId);
        BOOKS_LIST_SERVICE_DATA.remove(bookId);
    }

    @Override
    public void deleteAllBooks() {
        BOOK_SERVICE_DATA.clear();
        BOOKS_LIST_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addBooks(Book... books) {
        if (books != null) {
            for (Book book : books) {
                BOOK_SERVICE_DATA.put(book.getId(), book);
                BOOKS_LIST_SERVICE_DATA.put(book.getId(), new BookListItem(book.getTitle(), book.getId(), BOOK_DETAILS_API_PATH + book.getId()));
            }
        }
    }

    public String getJSONString(Context context, String fileName) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }
}
