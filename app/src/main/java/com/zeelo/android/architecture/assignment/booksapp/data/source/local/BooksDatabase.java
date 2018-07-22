
package com.zeelo.android.architecture.assignment.booksapp.data.source.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.source.local.converter.StringListConverter;

/**
 * The Room Database that contains the Book table.
 */
@Database(entities = {Book.class}, version = 1)
@TypeConverters({StringListConverter.class})
public abstract class BooksDatabase extends RoomDatabase {

    private static BooksDatabase INSTANCE;

    public abstract BooksDao bookDao();

    private static final Object sLock = new Object();

    public static BooksDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        BooksDatabase.class, "Books.db")
                        .build();
            }
            return INSTANCE;
        }
    }

}
