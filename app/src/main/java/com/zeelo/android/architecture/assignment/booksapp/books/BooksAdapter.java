package com.zeelo.android.architecture.assignment.booksapp.books;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.zeelo.android.architecture.assignment.booksapp.R;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
import com.zeelo.android.architecture.assignment.booksapp.databinding.BookItemBinding;

import java.util.List;


public class BooksAdapter extends BaseAdapter {

    private final BooksViewModel mBooksViewModel;

    private List<BookListItem> mBookItems;

    private final RequestManager glide;

    public BooksAdapter(List<BookListItem> books,
                        BooksViewModel booksViewModel,
                        RequestManager glide) {
        mBooksViewModel = booksViewModel;
        this.glide = glide;
        setList(books);

    }

    public void replaceData(List<BookListItem> books) {
        setList(books);
    }

    @Override
    public int getCount() {
        return mBookItems != null ? mBookItems.size() : 0;
    }

    @Override
    public BookListItem getItem(int position) {
        return mBookItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View view, final ViewGroup viewGroup) {
        BookItemBinding binding;
        if (view == null) {
            // Inflate
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            // Create the binding
            binding = BookItemBinding.inflate(inflater, viewGroup, false);
        } else {
            // Recycling view
            binding = DataBindingUtil.getBinding(view);
        }

        BookItemUserActionsListener userActionsListener = new BookItemUserActionsListener() {

            @Override
            public void onBookClicked(BookListItem book) {
                mBooksViewModel.getOpenBookEvent().setValue(book.getId());
            }
        };

        binding.setBook(mBookItems.get(position));

        BookListItem.VolumeInfo volumeInfo = mBookItems.get(position).getVolumeInfo();
        if (volumeInfo != null && volumeInfo.getImageLinks() != null) {
            loadThumbnail(volumeInfo.getImageLinks().getThumbnail(), binding.bookThumbnail);
        }

        binding.setListener(userActionsListener);

        binding.executePendingBindings();
        return binding.getRoot();
    }

    private void loadThumbnail(String url, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.loadingBookBackground);
        requestOptions.error(R.drawable.logo);
        requestOptions.centerInside();
        glide.load(url)
                .apply(requestOptions)
                .into(imageView);
    }

    private void setList(List<BookListItem> books) {
        mBookItems = books;
        notifyDataSetChanged();
    }
}
