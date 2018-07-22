
package com.zeelo.android.architecture.assignment.booksapp.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class BooksRemoteDataSource implements BooksDataSource {

    private static BooksRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 2000;

    private final static Map<String, Book> TASKS_SERVICE_DATA;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addBook("Build tower in Pisa", "Ground looks good, no foundation work required.", "0");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "1");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "2");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "3");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "4");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "5");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "6");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "7");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "8");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "12");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "13");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "14");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "15");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "16");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "17");
        addBook("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "18");
    }

    public static BooksRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BooksRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private BooksRemoteDataSource() {}

    private static void addBook(String title, String description, String id) {
        Book newBook = new Book(title, description, id);
        TASKS_SERVICE_DATA.put(newBook.getId(), newBook);
    }

    /**
     * Note: {@link LoadBooksCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getBooks(final @NonNull LoadBooksCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onBooksLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    /**
     * Note: {@link GetBookCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getBook(@NonNull String bookId, final @NonNull GetBookCallback callback) {
        final Book book = TASKS_SERVICE_DATA.get(bookId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onBookLoaded(book);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveBook(@NonNull Book book) {
        TASKS_SERVICE_DATA.put(book.getId(), book);
    }


    @Override
    public void refreshBook() {
        // Not required because the {@link BooksRepository} handles the logic of refreshing the
        // books from all the available data sources.
    }

    @Override
    public void deleteAllBooks() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteBook(@NonNull String bookId) {
        TASKS_SERVICE_DATA.remove(bookId);
    }
}
