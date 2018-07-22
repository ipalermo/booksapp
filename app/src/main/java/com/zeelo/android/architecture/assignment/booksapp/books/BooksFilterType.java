
package com.zeelo.android.architecture.assignment.booksapp.books;

/**
 * Used with the filter spinner in the books list.
 */
public enum BooksFilterType {
    /**
     * Do not filter books.
     */
    ALL_TASKS,

    /**
     * Filters only the active (not favorited yet) books.
     */
    ACTIVE_TASKS,

    /**
     * Filters only the favorited books.
     */
    COMPLETED_TASKS
}
