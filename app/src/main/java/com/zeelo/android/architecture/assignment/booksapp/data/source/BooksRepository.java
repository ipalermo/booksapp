
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load books from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 *
 * //TODO: Implement this class using LiveData.
 */
public class BooksRepository implements BooksDataSource {

    private volatile static BooksRepository INSTANCE = null;

    private final BooksDataSource mBooksRemoteDataSource;

    private final BooksDataSource mBooksLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Book> mCachedBooks;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    private boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private BooksRepository(@NonNull BooksDataSource booksRemoteDataSource,
                            @NonNull BooksDataSource booksLocalDataSource) {
        mBooksRemoteDataSource = checkNotNull(booksRemoteDataSource);
        mBooksLocalDataSource = checkNotNull(booksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param booksRemoteDataSource the backend data source
     * @param booksLocalDataSource  the device storage data source
     * @return the {@link BooksRepository} instance
     */
    public static BooksRepository getInstance(BooksDataSource booksRemoteDataSource,
                                              BooksDataSource booksLocalDataSource) {
        if (INSTANCE == null) {
            synchronized (BooksRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BooksRepository(booksRemoteDataSource, booksLocalDataSource);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(BooksDataSource, BooksDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets books from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadBooksCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getBooks(@NonNull final LoadBooksCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedBooks != null && !mCacheIsDirty) {
            callback.onBooksLoaded(new ArrayList<>(mCachedBooks.values()));
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getBooksFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mBooksLocalDataSource.getBooks(new LoadBooksCallback() {
                @Override
                public void onBooksLoaded(List<Book> books) {
                    refreshCache(books);

                    EspressoIdlingResource.decrement(); // Set app as idle.
                    callback.onBooksLoaded(new ArrayList<>(mCachedBooks.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getBooksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveBook(@NonNull Book book) {
        checkNotNull(book);
        mBooksRemoteDataSource.saveBook(book);
        mBooksLocalDataSource.saveBook(book);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedBooks == null) {
            mCachedBooks = new LinkedHashMap<>();
        }
        mCachedBooks.put(book.getId(), book);
    }

    /**
     * Gets books from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetBookCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getBook(@NonNull final String bookId, @NonNull final GetBookCallback callback) {
        checkNotNull(bookId);
        checkNotNull(callback);

        Book cachedBook = getBookWithId(bookId);

        // Respond immediately with cache if available
        if (cachedBook != null) {
            callback.onBookLoaded(cachedBook);
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        // Load from server/persisted if needed.

        // Is the book in the local data source? If not, query the network.
        mBooksLocalDataSource.getBook(bookId, new GetBookCallback() {
            @Override
            public void onBookLoaded(Book book) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedBooks == null) {
                    mCachedBooks = new LinkedHashMap<>();
                }
                mCachedBooks.put(book.getId(), book);

                EspressoIdlingResource.decrement(); // Set app as idle.

                callback.onBookLoaded(book);
            }

            @Override
            public void onDataNotAvailable() {
                mBooksRemoteDataSource.getBook(bookId, new GetBookCallback() {
                    @Override
                    public void onBookLoaded(Book book) {
                        if (book == null) {
                            onDataNotAvailable();
                            return;
                        }
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedBooks == null) {
                            mCachedBooks = new LinkedHashMap<>();
                        }
                        mCachedBooks.put(book.getId(), book);
                        EspressoIdlingResource.decrement(); // Set app as idle.

                        callback.onBookLoaded(book);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        EspressoIdlingResource.decrement(); // Set app as idle.

                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshBook() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllBooks() {
        mBooksRemoteDataSource.deleteAllBooks();
        mBooksLocalDataSource.deleteAllBooks();

        if (mCachedBooks == null) {
            mCachedBooks = new LinkedHashMap<>();
        }
        mCachedBooks.clear();
    }

    @Override
    public void deleteBook(@NonNull String bookId) {
        mBooksRemoteDataSource.deleteBook(checkNotNull(bookId));
        mBooksLocalDataSource.deleteBook(checkNotNull(bookId));

        mCachedBooks.remove(bookId);
    }

    private void getBooksFromRemoteDataSource(@NonNull final LoadBooksCallback callback) {
        mBooksRemoteDataSource.getBooks(new LoadBooksCallback() {
            @Override
            public void onBooksLoaded(List<Book> books) {
                refreshCache(books);
                refreshLocalDataSource(books);

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onBooksLoaded(new ArrayList<>(mCachedBooks.values()));
            }

            @Override
            public void onDataNotAvailable() {

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Book> books) {
        if (mCachedBooks == null) {
            mCachedBooks = new LinkedHashMap<>();
        }
        mCachedBooks.clear();
        for (Book book : books) {
            mCachedBooks.put(book.getId(), book);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Book> books) {
        mBooksLocalDataSource.deleteAllBooks();
        for (Book book : books) {
            mBooksLocalDataSource.saveBook(book);
        }
    }

    @Nullable
    private Book getBookWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedBooks == null || mCachedBooks.isEmpty()) {
            return null;
        } else {
            return mCachedBooks.get(id);
        }
    }
}
