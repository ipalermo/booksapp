
package com.zeelo.android.architecture.assignment.booksapp.addeditbook;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zeelo.android.architecture.assignment.booksapp.TestUtils;
import com.zeelo.android.architecture.assignment.booksapp.data.Book;
import com.zeelo.android.architecture.assignment.booksapp.data.FakeBooksRemoteDataSource;
import com.zeelo.android.architecture.assignment.booksapp.data.source.BooksRepository;
import com.zeelo.android.architecture.assignment.booksapp.util.EspressoIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import booksapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.zeelo.android.architecture.assignment.booksapp.R.id.toolbar;

/**
 * Tests for the add book screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEditBookScreenTest {

    private static final String TASK_ID = "1";

    /**
     * {@link IntentsTestRule} is an {@link ActivityTestRule} which inits and releases Espresso
     * Intents before and after each test run.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<AddEditBookActivity> mActivityTestRule =
            new ActivityTestRule<>(AddEditBookActivity.class, false, false);

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void emptyBook_isNotSaved() {
        // Launch activity to add a new book
        launchNewBookActivity(null);

        // Add invalid title and link combination
        onView(withId(R.id.add_book_title)).perform(clearText());
        onView(withId(R.id.add_book_description)).perform(clearText());
        // Try to save the book
        onView(withId(R.id.fab_edit_book_done)).perform(click());

        // Verify that the activity is still displayed (a correct book would close it).
        onView(withId(R.id.add_book_title)).check(matches(isDisplayed()));
    }

    @Test
    public void toolbarTitle_newBook_persistsRotation() {
        // Launch activity to add a new book
        launchNewBookActivity(null);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_book)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // Check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_book)));
    }

    @Test
    public void toolbarTitle_editBook_persistsRotation() {
        // Put a book in the repository and start the activity to edit it
        BooksRepository.destroyInstance();
        FakeBooksRemoteDataSource.getInstance().addBooks(new Book("Title1", "", TASK_ID, false));
        launchNewBookActivity(TASK_ID);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_book)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_book)));
    }

    /**
     * @param bookId is null if used to add a new book, otherwise it edits the book.
     */
    private void launchNewBookActivity(@Nullable String bookId) {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation()
                .getTargetContext(), AddEditBookActivity.class);

        intent.putExtra(AddEditBookFragment.ARGUMENT_EDIT_TASK_ID, bookId);
        mActivityTestRule.launchActivity(intent);
    }

    /**
     * Matches the toolbar title with a specific string resource.
     *
     * @param resourceId the ID of the string resource to match
     */
    public static Matcher<View> withToolbarTitle(final int resourceId) {
        return new BoundedMatcher<View, Toolbar>(Toolbar.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with toolbar title from resource id: ");
                description.appendValue(resourceId);
            }

            @Override
            protected boolean matchesSafely(Toolbar toolbar) {
                CharSequence expectedText = "";
                try {
                    expectedText = toolbar.getResources().getString(resourceId);
                } catch (Resources.NotFoundException ignored) {
                    /* view could be from a context unaware of the resource id. */
                }
                CharSequence actualText = toolbar.getTitle();
                return expectedText.equals(actualText);
            }
        };
    }
}
