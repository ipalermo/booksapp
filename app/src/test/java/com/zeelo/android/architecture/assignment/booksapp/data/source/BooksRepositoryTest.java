
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import com.google.common.collect.Lists;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;

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

    private static List<BookListItem> BOOKS = Lists.newArrayList(new BookListItem("Title1", "Description1"),
            new BookListItem("Title2", "Description2"));

    private BooksRepository mBooksRepository;

    @Mock
    private BooksDataSource mBooksRemoteDataSource;

    @Mock
    private BooksDataSource mBooksLocalDataSource;

    @Mock
    private BooksDataSource.GetBookDetailsCallback mGetBookDetailsCallback;

    @Mock
    private BooksDataSource.LoadBooksListCallback mLoadBooksListCallback;

    @Captor
    private ArgumentCaptor<BooksDataSource.LoadBooksListCallback> mBooksCallbackCaptor;

    @Captor
    private ArgumentCaptor<BooksDataSource.GetBookDetailsCallback> mBookCallbackCaptor;

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
        twoBooksLoadCallsToRepository(mLoadBooksListCallback);

        // Then books were only requested once from Service API
        verify(mBooksRemoteDataSource).getBooks(any(BooksDataSource.LoadBooksListCallback.class));
    }

    @Test
    public void getBooks_requestsAllBooksFromLocalDataSource() {
        // When books are requested from the books repository
        mBooksRepository.getBooks(mLoadBooksListCallback);

        // Then books are loaded from the local data source
        verify(mBooksLocalDataSource).getBooks(any(BooksDataSource.LoadBooksListCallback.class));
    }

    @Test
    public void saveBook_savesBookToServiceAPI() {
        // Given a stub book with title and link
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description");

        // When a book is saved to the books repository
        mBooksRepository.saveBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).saveBook(newBook);
        verify(mBooksLocalDataSource).saveBook(newBook);
        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
    }

    @Test
    public void completeBook_completesBookToServiceAPIUpdatesCache() {
        // Given a stub active book with title and link added in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description");
        mBooksRepository.saveBook(newBook);

        // When a book is favorited to the books repository
        mBooksRepository.completeBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).completeBook(newBook);
        verify(mBooksLocalDataSource).completeBook(newBook);
        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
        assertThat(mBooksRepository.mCachedListItems.get(newBook.getId()).isActive(), is(false));
    }

    @Test
    public void completeBookId_completesBookToServiceAPIUpdatesCache() {
        // Given a stub active book with title and link added in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description");
        mBooksRepository.saveBook(newBook);

        // When a book is favorited using its id to the books repository
        mBooksRepository.completeBook(newBook.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).completeBook(newBook);
        verify(mBooksLocalDataSource).completeBook(newBook);
        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
        assertThat(mBooksRepository.mCachedListItems.get(newBook.getId()).isActive(), is(false));
    }

    @Test
    public void activateBook_activatesBookToServiceAPIUpdatesCache() {
        // Given a stub favorited book with title and link in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook);

        // When a favorited book is activated to the books repository
        mBooksRepository.activateBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).activateBook(newBook);
        verify(mBooksLocalDataSource).activateBook(newBook);
        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
        assertThat(mBooksRepository.mCachedListItems.get(newBook.getId()).isActive(), is(true));
    }

    @Test
    public void activateBookId_activatesBookToServiceAPIUpdatesCache() {
        // Given a stub favorited book with title and link in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook);

        // When a favorited book is activated with its id to the books repository
        mBooksRepository.activateBook(newBook.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).activateBook(newBook);
        verify(mBooksLocalDataSource).activateBook(newBook);
        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
        assertThat(mBooksRepository.mCachedListItems.get(newBook.getId()).isActive(), is(true));
    }

    @Test
    public void getBook_requestsSingleBookFromLocalDataSource() {
        // When a book is requested from the books repository
        mBooksRepository.getBookDetails(TASK_TITLE, mGetBookDetailsCallback);

        // Then the book is loaded from the database
        verify(mBooksLocalDataSource).getBookDetails(eq(TASK_TITLE), any(
                BooksDataSource.GetBookDetailsCallback.class));
    }

    @Test
    public void deleteCompletedBooks_deleteCompletedBooksToServiceAPIUpdatesCache() {
        // Given 2 stub favorited books and 1 stub active books in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook);
        BookListItem newBook2 = new BookListItem(TASK_TITLE2, "Some BookListItem Description");
        mBooksRepository.saveBook(newBook2);
        BookListItem newBook3 = new BookListItem(TASK_TITLE3, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook3);

        // When a favorited books are cleared to the books repository
        mBooksRepository.clearCompletedBooks();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).clearCompletedBooks();
        verify(mBooksLocalDataSource).clearCompletedBooks();

        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
        assertTrue(mBooksRepository.mCachedListItems.get(newBook2.getId()).isActive());
        assertThat(mBooksRepository.mCachedListItems.get(newBook2.getId()).getTitle(), is(TASK_TITLE2));
    }

    @Test
    public void deleteAllBooks_deleteBooksToServiceAPIUpdatesCache() {
        // Given 2 stub favorited books and 1 stub active books in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook);
        BookListItem newBook2 = new BookListItem(TASK_TITLE2, "Some BookListItem Description");
        mBooksRepository.saveBook(newBook2);
        BookListItem newBook3 = new BookListItem(TASK_TITLE3, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook3);

        // When all books are deleted to the books repository
        mBooksRepository.deleteAllBooks();

        // Verify the data sources were called
        verify(mBooksRemoteDataSource).deleteAllBooks();
        verify(mBooksLocalDataSource).deleteAllBooks();

        assertThat(mBooksRepository.mCachedListItems.size(), is(0));
    }

    @Test
    public void deleteBook_deleteBookToServiceAPIRemovedFromCache() {
        // Given a book in the repository
        BookListItem newBook = new BookListItem(TASK_TITLE, "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook);
        assertThat(mBooksRepository.mCachedListItems.containsKey(newBook.getId()), is(true));

        // When deleted
        mBooksRepository.deleteBook(newBook.getId());

        // Verify the data sources were called
        verify(mBooksRemoteDataSource).deleteBook(newBook.getId());
        verify(mBooksLocalDataSource).deleteBook(newBook.getId());

        // Verify it's removed from repository
        assertThat(mBooksRepository.mCachedListItems.containsKey(newBook.getId()), is(false));
    }

    @Test
    public void getBooksWithDirtyCache_booksAreRetrievedFromRemote() {
        // When calling getBooks in the repository with dirty cache
        mBooksRepository.refreshBook();
        mBooksRepository.getBooks(mLoadBooksListCallback);

        // And the remote data source has data available
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify the books from the remote data source are returned, not the local
        verify(mBooksLocalDataSource, never()).getBooks(mLoadBooksListCallback);
        verify(mLoadBooksListCallback).onBooksListLoaded(BOOKS);
    }

    @Test
    public void getBooksWithLocalDataSourceUnavailable_booksAreRetrievedFromRemote() {
        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksListCallback);

        // And the local data source has no data available
        setBooksNotAvailable(mBooksLocalDataSource);

        // And the remote data source has data available
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify the books from the local data source are returned
        verify(mLoadBooksListCallback).onBooksListLoaded(BOOKS);
    }

    @Test
    public void getBooksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksListCallback);

        // And the local data source has no data available
        setBooksNotAvailable(mBooksLocalDataSource);

        // And the remote data source has no data available
        setBooksNotAvailable(mBooksRemoteDataSource);

        // Verify no data is returned
        verify(mLoadBooksListCallback).onDataNotAvailable();
    }

    @Test
    public void getBookWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a book id
        final String bookId = "123";

        // When calling getBookDetails in the repository
        mBooksRepository.getBookDetails(bookId, mGetBookDetailsCallback);

        // And the local data source has no data available
        setBookNotAvailable(mBooksLocalDataSource, bookId);

        // And the remote data source has no data available
        setBookNotAvailable(mBooksRemoteDataSource, bookId);

        // Verify no data is returned
        verify(mGetBookDetailsCallback).onDataNotAvailable();
    }

    @Test
    public void getBooks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mBooksRepository.refreshBook();

        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksListCallback);

        // Make the remote data source return data
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mBooksLocalDataSource, times(BOOKS.size())).saveBook(any(BookListItem.class));
    }

    /**
     * Convenience method that issues two calls to the books repository
     */
    private void twoBooksLoadCallsToRepository(BooksDataSource.LoadBooksListCallback callback) {
        // When books are requested from repository
        mBooksRepository.getBooks(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mBooksLocalDataSource).getBooks(mBooksCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mBooksCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mBooksRemoteDataSource).getBooks(mBooksCallbackCaptor.capture());

        // Trigger callback so books are cached
        mBooksCallbackCaptor.getValue().onBooksListLoaded(BOOKS);

        mBooksRepository.getBooks(callback); // Second call to API
    }

    private void setBooksNotAvailable(BooksDataSource dataSource) {
        verify(dataSource).getBooks(mBooksCallbackCaptor.capture());
        mBooksCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setBooksAvailable(BooksDataSource dataSource, List<BookListItem> books) {
        verify(dataSource).getBooks(mBooksCallbackCaptor.capture());
        mBooksCallbackCaptor.getValue().onBooksListLoaded(books);
    }

    private void setBookNotAvailable(BooksDataSource dataSource, String bookId) {
        verify(dataSource).getBookDetails(eq(bookId), mBookCallbackCaptor.capture());
        mBookCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setBookAvailable(BooksDataSource dataSource, BookListItem book) {
        verify(dataSource).getBookDetails(eq(book.getId()), mBookCallbackCaptor.capture());
        mBookCallbackCaptor.getValue().onBookDetailsLoaded(book);
    }
}
