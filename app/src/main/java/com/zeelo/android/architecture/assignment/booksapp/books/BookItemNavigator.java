
package com.zeelo.android.architecture.assignment.booksapp.books;

/**
 * Defines the navigation actions that can be called from a list item in the book list.
 */
public interface BookItemNavigator {

    void openBookDetails(String bookId);
}
