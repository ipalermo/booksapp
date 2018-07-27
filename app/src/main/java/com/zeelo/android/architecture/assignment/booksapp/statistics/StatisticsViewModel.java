
package com.zeelo.android.architecture.assignment.booksapp.statistics;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
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

    public final ObservableField<String> numberOfNotFavoriteBooks = new ObservableField<>();

    public final ObservableField<String> numberOfFavoriteBooks = new ObservableField<>();

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    public final ObservableBoolean empty = new ObservableBoolean();

    private int mNumberOfNotFavoriteBooks = 0;

    private int mNumberOfFavoriteBooks = 0;

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

        mBooksRepository.getBooks(new BooksDataSource.LoadBooksListCallback() {
            @Override
            public void onBooksListLoaded(List<BookListItem> books) {
                error.set(false);
                computeStats(books);
            }

            @Override
            public void onDataNotAvailable() {
                error.set(true);
                mNumberOfNotFavoriteBooks = 0;
                mNumberOfFavoriteBooks = 0;
                updateDataBindingObservables();
            }
        });
    }

    /**
     * Called when new data is ready.
     */
    private void computeStats(List<BookListItem> books) {
//        int favorite = 0;
//        int notFavorite = 0;
//
//        for (BookListItem bookItem : books) {
//            if (bookItem.isFavorite()) {
//                favorite += 1;
//            } else {
//                notFavorite += 1;
//            }
//        }
//        mNumberOfFavoriteBooks = favorite;
//        mNumberOfNotFavoriteBooks = notFavorite;
//
        updateDataBindingObservables();
    }

    private void updateDataBindingObservables() {
        numberOfFavoriteBooks.set(
                mContext.getString(R.string.statistics_favorite_books, mNumberOfFavoriteBooks));
        numberOfNotFavoriteBooks.set(
                mContext.getString(R.string.statistics_not_fav_books, mNumberOfNotFavoriteBooks));
        empty.set(mNumberOfNotFavoriteBooks + mNumberOfFavoriteBooks == 0);
        dataLoading.set(false);

    }
}
