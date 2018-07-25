
package com.zeelo.android.architecture.assignment.booksapp.addeditbook;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;
import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.SingleLiveEvent;
import com.zeelo.android.architecture.assignment.booksapp.SnackbarMessage;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically. See
 * {@link com.zeelo.android.architecture.assignment.booksapp.statistics.StatisticsViewModel} for
 * how to deal with more complex scenarios.
 */
public class AddEditBookViewModel extends AndroidViewModel implements BooksDataSource.GetBookDetailsCallback {

    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> description = new ObservableField<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private final SingleLiveEvent<Void> mBookUpdated = new SingleLiveEvent<>();

    private final BooksRepository mBooksRepository;

    @Nullable
    private String mBookId;

    private boolean mIsNewBook;

    private boolean mIsDataLoaded = false;

    private boolean mBookCompleted = false;

    public AddEditBookViewModel(Application context,
                                BooksRepository booksRepository) {
        super(context);
        mBooksRepository = booksRepository;
    }

    public void start(String bookId) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return;
        }
        mBookId = bookId;
        if (bookId == null) {
            // No need to populate, it's a new book
            mIsNewBook = true;
            return;
        }
        if (mIsDataLoaded) {
            // No need to populate, already have data.
            return;
        }
        mIsNewBook = false;
        dataLoading.set(true);

        mBooksRepository.getBookDetails(bookId, this);
    }

    @Override
    public void onBookDetailsLoaded(Book book) {
        title.set(book.getTitle());
        if (book.getVolumeInfo() != null) {
            description.set(book.getVolumeInfo().getDescription());
        }

        dataLoading.set(false);
        mIsDataLoaded = true;

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on fab.
    void saveBook() {
        Book book = new Book(title.get(), description.get());
        if (Strings.isNullOrEmpty(book.getTitle())) {
            mSnackbarText.setValue(R.string.empty_book_message);
            return;
        }
        if (isNewBook() || mBookId == null) {
            createBook(book);
        } else {
            book = new Book(title.get(), mBookId, description.get());
            updateBook(book);
        }
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    SingleLiveEvent<Void> getBookUpdatedEvent() {
        return mBookUpdated;
    }

    private boolean isNewBook() {
        return mIsNewBook;
    }

    private void createBook(Book newBook) {
        mBooksRepository.saveBook(newBook);
        mBookUpdated.call();
    }

    private void updateBook(Book book) {
        if (isNewBook()) {
            throw new RuntimeException("updateBook() was called but book is new.");
        }
        mBooksRepository.saveBook(book);
        mBookUpdated.call();
    }
}
