
package com.zeelo.android.architecture.assignment.booksapp.data.source.local;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class BooksDaoTest {

    private static final Book BOOK = new Book("title", "link", "id", true);

    private BooksDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                BooksDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertBookAndGetById() {
        // When inserting a book
        mDatabase.bookDao().insertBook(BOOK);

        // When getting the book by id from the database
        Book loaded = mDatabase.bookDao().getBookById(BOOK.getId());

        // The loaded data contains the expected values
        assertBook(loaded, "id", "title", "link", true);
    }

    @Test
    public void insertBookReplacesOnConflict() {
        //Given that a book is inserted
        mDatabase.bookDao().insertBook(BOOK);

        // When a book with the same id is inserted
        Book newBook = new Book("title2", "description2", "id", true);
        mDatabase.bookDao().insertBook(newBook);
        // When getting the book by id from the database
        Book loaded = mDatabase.bookDao().getBookById(BOOK.getId());

        // The loaded data contains the expected values
        assertBook(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void insertBookAndGetBooks() {
        // When inserting a book
        mDatabase.bookDao().insertBook(BOOK);

        // When getting the books from the database
        List<Book> books = mDatabase.bookDao().getBooks();

        // There is only 1 book in the database
        assertThat(books.size(), is(1));
        // The loaded data contains the expected values
        assertBook(books.get(0), "id", "title", "link", true);
    }

    @Test
    public void updateBookAndGetById() {
        // When inserting a book
        mDatabase.bookDao().insertBook(BOOK);

        // When the book is updated
        Book updatedBook = new Book("title2", "description2", "id", true);
        mDatabase.bookDao().updateBook(updatedBook);

        // When getting the book by id from the database
        Book loaded = mDatabase.bookDao().getBookById("id");

        // The loaded data contains the expected values
        assertBook(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void updateCompletedAndGetById() {
        // When inserting a book
        mDatabase.bookDao().insertBook(BOOK);

        // When the book is updated
        mDatabase.bookDao().updateCompleted(BOOK.getId(), false);

        // When getting the book by id from the database
        Book loaded = mDatabase.bookDao().getBookById("id");

        // The loaded data contains the expected values
        assertBook(loaded, BOOK.getId(), BOOK.getTitle(), BOOK.getDescription(), false);
    }

    @Test
    public void deleteBookByIdAndGettingBooks() {
        //Given a book inserted
        mDatabase.bookDao().insertBook(BOOK);

        //When deleting a book by id
        mDatabase.bookDao().deleteBookById(BOOK.getId());

        //When getting the books
        List<Book> books = mDatabase.bookDao().getBooks();
        // The list is empty
        assertThat(books.size(), is(0));
    }

    @Test
    public void deleteBooksAndGettingBooks() {
        //Given a book inserted
        mDatabase.bookDao().insertBook(BOOK);

        //When deleting all books
        mDatabase.bookDao().deleteBooks();

        //When getting the books
        List<Book> books = mDatabase.bookDao().getBooks();
        // The list is empty
        assertThat(books.size(), is(0));
    }

    @Test
    public void deleteCompletedBooksAndGettingBooks() {
        //Given a favorited book inserted
        mDatabase.bookDao().insertBook(BOOK);

        //When deleting favorited books
        mDatabase.bookDao().deleteCompletedBooks();

        //When getting the books
        List<Book> books = mDatabase.bookDao().getBooks();
        // The list is empty
        assertThat(books.size(), is(0));
    }

    private void assertBook(Book book, String id, String title,
                            String description, boolean completed) {
        assertThat(book, notNullValue());
        assertThat(book.getId(), is(id));
        assertThat(book.getTitle(), is(title));
        assertThat(book.getDescription(), is(description));
        assertThat(book.isCompleted(), is(completed));
    }
}
