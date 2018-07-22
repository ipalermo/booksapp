
package com.zeelo.android.architecture.assignment.booksapp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;
import com.zeelo.android.architecture.assignment.booksapp.data.source.local.BooksLocalDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.local.ToDoDatabase;
import com.zeelo.android.architecture.assignment.booksapp.data.source.remote.BooksRemoteDataSource;
import com.zeelo.android.architecture.assignment.booksapp.util.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of production implementations for
 * {@link BooksDataSource} at compile time.
 */
public class Injection {

    public static BooksRepository provideBooksRepository(@NonNull Context context) {
        checkNotNull(context);
        ToDoDatabase database = ToDoDatabase.getInstance(context);
        return BooksRepository.getInstance(BooksRemoteDataSource.getInstance(),
                BooksLocalDataSource.getInstance(new AppExecutors(),
                        database.bookDao()));
    }
}
