
package com.zeelo.android.architecture.assignment.booksapp.statistics;


import android.app.Application;
import android.arch.core.executor.testing.InstantBookExecutorRule;

import com.google.common.collect.Lists;
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

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
public class StatisticsViewModelTest {

    // Executes each book synchronously using Architecture Components.
    @Rule
    public InstantBookExecutorRule instantExecutorRule = new InstantBookExecutorRule();

    private static List<Book> BOOKS;

    @Mock
    private BooksRepository mBooksRepository;

    @Captor
    private ArgumentCaptor<BooksDataSource.LoadBooksCallback> mLoadBooksCallbackCaptor;

    private StatisticsViewModel mStatisticsViewModel;

    @Before
    public void setupStatisticsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mStatisticsViewModel = new StatisticsViewModel(mock(Application.class), mBooksRepository);

        // We initialise the books to 3, with one active and two favorited
        BOOKS = Lists.newArrayList(new Book("Title1", "Description1"),
                new Book("Title2", "Description2", true), new Book("Title3", "Description3", true));
    }

    @Test
    public void loadEmptyBooksFromRepository_EmptyResults() {
        // Given an initialized StatisticsViewModel with no books
        BOOKS.clear();

        // When loading of Books is requested
        mStatisticsViewModel.loadStatistics();

        // Callback is captured and invoked with stubbed books
        verify(mBooksRepository).getBooks(mLoadBooksCallbackCaptor.capture());
        mLoadBooksCallbackCaptor.getValue().onBooksLoaded(BOOKS);

        // Then the results are empty
        assertThat(mStatisticsViewModel.empty.get(), is(true));
    }

    @Test
    public void loadNonEmptyBooksFromRepository_NonEmptyResults() {
        // When loading of Books is requested
        mStatisticsViewModel.loadStatistics();

        // Callback is captured and invoked with stubbed books
        verify(mBooksRepository).getBooks(mLoadBooksCallbackCaptor.capture());
        mLoadBooksCallbackCaptor.getValue().onBooksLoaded(BOOKS);

        // Then the results are empty
        assertThat(mStatisticsViewModel.empty.get(), is(false));
    }


    @Test
    public void loadStatisticsWhenBooksAreUnavailable_CallErrorToDisplay() {
        // When statistics are loaded
        mStatisticsViewModel.loadStatistics();

        // And books data isn't available
        verify(mBooksRepository).getBooks(mLoadBooksCallbackCaptor.capture());
        mLoadBooksCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        assertEquals(mStatisticsViewModel.empty.get(), true);
        assertEquals(mStatisticsViewModel.error.get(), true);
    }
}
