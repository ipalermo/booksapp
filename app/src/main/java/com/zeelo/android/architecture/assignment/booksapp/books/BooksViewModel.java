
package com.zeelo.android.architecture.assignment.booksapp.books;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.SingleLiveEvent;
import com.zeelo.android.architecture.assignment.booksapp.SnackbarMessage;
import com.zeelo.android.architecture.assignment.booksapp.addeditbook.AddEditBookActivity;
import com.zeelo.android.architecture.assignment.booksapp.bookdetail.BookDetailActivity;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;

import java.util.List;


/**
 * Exposes the data to be used in the book list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class BooksViewModel extends AndroidViewModel {

    // These observable fields will update Views automatically
    public final ObservableList<Book> items = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> currentFilteringLabel = new ObservableField<>();

    public final ObservableField<String> noBooksLabel = new ObservableField<>();

    public final ObservableField<Drawable> noBookIconRes = new ObservableField<>();

    public final ObservableBoolean empty = new ObservableBoolean(false);

    public final ObservableBoolean booksAddViewVisible = new ObservableBoolean();

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private BooksFilterType mCurrentFiltering = BooksFilterType.ALL_TASKS;

    private final BooksRepository mBooksRepository;

    private final ObservableBoolean mIsDataLoadingError = new ObservableBoolean(false);

    private final SingleLiveEvent<String> mOpenBookEvent = new SingleLiveEvent<>();

    private final Context mContext; // To avoid leaks, this must be an Application Context.

    private final SingleLiveEvent<Void> mNewBookEvent = new SingleLiveEvent<>();

    public BooksViewModel(
            Application context,
            BooksRepository repository) {
        super(context);
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mBooksRepository = repository;

        // Set initial state
        setFiltering(BooksFilterType.ALL_TASKS);
    }

    public void start() {
        loadBooks(false);
    }

    public void loadBooks(boolean forceUpdate) {
        loadBooks(forceUpdate, true);
    }

    /**
     * Sets the current book filtering type.
     *
     * @param requestType Can be {@link BooksFilterType#ALL_TASKS},
     *                    {@link BooksFilterType#COMPLETED_TASKS}, or
     *                    {@link BooksFilterType#ACTIVE_TASKS}
     */
    public void setFiltering(BooksFilterType requestType) {
        mCurrentFiltering = requestType;

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        switch (requestType) {
            case ALL_TASKS:
                currentFilteringLabel.set(mContext.getString(R.string.label_all));
                noBooksLabel.set(mContext.getResources().getString(R.string.no_books_all));
                noBookIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_assignment_turned_in_24dp));
                booksAddViewVisible.set(true);
                break;
            case ACTIVE_TASKS:
                currentFilteringLabel.set(mContext.getString(R.string.label_active));
                noBooksLabel.set(mContext.getResources().getString(R.string.no_books_active));
                noBookIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_check_circle_24dp));
                booksAddViewVisible.set(false);
                break;
            case COMPLETED_TASKS:
                currentFilteringLabel.set(mContext.getString(R.string.label_completed));
                noBooksLabel.set(mContext.getResources().getString(R.string.no_books_completed));
                noBookIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_verified_user_24dp));
                booksAddViewVisible.set(false);
                break;
        }
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    SingleLiveEvent<String> getOpenBookEvent() {
        return mOpenBookEvent;
    }

    SingleLiveEvent<Void> getNewBookEvent() {
        return mNewBookEvent;
    }

    private void showSnackbarMessage(Integer message) {
        mSnackbarText.setValue(message);
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    public void addNewBook() {
        mNewBookEvent.call();
    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditBookActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case BookDetailActivity.EDIT_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_saved_book_message);
                    break;
                case AddEditBookActivity.ADD_EDIT_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_added_book_message);
                    break;
                case BookDetailActivity.DELETE_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_deleted_book_message);
                    break;
            }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link BooksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadBooks(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            dataLoading.set(true);
        }
        if (forceUpdate) {

            mBooksRepository.refreshBook();
        }

        mBooksRepository.getBooks(new BooksDataSource.LoadBooksCallback() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                if (showLoadingUI) {
                    dataLoading.set(false);
                }
                mIsDataLoadingError.set(false);

                items.clear();
                items.addAll(books);
                empty.set(items.isEmpty());
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.set(true);
            }
        });
    }
}
