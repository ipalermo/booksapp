
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import android.support.annotation.NonNull;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;

import java.util.List;

/**
 * Main entry point for accessing books data.
 */
public interface BooksDataSource {

    interface LoadBooksCallback {

        void onBooksLoaded(List<Book> books);

        void onDataNotAvailable();
    }

    interface GetBookCallback {

        void onBookLoaded(Book book);

        void onDataNotAvailable();
    }

    void getBooks(@NonNull LoadBooksCallback callback);

    void getBook(@NonNull String bookId, @NonNull GetBookCallback callback);

    void saveBook(@NonNull Book book);
    
    void refreshBook();

    void deleteAllBooks();

    void deleteBook(@NonNull String bookId);
}
