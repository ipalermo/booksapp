
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import com.google.common.collect.Lists;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class BooksRepositoryTest {

    private final static String BOOK_ID = "id1";

    private final static String BOOK_TITLE = "title";

    private final static String BOOK_TITLE2 = "title2";

    private final static String BOOK_TITLE3 = "title3";

    private static List<BookListItem> BOOKS = Lists.newArrayList(new BookListItem("Title1", "Id1"),
            new BookListItem("Title2", "Id2"));

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
        Book newBook = new Book(BOOK_TITLE, "Some Book Description");

        // When a book is saved to the books repository
        mBooksRepository.saveBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).saveBook(newBook);
        verify(mBooksLocalDataSource).saveBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedListItems.size(), is(1));
    }

    @Test
    public void favoriteBook_favoritesBookToServiceAPIUpdatesCache() {
        // Given a stub not favorite book with title and description added in the repository
        Book newBook = new Book(BOOK_TITLE, "Some Book Description");
        mBooksRepository.saveBook(newBook);

        // When a book is favorite on the books repository
        mBooksRepository.favoriteBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).favoriteBook(newBook);
        verify(mBooksLocalDataSource).favoriteBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isFavorite(), is(true));
    }

    @Test
    public void unfavoriteBook_unfavoritesBookToServiceAPIUpdatesCache() {
        // Given a stub favorite book with title and description in the repository
        Book newBook = new Book(BOOK_TITLE, "Some Book Description");
        newBook.setFavorite(true);
        mBooksRepository.saveBook(newBook);

        // When a favorite book is set as not favorite in the books repository
        mBooksRepository.unFavoriteBook(newBook);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).unFavoriteBook(newBook);
        verify(mBooksLocalDataSource).unFavoriteBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isFavorite(), is(false));
    }

    @Test
    public void unfavoriteBookId_unfavoritesBookToServiceAPIUpdatesCache() {
        // Given a stub favorite book with title and description in the repository
        Book newBook = new Book(BOOK_TITLE, "Some Book Description");
        mBooksRepository.saveBook(newBook);

        // When a favorite book is set as not favorite in the books repository
        mBooksRepository.unFavoriteBook(newBook.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mBooksRemoteDataSource).unFavoriteBook(newBook);
        verify(mBooksLocalDataSource).unFavoriteBook(newBook);
        assertThat(mBooksRepository.mCachedBooks.size(), is(1));
        assertThat(mBooksRepository.mCachedBooks.get(newBook.getId()).isFavorite(), is(false));
    }

    @Test
    public void getBook_requestsSingleBookFromLocalDataSource() {
        // When a book is requested from the books repository
        mBooksRepository.getBookDetails(BOOK_ID, mGetBookDetailsCallback);

        // Then the book is loaded from the database
        verify(mBooksLocalDataSource).getBookDetails(eq(BOOK_ID), any(
                BooksDataSource.GetBookDetailsCallback.class));
    }

    @Test
    public void deleteAllBooks_deleteBooksToServiceAPIUpdatesCache() {
        // Given 2 stub favorite books and 1 stub not favorite books in the repository
        Book newBook = new Book(BOOK_TITLE, "1", "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook);
        Book newBook2 = new Book(BOOK_TITLE2, "2", "Some BookListItem Description", true);
        mBooksRepository.saveBook(newBook2);
        Book newBook3 = new Book(BOOK_TITLE3, "3", "Some BookListItem Description", false);
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
        Book newBook = new Book(BOOK_TITLE, "1", "Some Book Description", true);
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
        mBooksRepository.refreshBooks();
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
        mBooksRepository.refreshBooks();

        // When calling getBooks in the repository
        mBooksRepository.getBooks(mLoadBooksListCallback);

        // Make the remote data source return data
        setBooksAvailable(mBooksRemoteDataSource, BOOKS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mBooksLocalDataSource, times(BOOKS.size())).saveBook(any(Book.class));
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

    private void setBookAvailable(BooksDataSource dataSource, Book book) {
        verify(dataSource).getBookDetails(eq(book.getId()), mBookCallbackCaptor.capture());
        mBookCallbackCaptor.getValue().onBookDetailsLoaded(book);
    }
}
