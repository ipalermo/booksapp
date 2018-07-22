
package com.zeelo.android.architecture.assignment.booksapp.books;


import com.zeelo.android.architecture.assignment.booksapp.data.Book;

/**
 * Listener used with data binding to process user actions.
 */
public interface BookItemUserActionsListener {
    void onBookClicked(Book book);
}
