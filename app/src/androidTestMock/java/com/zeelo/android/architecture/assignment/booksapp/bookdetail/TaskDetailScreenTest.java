
package com.zeelo.android.architecture.assignment.booksapp.bookdetail;

import android.app.Activity;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.zeelo.android.architecture.assignment.booksapp.Injection;
import com.zeelo.android.architecture.assignment.booksapp.TestUtils;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.FakeBooksRemoteDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;
import com.zeelo.android.architecture.assignment.booksapp.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import booksapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the books screen, the main screen which contains a list of all books.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BookDetailScreenTest {

    private static String TASK_TITLE = "ATSL";

    private static String TASK_DESCRIPTION = "Rocks";

    /**
     * {@link Book} stub that is added to the fake service API layer.
     */
    private static Book ACTIVE_BOOK = new Book(TASK_TITLE, TASK_DESCRIPTION, false);

    /**
     * {@link Book} stub that is added to the fake service API layer.
     */
    private static Book COMPLETED_BOOK = new Book(TASK_TITLE, TASK_DESCRIPTION, true);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target
     * Activity.
     */
    @Rule
    public ActivityTestRule<BookDetailActivity> mBookDetailActivityTestRule =
            new ActivityTestRule<>(BookDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);

    private void loadActiveBook() {
        startActivityWithWithStubbedBook(ACTIVE_BOOK);
    }

    private void loadCompletedBook() {
        startActivityWithWithStubbedBook(COMPLETED_BOOK);
    }

    /**
     * Setup your test fixture with a fake book id. The {@link BookDetailActivity} is started with
     * a particular book id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private void startActivityWithWithStubbedBook(Book book) {
        // Add a book stub to the fake service api layer.
        BooksRepository booksRepository = Injection.provideBooksRepository(InstrumentationRegistry.getTargetContext());
        booksRepository.deleteAllBooks();
        FakeBooksRemoteDataSource.getInstance().addBooks(book);

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        startIntent.putExtra(BookDetailActivity.EXTRA_TASK_ID, book.getId());
        mBookDetailActivityTestRule.launchActivity(startIntent);
    }

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void activeBookDetails_DisplayedInUi() throws Exception {
        loadActiveBook();

        // Check that the book title and link are displayed
        onView(withId(R.id.book_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.book_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.book_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedBookDetails_DisplayedInUi() throws Exception {
        loadCompletedBook();

        // Check that the book title and link are displayed
        onView(withId(R.id.book_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.book_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.book_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndBookPersist() {
        loadActiveBook();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mBookDetailActivityTestRule.getActivity());

        // Check that the book is shown
        onView(withId(R.id.book_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.book_detail_description)).check(matches(withText(TASK_DESCRIPTION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }
}
