
package com.zeelo.android.architecture.assignment.booksapp.statistics;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.zeelo.android.architecture.assignment.booksapp.Injection;
import com.zeelo.android.architecture.assignment.booksapp.ViewModelFactory;
import com.zeelo.android.architecture.assignment.booksapp.bookdetail.BookDetailActivity;
import com.zeelo.android.architecture.assignment.booksapp.data.BookListItem;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;
import com.zeelo.android.architecture.assignment.booksapp.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.zeelo.android.architecture.assignment.booksapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests for the statistics screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class StatisticsScreenTest {

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<StatisticsActivity> mStatisticsActivityTestRule =
            new ActivityTestRule<>(StatisticsActivity.class, true, false);

    /**
     * Setup your test fixture with a fake book id. The {@link BookDetailActivity} is started with
     * a particular book id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    @Before
    public void intentWithStubbedBookId() {
        // Given some books
        ViewModelFactory.destroyInstance();
        BooksRepository booksRepository = Injection.provideBooksRepository(
                InstrumentationRegistry.getTargetContext());
        booksRepository.deleteAllBooks();
        booksRepository.saveBook(new BookListItem("Title1", "", false));
        booksRepository.saveBook(new BookListItem("Title2", "", true));

        // Lazily start the Activity from the ActivityTestRule
        Intent startIntent = new Intent();
        mStatisticsActivityTestRule.launchActivity(startIntent);
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
    public void Books_ShowsNonEmptyMessage() throws Exception {
        // Check that the active and favorited books text is displayed
        String expectedActiveBookText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.statistics_active_books, 1);
        onView(withText(containsString(expectedActiveBookText))).check(matches(isDisplayed()));
        String expectedCompletedBookText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.statistics_completed_books, 1);
        onView(withText(containsString(expectedCompletedBookText))).check(matches(isDisplayed()));
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }
}
