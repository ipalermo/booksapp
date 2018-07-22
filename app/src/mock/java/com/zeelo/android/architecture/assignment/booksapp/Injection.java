
package com.zeelo.android.architecture.assignment.booksapp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zeelo.android.architecture.assignment.booksapp.data.FakeBooksRemoteDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;
import com.zeelo.android.architecture.assignment.booksapp.data.source.local.BooksDatabase;
import com.zeelo.android.architecture.assignment.booksapp.data.source.local.BooksLocalDataSource;
import com.zeelo.android.architecture.assignment.booksapp.util.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of mock implementations for
 * {@link BooksDataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static BooksRepository provideBooksRepository(@NonNull Context context) {
        checkNotNull(context);
        BooksDatabase database = BooksDatabase.getInstance(context);
        return BooksRepository.getInstance(FakeBooksRemoteDataSource.getInstance(context),
                BooksLocalDataSource.getInstance(new AppExecutors(),
                        database.bookDao()));
    }
}
