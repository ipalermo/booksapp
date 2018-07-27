
package com.zeelo.android.architecture.assignment.booksapp.addeditbook;


import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;

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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddEditBookViewModel}.
 */
public class AddEditBookViewModelTest {

    // Executes each book synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private BooksRepository mBooksRepository;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<BooksDataSource.GetBookDetailsCallback> mGetBookCallbackCaptor;

    private AddEditBookViewModel mAddEditBookViewModel;

    @Before
    public void setupAddEditBookViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddEditBookViewModel = new AddEditBookViewModel(
                mock(Application.class), mBooksRepository);
    }

    @Test
    public void saveNewBookToRepository_showsSuccessMessageUi() {
        // When the ViewModel is asked to save a book
        mAddEditBookViewModel.description.set("Some BookListItem Description");
        mAddEditBookViewModel.title.set("New BookListItem Title");
        mAddEditBookViewModel.saveBook();

        // Then a book is saved in the repository and the view updated
        verify(mBooksRepository).saveBook(any(Book.class)); // saved to the model
    }

    @Test
    public void populateBook_callsRepoAndUpdatesView() {
        Book testBook = new Book("TITLE","1","DESCRIPTION");

        // Get a reference to the class under test
        mAddEditBookViewModel = new AddEditBookViewModel(
                mock(Application.class), mBooksRepository);


        // When the ViewModel is asked to populate an existing book
        mAddEditBookViewModel.start(testBook.getId());

        // Then the book repository is queried and the view updated
        verify(mBooksRepository).getBookDetails(eq(testBook.getId()), mGetBookCallbackCaptor.capture());

        // Simulate callback
        mGetBookCallbackCaptor.getValue().onBookDetailsLoaded(testBook);

        // Verify the fields were updated
        assertThat(mAddEditBookViewModel.title.get(), is(testBook.getTitle()));
        assertThat(mAddEditBookViewModel.description.get(), is(testBook.getVolumeInfo().getDescription()));
    }
}
