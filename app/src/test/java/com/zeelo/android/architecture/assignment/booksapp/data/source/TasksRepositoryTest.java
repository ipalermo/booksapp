
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import com.google.common.collect.Lists;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class BooksRepositoryTest {

    private final static String TASK_TITLE = "title";

    private final static String TASK_TITLE2 = "title2";

    private final static String TASK_TITLE3 = "title3";

    private static List<Book> BOOKS = Lists.newArrayList(new Book("Title1", "Description1"),
            new Book("Title2", "Description2"));

    private BooksRepository mBooksRepository;

    @Mock
    private BooksDataSource mBooksRemoteDataSource;

    @Mock
    private BooksDataSource mBooksLocalDataSource;

    @Mock
    private BooksDataSource.GetBookCallback mGetBookCallback;

    @Mock
    private BooksDataSource.LoadBooksCallback mLoadBooksCallback;

    @Captor
    private ArgumentCaptor<BooksDataSource.LoadBooksCallback> mBooksCallbackCaptor;

    @Captor
    private ArgumentCaptor<BooksDataSource.GetBookCallback> mBookCallbackCaptor;

    @Before
    public void setupBooksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mBooksRepository = BooksRepository.getInstance(
                mBooksRemoteDataSource, mBooksLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        BooksRepository.destroyInstance();
    }

    @Test
    public void getBooks_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the books repository
        twoBooksLoadCallsToRepository(mLoadBooksCallback);

        // Then books were only requested once from Service API
        verify(mBooksRemoteDataSource).getBooks(any(BooksDataSource.LoadBooksCallback.class));
    }

    @Test
    public void getBooks_requestsAllBooksFromLocalDataSource() {
        // When books are requested from the books repository
        mBooksRepository.getBooks(mLoadBooksCallback);

        // Then books are loaded from the local data source
        verify(mBooksLocalDataSource).getBooks(any(BooksDataSource.LoadBooksCallback.class));
    }

    @Test
    public void saveBook_savesBookToServiceAPI() {
        // Given a stub book with title and link
        Book newBook = new Book(TASK_TITLE, "Some Book Description");

        // When a book is saved to the books repository
        mBooksRepository.saveBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).saveBook(newBook);
        verify(mBooksLocalDataSource).saveBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
    }

    @Test
    public void completeBook_completesBookToServiceAPIUpdatesCache() {
        // Given a stub active book with title and link added in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description");
        mBooksRepository.saveBook(newBook);

        // When a book is favorited to the books repository
        mBooksRepository.completeBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).completeBook(newBook);
        verify(mBooksLocalDataSource).completeBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isActive(), is(false));
    }

    @Test
    public void completeBookId_completesBookToServiceAPIUpdatesCache() {
        // Given a stub active book with title and link added in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description");
        mBooksRepository.saveBook(newBook);

        // When a book is favorited using its id to the books repository
        mBooksRepository.completeBook(newBook.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).completeBook(newBook);
        verify(mBooksLocalDataSource).completeBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isActive(), is(false));
    }

    @Test
    public void activateBook_activatesBookToServiceAPIUpdatesCache() {
        // Given a stub favorited book with title and link in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description", true);
        mBooksRepository.saveBook(newBook);

        // When a favorited book is activated to the books repository
        mBooksRepository.activateBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).activateBook(newBook);
        verify(mBooksLocalDataSource).activateBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isActive(), is(true));
    }

    @Test
    public void activateBookId_activatesBookToServiceAPIUpdatesCache() {
        // Given a stub favorited book with title and link in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description", true);
        mBooksRepository.saveBook(newBook);

        // When a favorited book is activated with its id to the books repository
        mBooksRepository.activateBook(newBook.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).activateBook(newBook);
        verify(mBooksLocalDataSource).activateBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isActive(), is(true));
    }

    @Test
    public void getBook_requestsSingleBookFromLocalDataSource() {
        // When a book is requested from the books repository
        mBooksRepository.getBook(TASK_TITLE, mGetBookCallback);

        // Then the book is loaded from the database
        verify(mBooksLocalDataSource).getBook(eq(TASK_TITLE), any(
                BooksDataSource.GetBookCallback.class));
    }

    @Test
    public void deleteCompletedBooks_deleteCompletedBooksToServiceAPIUpdatesCache() {
        // Given 2 stub favorited books and 1 stub active books in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description", true);
        mBooksRepository.saveBook(newBook);
        Book newBook2 = new Book(TASK_TITLE2, "Some Book Description");
        mBooksRepository.saveBook(newBook2);
        Book newBook3 = new Book(TASK_TITLE3, "Some Book Description", true);
        mBooksRepository.saveBook(newBook3);

        // When a favorited books are cleared to the books repository
        mBooksRepository.clearCompletedBooks();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).clearCompletedBooks();
        verify(mBooksLocalDataSource).clearCompletedBooks();

        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertTrue(mBooksRepository.mCachedBooks.get(newBook2.getId()).isActive());
        assertThat(mBooksRepository.mCachedBooks.get(newBook2.getId()).getTitle(), is(TASK_TITLE2));
    }

    @Test
    public void deleteAllBooks_deleteBooksToServiceAPIUpdatesCache() {
        // Given 2 stub favorited books and 1 stub active books in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description", true);
        mBooksRepository.saveBook(newBook);
        Book newBook2 = new Book(TASK_TITLE2, "Some Book Description");
        mBooksRepository.saveBook(newBook2);
        Book newBook3 = new Book(TASK_TITLE3, "Some Book Description", true);
        mBooksRepository.saveBook(newBook3);

        // When all books are deleted to the books repository
        mBooksRepository.deleteAllBooks();

        // Verify the data sources were called
        verify(mBooksRemoteDataSource).deleteAllBooks();
        verify(mBooksLocalDataSource).deleteAllBooks();

        assertThat(mBooksRepository.mCachedBooks.size(), is(0));
    }

    @Test
    public void deleteBook_deleteBookToServiceAPIRemovedFromCache() {
        // Given a book in the repository
        Book newBook = new Book(TASK_TITLE, "Some Book Description", true);
        mBooksRepository.saveBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.containsKey(newBook.getId()), is(true));

        // When deleted
        mBooksRepository.deleteBook(newBook.getId());

        // Verify the data sources were called
        verify(mBooksRemoteDataSource).deleteBook(newBook.getId());
        verify(mBooksLocalDataSource).deleteBook(newBook.getId());

        // Verify it's removed from repository
        assertThat(mBooksRepository.mCachedBooks.containsKey(newBook.getId()), is(false));
    }

    @Test
    public void getBooksWithDirtyCache_booksAreRetrievedFromRemote() {
        // When calling getBooks in the repository with dirty cache
        mBooksRepository.refreshBook();
        mBooksRepository.getBooks(mLoadBooksCallback);

        // And the remote data source has data available
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify the books from the remote data source are returned, not the local
        verify(mBooksLocalDataSource, never()).getBooks(mLoadBooksCallback);
        verify(mLoadBooksCallback).onBooksLoaded(BOOKS);
    }

    @Test
    public void getBooksWithLocalDataSourceUnavailable_booksAreRetrievedFromRemote() {
        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksCallback);

        // And the local data source has no data available
        setBooksNotAvailable(mBooksLocalDataSource);

        // And the remote data source has data available
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify the books from the local data source are returned
        verify(mLoadBooksCallback).onBooksLoaded(BOOKS);
    }

    @Test
    public void getBooksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksCallback);

        // And the local data source has no data available
        setBooksNotAvailable(mBooksLocalDataSource);

        // And the remote data source has no data available
        setBooksNotAvailable(mBooksRemoteDataSource);

        // Verify no data is returned
        verify(mLoadBooksCallback).onDataNotAvailable();
    }

    @Test
    public void getBookWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a book id
        final String bookId = "123";

        // When calling getBook in the repository
        mBooksRepository.getBook(bookId, mGetBookCallback);

        // And the local data source has no data available
        setBookNotAvailable(mBooksLocalDataSource, bookId);

        // And the remote data source has no data available
        setBookNotAvailable(mBooksRemoteDataSource, bookId);

        // Verify no data is returned
        verify(mGetBookCallback).onDataNotAvailable();
    }

    @Test
    public void getBooks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mBooksRepository.refreshBook();

        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksCallback);

        // Make the remote data source return data
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mBooksLocalDataSource, times(BOOKS.size())).saveBook(any(Book.class));
    }

    /**
     * Convenience method that issues two calls to the books repository
     */
    private void twoBooksLoadCallsToRepository(BooksDataSource.LoadBooksCallback callback) {
        // When books are requested from repository
        mBooksRepository.getBooks(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mBooksLocalDataSource).getBooks(mBooksCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mBooksCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mBooksRemoteDataSource).getBooks(mBooksCallbackCaptor.capture());

        // Trigger callback so books are cached
        mBooksCallbackCaptor.getValue().onBooksLoaded(BOOKS);

        mBooksRepository.getBooks(callback); // Second call to API
    }

    private void setBooksNotAvailable(BooksDataSource dataSource) {
        verify(dataSource).getBooks(mBooksCallbackCaptor.capture());
        mBooksCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setBooksAvailable(BooksDataSource dataSource, List<Book> books) {
        verify(dataSource).getBooks(mBooksCallbackCaptor.capture());
        mBooksCallbackCaptor.getValue().onBooksLoaded(books);
    }

    private void setBookNotAvailable(BooksDataSource dataSource, String bookId) {
        verify(dataSource).getBook(eq(bookId), mBookCallbackCaptor.capture());
        mBookCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setBookAvailable(BooksDataSource dataSource, Book book) {
        verify(dataSource).getBook(eq(book.getId()), mBookCallbackCaptor.capture());
        mBookCallbackCaptor.getValue().onBookLoaded(book);
    }
}
