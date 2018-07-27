
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

    void saveBooksListItems(@NonNull List<BookListItem> booksListItems);

    void getBookDetails(@NonNull String bookId, @NonNull GetBookDetailsCallback callback);

    void saveBook(@NonNull Book book);

    void favoriteBook(@NonNull Book book);

    void favoriteBook(@NonNull String bookId);

    void unFavoriteBook(@NonNull Book book);

    void unFavoriteBook(@NonNull String bookId);
    
    void refreshBooks();

    void deleteAllBooks();

    void deleteBook(@NonNull String bookId);
}
