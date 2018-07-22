
package com.zeelo.android.architecture.assignment.booksapp.statistics;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;

import java.util.List;

/**
 * Exposes the data to be used in the statistics screen.
 * <p>
 * This ViewModel uses both {@link ObservableField}s ({@link ObservableBoolean}s in this case) and
 * {@link Bindable} getters. The values in {@link ObservableField}s are used directly in the layout,
 * whereas the {@link Bindable} getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
public class StatisticsViewModel extends AndroidViewModel {

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableBoolean error = new ObservableBoolean(false);

    public final ObservableField<String> numberOfActiveBooks = new ObservableField<>();

    public final ObservableField<String> numberOfCompletedBooks = new ObservableField<>();

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    public final ObservableBoolean empty = new ObservableBoolean();

    private int mNumberOfActiveBooks = 0;

    private int mNumberOfCompletedBooks = 0;

    private final Context mContext;

    private final BooksRepository mBooksRepository;

    public StatisticsViewModel(Application context, BooksRepository booksRepository) {
        super(context);
        mContext = context;
        mBooksRepository = booksRepository;
    }

    public void start() {
        loadStatistics();
    }

    public void loadStatistics() {
        dataLoading.set(true);

        mBooksRepository.getBooks(new BooksDataSource.LoadBooksCallback() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                error.set(false);
                computeStats(books);
            }

            @Override
            public void onDataNotAvailable() {
                error.set(true);
                mNumberOfActiveBooks = 0;
                mNumberOfCompletedBooks = 0;
                updateDataBindingObservables();
            }
        });
    }

    /**
     * Called when new data is ready.
     */
    private void computeStats(List<Book> books) {
    }

    private void updateDataBindingObservables() {
        numberOfCompletedBooks.set(
                mContext.getString(R.string.statistics_completed_books, mNumberOfCompletedBooks));
        numberOfActiveBooks.set(
                mContext.getString(R.string.statistics_active_books, mNumberOfActiveBooks));
        empty.set(mNumberOfActiveBooks + mNumberOfCompletedBooks == 0);
        dataLoading.set(false);

    }
}
