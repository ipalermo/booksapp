
package com.zeelo.android.architecture.assignment.booksapp.books;

import android.databinding.BindingAdapter;
import android.widget.ListView;

import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;

import java.util.List;

/**
 * Contains {@link BindingAdapter}s for the {@link BookListItem} list.
 */
public class BooksListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(ListView listView, List<BookListItem> items) {
        BooksAdapter adapter = (BooksAdapter) listView.getAdapter();
        if (adapter != null)
        {
            adapter.replaceData(items);
        }
    }
}
