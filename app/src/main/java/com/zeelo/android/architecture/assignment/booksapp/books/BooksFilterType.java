
package com.zeelo.android.architecture.assignment.booksapp.books;

/**
 * Used with the filter spinner in the books list.
 */
public enum BooksFilterType {
    /**
     * Do not filter books.
     */
    ALL_BOOKS,

    /**
     * Filters only the active (not favorited yet) books.
     */
    NOT_FAVORITED_BOOKS,

    /**
     * Filters only the favorited books.
     */
    FAVORITED_BOOKS
}
