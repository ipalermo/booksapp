
package com.zeelo.android.architecture.assignment.booksapp.books;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.content.res.Resources;

import com.google.common.collect.Lists;
import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.TestUtils;
import com.zeelo.android.architecture.assignment.booksapp.addeditbook.AddEditBookActivity;
import com.zeelo.android.architecture.assignment.booksapp.bookdetail.BookDetailActivity;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource.LoadBooksListCallback;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.zeelo.android.architecture.assignment.booksapp.R.string.successfully_deleted_book_message;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link BooksViewModel}
 */
public class BooksViewModelTest {

    // Executes each book synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static List<BookListItem> BOOKS;

    @Mock
    private BooksRepository mBooksRepository;

    @Mock
    private Application mContext;

    @Captor
    private ArgumentCaptor<LoadBooksListCallback> mLoadBooksCallbackCaptor;

    private BooksViewModel mBooksViewModel;

    @Before
    public void setupBooksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mBooksViewModel = new BooksViewModel(mContext, mBooksRepository);

        // We initialise the books to 3, with one active and two favorited
        BOOKS = Lists.newArrayList(new BookListItem("Title1", "Description1"),
                new BookListItem("Title2", "Description2", true), new BookListItem("Title3", "Description3", true));

        mBooksViewModel.getSnackbarMessage().removeObservers(TestUtils.TEST_OBSERVER);

    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.successfully_saved_book_message))
                .thenReturn("EDIT_RESULT_OK");
        when(mContext.getString(R.string.successfully_added_book_message))
                .thenReturn("ADD_EDIT_RESULT_OK");
        when(mContext.getString(successfully_deleted_book_message))
                .thenReturn("DELETE_RESULT_OK");

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void loadAllBooksFromRepository_dataLoaded() {
        // Given an initialized BooksViewModel with initialized books
        // When loading of Books is requested
        mBooksViewModel.setFiltering(BooksFilterType.ALL_BOOKS);
        mBooksViewModel.loadBooks(true);

        // Callback is captured and invoked with stubbed books
        verify(mBooksRepository).getBooks(mLoadBooksCallbackCaptor.capture());


        // Then progress indicator is shown
        assertTrue(mBooksViewModel.dataLoading.get());
        mLoadBooksCallbackCaptor.getValue().onBooksListLoaded(BOOKS);

        // Then progress indicator is hidden
        assertFalse(mBooksViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mBooksViewModel.items.isEmpty());
        assertTrue(mBooksViewModel.items.size() == 3);
    }

    @Test
    public void loadActiveBooksFromRepositoryAndLoadIntoView() {
        // Given an initialized BooksViewModel with initialized books
        // When loading of Books is requested
        mBooksViewModel.setFiltering(BooksFilterType.NOT_FAVORITED_BOOKS);
        mBooksViewModel.loadBooks(true);

        // Callback is captured and invoked with stubbed books
        verify(mBooksRepository).getBooks(mLoadBooksCallbackCaptor.capture());
        mLoadBooksCallbackCaptor.getValue().onBooksListLoaded(BOOKS);

        // Then progress indicator is hidden
        assertFalse(mBooksViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mBooksViewModel.items.isEmpty());
        assertTrue(mBooksViewModel.items.size() == 1);
    }

    @Test
    public void loadCompletedBooksFromRepositoryAndLoadIntoView() {
        // Given an initialized BooksViewModel with initialized books
        // When loading of Books is requested
        mBooksViewModel.setFiltering(BooksFilterType.FAVORITED_BOOKS);
        mBooksViewModel.loadBooks(true);

        // Callback is captured and invoked with stubbed books
        verify(mBooksRepository).getBooks(mLoadBooksCallbackCaptor.capture());
        mLoadBooksCallbackCaptor.getValue().onBooksListLoaded(BOOKS);

        // Then progress indicator is hidden
        assertFalse(mBooksViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mBooksViewModel.items.isEmpty());
        assertTrue(mBooksViewModel.items.size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddBookUi() {

        Observer<Void> observer = mock(Observer.class);

        mBooksViewModel.getNewBookEvent().observe(TestUtils.TEST_OBSERVER, observer);

        // When adding a new book
        mBooksViewModel.addNewBook();

        // Then the event is triggered
        verify(observer).onChanged(null);
    }

    @Test
    public void clearCompletedBooks_ClearsBooks() {
        // When favorited books are cleared
        mBooksViewModel.clearCompletedBooks();

        // Then repository is called and the view is notified
        verify(mBooksRepository).clearCompletedBooks();
        verify(mBooksRepository).getBooks(any(LoadBooksListCallback.class));
    }

    @Test
    public void handleActivityResult_editOK() {
        // When BookDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mBooksViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        mBooksViewModel.handleActivityResult(
                AddEditBookActivity.REQUEST_CODE, BookDetailActivity.EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_saved_book_message);
    }

    @Test
    public void handleActivityResult_addEditOK() {
        // When BookDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mBooksViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        // When AddEditBookActivity sends a ADD_EDIT_RESULT_OK
        mBooksViewModel.handleActivityResult(
                AddEditBookActivity.REQUEST_CODE, AddEditBookActivity.ADD_EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_added_book_message);
    }

    @Test
    public void handleActivityResult_deleteOk() {
        // When BookDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mBooksViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        // When AddEditBookActivity sends a ADD_EDIT_RESULT_OK
        mBooksViewModel.handleActivityResult(
                AddEditBookActivity.REQUEST_CODE, BookDetailActivity.DELETE_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_deleted_book_message);
    }

    @Test
    public void getBooksAddViewVisible() {
        // When the filter type is ALL_BOOKS
        mBooksViewModel.setFiltering(BooksFilterType.ALL_BOOKS);

        // Then the "Add book" action is visible
        assertThat(mBooksViewModel.booksAddViewVisible.get(), is(true));
    }
}
