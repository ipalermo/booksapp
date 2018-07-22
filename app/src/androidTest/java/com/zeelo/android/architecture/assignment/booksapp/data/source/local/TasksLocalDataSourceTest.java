
package com.zeelo.android.architecture.assignment.booksapp.data.source.local;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration test for the {@link BooksDataSource}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BooksLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private BooksLocalDataSource mLocalDataSource;

    private BooksDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                BooksDatabase.class)
                .build();
        BooksDao booksDao = mDatabase.bookDao();

        // Make sure that we're not keeping a reference to the wrong instance.
        BooksLocalDataSource.clearInstance();
        mLocalDataSource = BooksLocalDataSource.getInstance(new SingleExecutors(), booksDao);
    }

    @After
    public void cleanUp() {
        mDatabase.close();
        BooksLocalDataSource.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveBook_retrievesBook() {
        // Given a new book
        final Book newBook = new Book(TITLE, "");

        // When saved into the persistent repository
        mLocalDataSource.saveBook(newBook);

        // Then the book can be retrieved from the persistent repository
        mLocalDataSource.getBook(newBook.getId(), new BooksDataSource.GetBookCallback() {
            @Override
            public void onBookLoaded(Book book) {
                assertThat(book, is(newBook));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeBook_retrievedBookIsComplete() {
        // Initialize mock for the callback.
        BooksDataSource.GetBookCallback callback = mock(BooksDataSource.GetBookCallback.class);
        // Given a new book in the persistent repository
        final Book newBook = new Book(TITLE, "");
        mLocalDataSource.saveBook(newBook);

        // When favorited in the persistent repository
        mLocalDataSource.completeBook(newBook);

        // Then the book can be retrieved from the persistent repository and is complete
        mLocalDataSource.getBook(newBook.getId(), new BooksDataSource.GetBookCallback() {
            @Override
            public void onBookLoaded(Book book) {
                assertThat(book, is(newBook));
                assertThat(book.isCompleted(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateBook_retrievedBookIsActive() {
        // Initialize mock for the callback.
        BooksDataSource.GetBookCallback callback = mock(BooksDataSource.GetBookCallback.class);

        // Given a new favorited book in the persistent repository
        final Book newBook = new Book(TITLE, "");
        mLocalDataSource.saveBook(newBook);
        mLocalDataSource.completeBook(newBook);

        // When activated in the persistent repository
        mLocalDataSource.activateBook(newBook);

        // Then the book can be retrieved from the persistent repository and is active
        mLocalDataSource.getBook(newBook.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onBookLoaded(newBook);

        assertThat(newBook.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedBook_bookNotRetrievable() {
        // Initialize mocks for the callbacks.
        BooksDataSource.GetBookCallback callback1 = mock(BooksDataSource.GetBookCallback.class);
        BooksDataSource.GetBookCallback callback2 = mock(BooksDataSource.GetBookCallback.class);
        BooksDataSource.GetBookCallback callback3 = mock(BooksDataSource.GetBookCallback.class);

        // Given 2 new favorited books and 1 active book in the persistent repository
        final Book newBook1 = new Book(TITLE, "");
        mLocalDataSource.saveBook(newBook1);
        mLocalDataSource.completeBook(newBook1);
        final Book newBook2 = new Book(TITLE2, "");
        mLocalDataSource.saveBook(newBook2);
        mLocalDataSource.completeBook(newBook2);
        final Book newBook3 = new Book(TITLE3, "");
        mLocalDataSource.saveBook(newBook3);

        // When favorited books are cleared in the repository
        mLocalDataSource.clearCompletedBooks();

        // Then the favorited books cannot be retrieved and the active one can
        mLocalDataSource.getBook(newBook1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onBookLoaded(newBook1);

        mLocalDataSource.getBook(newBook2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onBookLoaded(newBook2);

        mLocalDataSource.getBook(newBook3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onBookLoaded(newBook3);
    }

    @Test
    public void deleteAllBooks_emptyListOfRetrievedBook() {
        // Given a new book in the persistent repository and a mocked callback
        Book newBook = new Book(TITLE, "");
        mLocalDataSource.saveBook(newBook);
        BooksDataSource.LoadBooksCallback callback = mock(BooksDataSource.LoadBooksCallback.class);

        // When all books are deleted
        mLocalDataSource.deleteAllBooks();

        // Then the retrieved books is an empty list
        mLocalDataSource.getBooks(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onBooksLoaded(anyList());
    }

    @Test
    public void getBooks_retrieveSavedBooks() {
        // Given 2 new books in the persistent repository
        final Book newBook1 = new Book(TITLE, "");
        mLocalDataSource.saveBook(newBook1);
        final Book newBook2 = new Book(TITLE, "");
        mLocalDataSource.saveBook(newBook2);

        // Then the books can be retrieved from the persistent repository
        mLocalDataSource.getBooks(new BooksDataSource.LoadBooksCallback() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                assertNotNull(books);
                assertTrue(books.size() >= 2);

                boolean newBook1IdFound = false;
                boolean newBook2IdFound = false;
                for (Book book : books) {
                    if (book.getId().equals(newBook1.getId())) {
                        newBook1IdFound = true;
                    }
                    if (book.getId().equals(newBook2.getId())) {
                        newBook2IdFound = true;
                    }
                }
                assertTrue(newBook1IdFound);
                assertTrue(newBook2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
