
package com.zeelo.android.architecture.assignment.booksapp.bookdetail;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.content.res.Resources;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.SnackbarMessage;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link BookDetailViewModel}
 */
public class BookDetailViewModelTest {

    // Executes each book synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static final String TITLE_TEST = "title";

    private static final String DESCRIPTION_TEST = "link";

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    @Mock
    private BooksRepository mBooksRepository;

    @Mock
    private Application mContext;

    @Mock
    private BooksDataSource.GetBookDetailsCallback mRepositoryCallback;

    @Mock
    private BooksDataSource.GetBookDetailsCallback mViewModelCallback;

    @Captor
    private ArgumentCaptor<BooksDataSource.GetBookDetailsCallback> mGetBookCallbackCaptor;

    private BookDetailViewModel mBookDetailViewModel;

    private Book mBook;

    @Before
    public void setupBooksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        mBook = new Book(TITLE_TEST, DESCRIPTION_TEST);

        // Get a reference to the class under test
        mBookDetailViewModel = new BookDetailViewModel(mContext, mBooksRepository);
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);
        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void getNotFavoriteBookFromRepositoryAndLoadIntoView() {
        setupViewModelRepositoryCallback();

        // Then verify that the view was notified
        assertEquals(mBookDetailViewModel.book.get().getTitle(), mBook.getTitle());
        assertEquals(mBookDetailViewModel.book.get().getVolumeInfo().getDescription(), mBook.getVolumeInfo().getDescription());
    }

    @Test
    public void deleteBook() {
        setupViewModelRepositoryCallback();

        // When the deletion of a book is requested
        mBookDetailViewModel.deleteBook();

        // Then the repository is notified
        verify(mBooksRepository).deleteBook(mBook.getId());
    }

    @Test
    public void favoriteBook() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to favorite the book
        mBookDetailViewModel.setFavorited(true);

        // Then a request is sent to the book repository and the UI is updated
        verify(mBooksRepository).favoriteBook(mBook);
        assertThat(mBookDetailViewModel.getSnackbarMessage().getValue(),
                is(R.string.book_marked_favorite));
    }

    @Test
    public void activateBook() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to set book as not favorite
        mBookDetailViewModel.setFavorited(false);

        // Then a request is sent to the book repository and the UI is updated
        verify(mBooksRepository).unFavoriteBook(mBook);
        assertThat(mBookDetailViewModel.getSnackbarMessage().getValue(),
                is(R.string.book_removed_favorite));
    }

    @Test
    public void BookDetailViewModel_repositoryError() {
        // Given an initialized ViewModel with an active book
        mViewModelCallback = mock(BooksDataSource.GetBookDetailsCallback.class);

        mBookDetailViewModel.start(mBook.getId());

        // Use a captor to get a reference for the callback.
        verify(mBooksRepository).getBookDetails(eq(mBook.getId()), mGetBookCallbackCaptor.capture());

        // When the repository returns an error
        mGetBookCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mBookDetailViewModel.isDataAvailable());
    }

    @Test
    public void BookDetailViewModel_repositoryNull() {
        setupViewModelRepositoryCallback();

        // When the repository returns a null book
        mGetBookCallbackCaptor.getValue().onBookDetailsLoaded(null); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mBookDetailViewModel.isDataAvailable());

        // Then book detail UI is shown
        assertThat(mBookDetailViewModel.book.get(), is(nullValue()));
    }

    private void setupViewModelRepositoryCallback() {
        // Given an initialized ViewModel with an active book
        mViewModelCallback = mock(BooksDataSource.GetBookDetailsCallback.class);

        mBookDetailViewModel.start(mBook.getId());

        // Use a captor to get a reference for the callback.
        verify(mBooksRepository).getBookDetails(eq(mBook.getId()), mGetBookCallbackCaptor.capture());

        mGetBookCallbackCaptor.getValue().onBookDetailsLoaded(mBook); // Trigger callback
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        SnackbarMessage snackbarText = mBookDetailViewModel.getSnackbarMessage();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText.getValue(), is(nullValue()));
    }
}
