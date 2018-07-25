
package com.zeelo.android.architecture.assignment.booksapp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
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
    Map<String, BookListItem> mCachedListItems;

    Map<String, Book> mCachedBooks;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    private boolean mBooksCacheIsDirty = false;
    private boolean mListItemsCacheIsDirty = false;

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
     * Note: {@link LoadBooksListCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getBooks(@NonNull final LoadBooksListCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedListItems != null && !mListItemsCacheIsDirty) {
            callback.onBooksListLoaded(new ArrayList<>(mCachedListItems.values()));
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        if (mListItemsCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getBooksFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mBooksLocalDataSource.getBooks(new LoadBooksListCallback() {
                @Override
                public void onBooksListLoaded(List<BookListItem> bookItems) {
                    refreshListItemsCache(bookItems);

                    EspressoIdlingResource.decrement(); // Set app as idle.
                    callback.onBooksListLoaded(bookItems);
                }

                @Override
                public void onDataNotAvailable() {
                    getBooksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveBooksListItems(@NonNull List<BookListItem> booksListItems) {
        checkNotNull(booksListItems);
        mBooksLocalDataSource.saveBooksListItems(booksListItems);
    }

    @Override
    public void saveBook(@NonNull Book book) {
        checkNotNull(book);
        mBooksRemoteDataSource.saveBook(book);
        mBooksLocalDataSource.saveBook(book);

        // Do in memory cache update to keep the app UI up to date
        saveToBookToCache(book);
    }

    private void saveToBookToCache(Book book) {
        if (mCachedBooks == null) {
            mCachedBooks = new LinkedHashMap<>();
        }
        mCachedBooks.put(book.getId(), book);

        if (mCachedListItems == null) {
            mCachedListItems = new LinkedHashMap<>();
        }
        mCachedListItems.put(book.getId(), new BookListItem(book.getTitle(), book.getId()));
    }

    /**
     * Gets books from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetBookDetailsCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getBookDetails(@NonNull final String bookId, @NonNull final GetBookDetailsCallback callback) {
        checkNotNull(bookId);
        checkNotNull(callback);

        Book cachedBook = getBookWithId(bookId);

        // Respond immediately with cache if available
        if (cachedBook != null) {
            callback.onBookDetailsLoaded(cachedBook);
            return;
        }

        EspressoIdlingResource.increment(); // App is busy until further notice

        // Load from server/persisted if needed.

        // Is the book in the local data source? If not, query the network.
        mBooksLocalDataSource.getBookDetails(bookId, new GetBookDetailsCallback() {
            @Override
            public void onBookDetailsLoaded(Book book) {
                // Do in memory cache update to keep the app UI up to date
                saveToBookToCache(book);

                EspressoIdlingResource.decrement(); // Set app as idle.

                callback.onBookDetailsLoaded(book);
            }

            @Override
            public void onDataNotAvailable() {
                mBooksRemoteDataSource.getBookDetails(bookId, new GetBookDetailsCallback() {
                    @Override
                    public void onBookDetailsLoaded(Book book) {
                        if (book == null) {
                            onDataNotAvailable();
                            return;
                        }
                        // Do in memory cache update to keep the app UI up to date
                        saveToBookToCache(book);

                        EspressoIdlingResource.decrement(); // Set app as idle.

                        callback.onBookDetailsLoaded(book);
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
        mBooksCacheIsDirty = true;
    }

    @Override
    public void deleteAllBooks() {
        mBooksRemoteDataSource.deleteAllBooks();
        mBooksLocalDataSource.deleteAllBooks();

        if (mCachedListItems == null) {
            mCachedListItems = new LinkedHashMap<>();
        }
        mCachedListItems.clear();
    }

    @Override
    public void deleteBook(@NonNull String bookId) {
        mBooksRemoteDataSource.deleteBook(checkNotNull(bookId));
        mBooksLocalDataSource.deleteBook(checkNotNull(bookId));

        mCachedListItems.remove(bookId);
    }

    private void getBooksFromRemoteDataSource(@NonNull final LoadBooksListCallback callback) {
        mBooksRemoteDataSource.getBooks(new LoadBooksListCallback() {
            @Override
            public void onBooksListLoaded(List<BookListItem> bookItems) {
                refreshListItemsCache(bookItems);

                mBooksLocalDataSource.saveBooksListItems(bookItems);

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onBooksListLoaded(new ArrayList<>(mCachedListItems.values()));
            }

            @Override
            public void onDataNotAvailable() {

                EspressoIdlingResource.decrement(); // Set app as idle.
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshBooksCache(List<Book> books) {
        if (mCachedBooks == null) {
            mCachedBooks = new LinkedHashMap<>();
        }
        mCachedBooks.clear();
        for (Book book : books) {
            saveToBookToCache(book);
        }
        mBooksCacheIsDirty = false;
    }

    private void refreshListItemsCache(List<BookListItem> bookItems) {
        if (mCachedListItems == null) {
            mCachedListItems = new LinkedHashMap<>();
        }
        mCachedListItems.clear();
        for (BookListItem bookItem : bookItems) {
            mCachedListItems.put(bookItem.getId(), bookItem);
        }
        mListItemsCacheIsDirty = false;
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
