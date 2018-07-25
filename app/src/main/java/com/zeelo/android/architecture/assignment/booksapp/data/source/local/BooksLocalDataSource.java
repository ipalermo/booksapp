
package com.zeelo.android.architecture.assignment.booksapp.data.source.local;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.util.AppExecutors;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.zeelo.android.architecture.assignment.booksapp.data.source.remote.BooksRemoteDataSource.BOOK_DETAILS_API_PATH;


/**
 * Concrete implementation of a data source as a db.
 */
public class BooksLocalDataSource implements BooksDataSource {

    private static volatile BooksLocalDataSource INSTANCE;

    private BooksDao mBooksDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private BooksLocalDataSource(@NonNull AppExecutors appExecutors,
                                 @NonNull BooksDao booksDao) {
        mAppExecutors = appExecutors;
        mBooksDao = booksDao;
    }

    public static BooksLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                   @NonNull BooksDao booksDao) {
        if (INSTANCE == null) {
            synchronized (BooksLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BooksLocalDataSource(appExecutors, booksDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadBooksListCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getBooks(@NonNull final LoadBooksListCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<BookListItem> booksListItems = mBooksDao.getBookListItems();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (booksListItems.isEmpty()) {
                            // This will be called if the table is new or just empty.
                            callback.onDataNotAvailable();
                        } else {
                            callback.onBooksListLoaded(booksListItems);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveBooksListItems(@NonNull final List<BookListItem> booksListItems) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mBooksDao.insertBookListItems(booksListItems);
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Note: {@link GetBookDetailsCallback#onDataNotAvailable()} is fired if the {@link BookListItem} isn't
     * found.
     */
    @Override
    public void getBookDetails(@NonNull final String bookId, @NonNull final GetBookDetailsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Book book = mBooksDao.getBookById(bookId);

                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (book != null) {
                            callback.onBookDetailsLoaded(book);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveBook(@NonNull final Book book) {
        checkNotNull(book);
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mBooksDao.insertBookListItem(new BookListItem(book.getTitle(), book.getId(), BOOK_DETAILS_API_PATH + book.getId()));
                mBooksDao.insertBook(book);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void refreshBook() {
        // Not required because the {@link BooksRepository} handles the logic of refreshing the
        // books from all the available data sources.
    }

    @Override
    public void deleteAllBooks() {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mBooksDao.deleteBooks();
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void deleteBook(@NonNull final String bookId) {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mBooksDao.deleteBookById(bookId);
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
