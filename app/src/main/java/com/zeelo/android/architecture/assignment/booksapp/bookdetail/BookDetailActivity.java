
package com.zeelo.android.architecture.assignment.booksapp.bookdetail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.ViewModelFactory;
import com.zeelo.android.architecture.assignment.booksapp.addeditbook.AddEditBookActivity;
import com.zeelo.android.architecture.assignment.booksapp.addeditbook.AddEditBookFragment;
import com.zeelo.android.architecture.assignment.booksapp.util.ActivityUtils;

import static com.zeelo.android.architecture.assignment.booksapp.addeditbook.AddEditBookActivity.ADD_EDIT_RESULT_OK;
import static com.zeelo.android.architecture.assignment.booksapp.bookdetail.BookDetailFragment.REQUEST_EDIT_TASK;

/**
 * Displays book details screen.
 */
public class BookDetailActivity extends AppCompatActivity implements BookDetailNavigator {

    public static final String EXTRA_BOOK_ID = "BOOK_ID";

    public static final int DELETE_RESULT_OK = RESULT_FIRST_USER + 2;

    public static final int EDIT_RESULT_OK = RESULT_FIRST_USER + 3;

    private BookDetailViewModel mBookViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bookdetail_act);

        setupToolbar();

        BookDetailFragment bookDetailFragment = findOrCreateViewFragment();

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),
                bookDetailFragment, R.id.contentFrame);

        mBookViewModel = obtainViewModel(this);

        subscribeToNavigationChanges(mBookViewModel);
    }

    @NonNull
    private BookDetailFragment findOrCreateViewFragment() {
        // Get the requested book id
        String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);

        BookDetailFragment bookDetailFragment = (BookDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (bookDetailFragment == null) {
            bookDetailFragment = BookDetailFragment.newInstance(bookId);
        }
        return bookDetailFragment;
    }

    @NonNull
    public static BookDetailViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(BookDetailViewModel.class);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    private void subscribeToNavigationChanges(BookDetailViewModel viewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.getEditBookCommand().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                BookDetailActivity.this.onStartEditBook();
            }
        });
        viewModel.getDeleteBookCommand().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                BookDetailActivity.this.onBookDeleted();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the book was edited successfully, go back to the list.
            if (resultCode == ADD_EDIT_RESULT_OK) {
                // If the result comes from the add/edit screen, it's an edit.
                setResult(EDIT_RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBookDeleted() {
        setResult(DELETE_RESULT_OK);
        // If the book was deleted successfully, go back to the list.
        finish();
    }

    @Override
    public void onStartEditBook() {
        String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        Intent intent = new Intent(this, AddEditBookActivity.class);
        intent.putExtra(AddEditBookFragment.ARGUMENT_EDIT_BOOK_ID, bookId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

}
