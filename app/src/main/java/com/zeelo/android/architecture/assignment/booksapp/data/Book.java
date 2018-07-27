package com.zeelo.android.architecture.assignment.booksapp.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Immutable model class for a Book.
 */
@Entity(tableName = "book",
        foreignKeys = {
                @ForeignKey(entity = BookListItem.class,
                        parentColumns = "id",
                        childColumns = "id",
                        onDelete = ForeignKey.CASCADE)
        })
public final class Book {

    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private String id;

    @Nullable
    @Embedded
    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    @ColumnInfo(name = "favorite")
    private boolean mFavorite;

    public Book(){}

    /**
     * Use this constructor to create a new Book.
     *
     * @param title title of the book
     * @param description  description of the book
     */
    @Ignore
    public Book(@Nullable String title, @Nullable String description) {
        this(title, UUID.randomUUID().toString(), description, false);
    }

    /**
     * Use this constructor to create a Book if the Book already has an id (copy of another
     * Book)
     *
     * @param title title of the book
     * @param id    id of the book
     * @param description  description of the book
     */
    @Ignore
    public Book(@Nullable String title, @NonNull String id, @Nullable String description) {
        this(title, id, description, false);
    }

    /**
     * Use this constructor to create a Book if the Book already has an id (copy of another
     * Book).
     *
     * @param title title of the book
     * @param id    id of the book
     * @param description  description of the book
     * @param favorite   true if the book is favorite
     */
    public Book(@Nullable String title, @NonNull String id, @Nullable String description, boolean favorite) {
        this.id = id;
        this.volumeInfo = new VolumeInfo(title);
        this.volumeInfo.setDescription(description);
        this.mFavorite = favorite;
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
    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(@Nullable VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }


    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.mFavorite = favorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
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
        @SerializedName("description")
        private String description;

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
        public String getDescription() {
            return description;
        }

        public void setDescription(@Nullable String description) {
            this.description = description;
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
