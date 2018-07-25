
package com.zeelo.android.architecture.assignment.booksapp.addeditbook;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.ViewModelFactory;
import com.zeelo.android.architecture.assignment.booksapp.util.ActivityUtils;

/**
 * Displays an add or edit book screen.
 */
public class AddEditBookActivity extends AppCompatActivity implements AddEditBookNavigator {

    public static final int REQUEST_CODE = 1;

    public static final int ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBookSaved() {
        setResult(ADD_EDIT_RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addbook_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        AddEditBookFragment addEditBookFragment = obtainViewFragment();

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),
                addEditBookFragment, R.id.contentFrame);

        subscribeToNavigationChanges();
    }

    private void subscribeToNavigationChanges() {
        AddEditBookViewModel viewModel = obtainViewModel(this);

        // The activity observes the navigation events in the ViewModel
        viewModel.getBookUpdatedEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                AddEditBookActivity.this.onBookSaved();
            }
        });
    }

    public static AddEditBookViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(AddEditBookViewModel.class);
    }

    @NonNull
    private AddEditBookFragment obtainViewFragment() {
        // View Fragment
        AddEditBookFragment addEditBookFragment = (AddEditBookFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (addEditBookFragment == null) {
            addEditBookFragment = AddEditBookFragment.newInstance();

            // Send the book ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString(AddEditBookFragment.ARGUMENT_EDIT_BOOK_ID,
                    getIntent().getStringExtra(AddEditBookFragment.ARGUMENT_EDIT_BOOK_ID));
            addEditBookFragment.setArguments(bundle);
        }
        return addEditBookFragment;
    }
}
