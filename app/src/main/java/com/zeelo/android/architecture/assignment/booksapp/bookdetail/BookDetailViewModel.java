
package com.zeelo.android.architecture.assignment.booksapp.bookdetail;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.SingleLiveEvent;
import com.zeelo.android.architecture.assignment.booksapp.SnackbarMessage;
import com.zeelo.android.architecture.assignment.booksapp.books.BooksFragment;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;


/**
 * Listens to user actions from the list item in ({@link BooksFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class BookDetailViewModel extends AndroidViewModel implements BooksDataSource.GetBookDetailsCallback {

    private final MutableLiveData<Book> mObservableBook = new MutableLiveData<>();

    public final ObservableField<Book> book = new ObservableField<>();

    public final ObservableBoolean favorited = new ObservableBoolean();

    private final SingleLiveEvent<Void> mEditBookCommand = new SingleLiveEvent<>();

    private final SingleLiveEvent<Void> mDeleteBookCommand = new SingleLiveEvent<>();

    private final BooksRepository mBooksRepository;

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private boolean mIsDataLoading;

    public BookDetailViewModel(Application context, BooksRepository booksRepository) {
        super(context);
        mBooksRepository = booksRepository;
    }

    public void deleteBook() {
        if (book.get() != null) {
            mBooksRepository.deleteBook(book.get().getId());
            mDeleteBookCommand.call();
        }
    }

    public void editBook() {
        mEditBookCommand.call();
    }

    public SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    public SingleLiveEvent<Void> getEditBookCommand() {
        return mEditBookCommand;
    }

    public SingleLiveEvent<Void> getDeleteBookCommand() {
        return mDeleteBookCommand;
    }

    public LiveData<Book> getObservableBook() {
        return mObservableBook;
    }

    public void setFavorited(boolean favorited) {
        if (mIsDataLoading) {
            return;
        }
//        Book book = this.book.get();
        if (favorited) {
//            mBooksRepository.favoriteBook(book);
            showSnackbarMessage(R.string.book_marked_favorite);
        } else {
//            mBooksRepository.unFavoriteBook(book);
            showSnackbarMessage(R.string.book_removed_favorite);
        }
    }

    public void start(String bookId) {
        if (bookId != null) {
            mIsDataLoading = true;
            mBooksRepository.getBookDetails(bookId, this);
        }
    }

    public void setBook(Book book) {
        this.book.set(book);
    }

    public boolean isDataAvailable() {
        return book.get() != null;
    }

    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    @Override
    public void onBookDetailsLoaded(Book book) {
        setBook(book);
        this.mObservableBook.setValue(book);
        mIsDataLoading = false;
    }

    @Override
    public void onDataNotAvailable() {
        book.set(null);
        mIsDataLoading = false;
    }

    public void onRefresh() {
        if (book.get() != null) {
            start(book.get().getId());
        }
    }

    @Nullable
    protected String getBookId() {
        return book.get().getId();
    }

    @Nullable
    public String getAuthor() {
        if (book.get().getVolumeInfo() != null && book.get().getVolumeInfo().getAuthors() != null) {
            return book.get().getVolumeInfo().getAuthors().get(0);
        }
        return null;
    }

    private void showSnackbarMessage(@StringRes Integer message) {
        mSnackbarText.setValue(message);
    }
}
