
package com.zeelo.android.architecture.assignment.booksapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.VisibleForTesting;

import com.zeelo.android.architecture.assignment.booksapp.addeditbook.AddEditBookViewModel;
import com.zeelo.android.architecture.assignment.booksapp.bookdetail.BookDetailViewModel;
import com.zeelo.android.architecture.assignment.booksapp.books.BooksViewModel;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;
import com.zeelo.android.architecture.assignment.booksapp.statistics.StatisticsViewModel;

/**
 * A creator is used to inject the product ID into the ViewModel
 * <p>
 * This creator is to showcase how to inject dependencies into ViewModels. It's not
 * actually necessary in this case, as the product ID can be passed in a public method.
 */
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    @SuppressLint("StaticFieldLeak")
    private static volatile ViewModelFactory INSTANCE;

    private final Application mApplication;

    private final BooksRepository mBooksRepository;

    public static ViewModelFactory getInstance(Application application) {

        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ViewModelFactory(application,
                            Injection.provideBooksRepository(application.getApplicationContext()));
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void destroyInstance() {
        INSTANCE = null;
    }

    private ViewModelFactory(Application application, BooksRepository repository) {
        mApplication = application;
        mBooksRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StatisticsViewModel.class)) {
            //noinspection unchecked
            return (T) new StatisticsViewModel(mApplication, mBooksRepository);
        } else if (modelClass.isAssignableFrom(BookDetailViewModel.class)) {
            //noinspection unchecked
            return (T) new BookDetailViewModel(mApplication, mBooksRepository);
        } else if (modelClass.isAssignableFrom(AddEditBookViewModel.class)) {
            //noinspection unchecked
            return (T) new AddEditBookViewModel(mApplication, mBooksRepository);
        } else if (modelClass.isAssignableFrom(BooksViewModel.class)) {
            //noinspection unchecked
            return (T) new BooksViewModel(mApplication, mBooksRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
