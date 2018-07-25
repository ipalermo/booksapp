package com.zeelo.android.architecture.assignment.booksapp.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.UUID;

import static com.zeelo.android.architecture.assignment.booksapp.data.source.remote.BooksRemoteDataSource.BOOK_DETAILS_API_PATH;

/**
 * Immutable model class for a BookListItem.
 */
@Entity(tableName = "bookslist")
public final class BookListItem {

    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private String id;

    @Nullable
    private String link;

    @Nullable
    @Embedded
    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    public BookListItem(){}

    /**
     * Use this constructor to create a new BookListItem.
     *
     * @param title title of the book
     */
    @Ignore
    public BookListItem(@Nullable String title) {
        this(title, UUID.randomUUID().toString());
    }

    @Ignore
    public BookListItem(@Nullable String title, @NonNull String id) {
        this.id = id;
        this.volumeInfo = new VolumeInfo(title);
        this.link = BOOK_DETAILS_API_PATH + this.id;
    }

    /**
     * Use this constructor to create a BookListItem if the BookListItem already has an id (copy of another
     * BookListItem).
     *
     * @param title title of the book
     * @param id    id of the book
     * @param link  link to the book details
     */
    public BookListItem(@Nullable String title, @NonNull String id, @Nullable String link) {
        this.id = id;
        this.volumeInfo = new VolumeInfo(title);
        this.link = link;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public String getTitle() {
        return volumeInfo.title;
    }

    public void setTitle(String title) {
        this.volumeInfo.title = title;
    }

    @Nullable
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Nullable
    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(@Nullable VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookListItem book = (BookListItem) o;
        return Objects.equal(id, book.id) &&
                Objects.equal(volumeInfo.title, book.volumeInfo.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, volumeInfo.title);
    }

    @Override
    public String toString() {
        return "BookListItem with title " + volumeInfo.title;
    }

    public static class VolumeInfo {

        @Nullable
        @ColumnInfo(name = "title")
        private String title;

        @Nullable
        @ColumnInfo(name = "authors")
        private ArrayList<String> authors;

        @Nullable
        @Embedded
        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        @Ignore
        public VolumeInfo(String title) {
            this(title, new ArrayList<String>());
        }

        public VolumeInfo(@Nullable String title, @Nullable ArrayList<String> authors) {
            this.title = title;
            this.authors = authors;
        }

        @Nullable
        public String getTitle() {
            return title;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        @Nullable
        public ArrayList<String> getAuthors() {
            return authors;
        }

        public void setAuthors(@Nullable ArrayList<String> authors) {
            this.authors = authors;
        }

        @Nullable
        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public void setImageLinks(@Nullable ImageLinks imageLinks) {
            this.imageLinks = imageLinks;
        }

        public static class ImageLinks {

            @Nullable
            @ColumnInfo(name = "thumbnail")
            private String thumbnail;

            @Nullable
            public String getThumbnail() {
                return thumbnail;
            }

            public void setThumbnail(@Nullable String thumbnail) {
                this.thumbnail = thumbnail;
            }
        }
    }
}
