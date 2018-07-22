
package com.zeelo.android.architecture.assignment.booksapp.books;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.ScrollChildSwipeRefreshLayout;
import com.zeelo.android.architecture.assignment.booksapp.SnackbarMessage;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.databinding.BooksFragBinding;
import com.zeelo.android.architecture.assignment.booksapp.util.SnackbarUtils;

import java.util.ArrayList;

/**
 * Display a grid of {@link Book}s. User can choose to view all, active or favorited books.
 */
public class BooksFragment extends Fragment {

    private BooksViewModel mBooksViewModel;

    private BooksFragBinding mBooksFragBinding;

    private BooksAdapter mListAdapter;

    public BooksFragment() {
        // Requires empty public constructor
    }

    public static BooksFragment newInstance() {
        return new BooksFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBooksViewModel.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBooksFragBinding = BooksFragBinding.inflate(inflater, container, false);

        mBooksViewModel = BooksActivity.obtainViewModel(getActivity());

        mBooksFragBinding.setViewmodel(mBooksViewModel);

        setHasOptionsMenu(true);

        return mBooksFragBinding.getRoot();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_clear:
//                mBooksViewModel.clearCompletedBooks();
//                break;
//            case R.id.menu_filter:
//                showFilteringPopUpMenu();
//                break;
//            case R.id.menu_refresh:
//                mBooksViewModel.loadBooks(true);
//                break;
//        }
//        return true;
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.books_fragment_menu, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();
    }

    private void setupSnackbar() {
        mBooksViewModel.getSnackbarMessage().observe(this, new SnackbarMessage.SnackbarObserver() {
            @Override
            public void onNewMessage(@StringRes int snackbarMessageResourceId) {
                SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
            }
        });
    }

    private void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_books, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mBooksViewModel.setFiltering(BooksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mBooksViewModel.setFiltering(BooksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mBooksViewModel.setFiltering(BooksFilterType.ALL_TASKS);
                        break;
                }
                mBooksViewModel.loadBooks(false);
                return true;
            }
        });

        popup.show();
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_book);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBooksViewModel.addNewBook();
            }
        });
    }

    private void setupListAdapter() {
        ListView listView =  mBooksFragBinding.booksList;

        mListAdapter = new BooksAdapter(
                new ArrayList<Book>(0),
                mBooksViewModel
        );
        listView.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        ListView listView =  mBooksFragBinding.booksList;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mBooksFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
    }

}
