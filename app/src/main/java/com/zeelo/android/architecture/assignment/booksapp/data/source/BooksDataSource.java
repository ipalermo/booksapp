
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import android.support.annotation.NonNull;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;

import java.util.List;

/**
 * Main entry point for accessing books data.
 */
public interface BooksDataSource {

    interface LoadBooksListCallback {

        void onBooksListLoaded(List<BookListItem> books);

        void onDataNotAvailable();
    }

    interface GetBookDetailsCallback {

        void onBookDetailsLoaded(Book book);

        void onDataNotAvailable();
    }

    void getBooks(@NonNull LoadBooksListCallback callback);

    void getBookDetails(@NonNull String bookId, @NonNull GetBookDetailsCallback callback);

    void saveBook(@NonNull Book book);
    
    void refreshBook();

    void deleteAllBooks();

    void deleteBook(@NonNull String bookId);
}
