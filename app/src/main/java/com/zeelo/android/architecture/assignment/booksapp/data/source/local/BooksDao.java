
package com.zeelo.android.architecture.assignment.booksapp.data.source.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;

import java.util.List;

/**
 * Data Access Object for the books table.
 */
@Dao
public interface BooksDao {

    /**
     * Select all books from the books table.
     *
     * @return all books.
     */
    @Query("SELECT * FROM Book")
    List<Book> getBooks();

    /**
     * Select all books from the books list table.
     *
     * @return all books.
     */
    @Query("SELECT * FROM bookslist")
    List<BookListItem> getBookListItems();

    /**
     * Select a book by id.
     *
     * @param bookId the book id.
     * @return the book with bookId.
     */
    @Query("SELECT * FROM Book WHERE id = :bookId")
    Book getBookById(String bookId);

    /**
     * Insert a book in the database. If the book already exists, replace it.
     *
     * @param book the book to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBook(Book book);

    /**
     * Insert a BookListItem in the database. If the BookListItem already exists, replace it.
     *
     * @param bookListItem the book list item to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookListItem(BookListItem bookListItem);

    /**
     * Insert a list of BookListItems in the database. If a BookListItem already exists, replace it.
     *
     * @param booksListItems the book list items to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookListItems(List<BookListItem> booksListItems);

    /**
     * Update a book.
     *
     * @param book book to be updated
     * @return the number of books updated. This should always be 1.
     */
    @Update
    int updateBook(Book book);

    /**
     * Delete a book by id.
     *
     * @return the number of books deleted. This should always be 1.
     */
    @Query("DELETE FROM Book WHERE id = :bookId")
    int deleteBookById(String bookId);

    /**
     * Delete all books.
     */
    @Query("DELETE FROM Book")
    void deleteBooks();
}
