
package com.zeelo.android.architecture.assignment.booksapp.bookdetail;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.SnackbarMessage;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.databinding.BookdetailFragBinding;
import com.zeelo.android.architecture.assignment.booksapp.util.SnackbarUtils;


/**
 * Main UI for the book detail screen.
 */
public class BookDetailFragment extends Fragment {

    public static final String ARGUMENT_BOOK_ID = "BOOK_ID";

    public static final int REQUEST_EDIT_TASK = 1;

    private BookDetailViewModel mViewModel;

    public static BookDetailFragment newInstance(String bookId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_BOOK_ID, bookId);
        BookDetailFragment fragment = new BookDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupFab();

        setupSnackbar();
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarMessage().observe(this, new SnackbarMessage.SnackbarObserver() {
            @Override
            public void onNewMessage(@StringRes int snackbarMessageResourceId) {
                SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));

            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_book);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.editBook();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start(getArguments().getString(ARGUMENT_BOOK_ID));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bookdetail_frag, container, false);

        BookdetailFragBinding viewDataBinding = BookdetailFragBinding.bind(view);

        mViewModel = BookDetailActivity.obtainViewModel(getActivity());

        viewDataBinding.setViewmodel(mViewModel);

        BookDetailUserActionsListener actionsListener = getBookDetailUserActionsListener();

        viewDataBinding.setListener(actionsListener);

        subscribeToModel(mViewModel);

        setHasOptionsMenu(true);

        return view;
    }

    private void subscribeToModel(final BookDetailViewModel model) {

        // Observe book details data
        model.getObservableBook().observe(this, new Observer<Book>() {
            @Override
            public void onChanged(@Nullable Book book) {
                model.setBook(book);

                updateBookLargeThumbnail(book);
            }
        });
    }

    private void updateBookLargeThumbnail(Book book) {
        ImageView appBarImage = getActivity().findViewById(R.id.book_large_thumbnail);
        ImageView bookImage = getActivity().findViewById(R.id.book_small_thumbnail);
        Book.VolumeInfo volumeInfo = book.getVolumeInfo();
        if (volumeInfo != null && volumeInfo.getImageLinks() != null) {
            loadThumbnail(volumeInfo.getImageLinks().getThumbnail(), appBarImage);
            loadThumbnail(volumeInfo.getImageLinks().getThumbnail(), bookImage);
        }
    }

    private void loadThumbnail(String url, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions()
            .placeholder(R.drawable.loadingBookBackground)
            .error(R.drawable.logo)
            .fitCenter();

        Glide.with(this)
                .load(url)
                .apply(requestOptions)
                .into(imageView);
    }

    private BookDetailUserActionsListener getBookDetailUserActionsListener() {
        return new BookDetailUserActionsListener() {
            @Override
            public void onFavoriteChanged(View v) {
                mViewModel.setFavorited(((CheckBox) v).isChecked());
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mViewModel.deleteBook();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bookdetail_fragment_menu, menu);
    }
}
