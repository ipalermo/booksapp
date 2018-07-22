
package com.zeelo.android.architecture.assignment.booksapp.books;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.zeelo.android.architecture.assignment.booksapp.Injection;
import com.zeelo.android.architecture.assignment.booksapp.TestUtils;
import com.zeelo.android.architecture.assignment.booksapp.ViewModelFactory;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import booksapp.R;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkArgument;
import static com.zeelo.android.architecture.assignment.booksapp.TestUtils.getCurrentActivity;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the books screen, the main screen which contains a list of all books.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BooksScreenTest {

    private final static String TITLE1 = "TITLE1";

    private final static String DESCRIPTION = "DESCR";

    private final static String TITLE2 = "TITLE2";

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<BooksActivity> mBooksActivityTestRule =
            new ActivityTestRule<>(BooksActivity.class);

    @Before
    public void resetState() {
        ViewModelFactory.destroyInstance();
        Injection.provideBooksRepository(InstrumentationRegistry.getTargetContext())
                .deleteAllBooks();
    }

    /**
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickAddBookButton_opensAddBookUi() {
        // Click on the add book button
        onView(withId(R.id.fab_add_book)).perform(click());

        // Check if the add book screen is displayed
        onView(withId(R.id.add_book_title)).check(matches(isDisplayed()));
    }

    @Test
    public void editBook() throws Exception {
        // First add a book
        createBook(TITLE1, DESCRIPTION);

        // Click on the book on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the edit book button
        onView(withId(R.id.fab_edit_book)).perform(click());

        String editBookTitle = TITLE2;
        String editBookDescription = "New Description";

        // Edit book title and link
        onView(withId(R.id.add_book_title))
                .perform(replaceText(editBookTitle), closeSoftKeyboard()); // Type new book title
        onView(withId(R.id.add_book_description)).perform(replaceText(editBookDescription),
                closeSoftKeyboard()); // Type new book link and close the keyboard

        // Save the book
        onView(withId(R.id.fab_edit_book_done)).perform(click());

        // Verify book is displayed on screen in the book list.
        onView(withItemText(editBookTitle)).check(matches(isDisplayed()));

        // Verify previous book is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void addBookToBooksList() throws Exception {
        createBook(TITLE1, DESCRIPTION);

        // Verify book is displayed on screen
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markBookAsComplete() {
        viewAllBooks();

        // Add active book
        createBook(TITLE1, DESCRIPTION);

        // Mark the book as complete
        clickCheckBoxForBook(TITLE1);

        // Verify book is shown as complete
        viewAllBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveBooks();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        viewCompletedBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markBookAsActive() {
        viewAllBooks();

        // Add favorited book
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);

        // Mark the book as active
        clickCheckBoxForBook(TITLE1);

        // Verify book is shown as active
        viewAllBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewCompletedBooks();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showAllBooks() {
        // Add 2 active books
        createBook(TITLE1, DESCRIPTION);
        createBook(TITLE2, DESCRIPTION);

        //Verify that all our books are shown
        viewAllBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showActiveBooks() {
        // Add 2 active books
        createBook(TITLE1, DESCRIPTION);
        createBook(TITLE2, DESCRIPTION);

        //Verify that all our books are shown
        viewActiveBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showCompletedBooks() {
        // Add 2 favorited books
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);
        createBook(TITLE2, DESCRIPTION);
        clickCheckBoxForBook(TITLE2);

        // Verify that all our books are shown
        viewCompletedBooks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void clearCompletedBooks() {
        viewAllBooks();

        // Add 2 complete books
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);
        createBook(TITLE2, DESCRIPTION);
        clickCheckBoxForBook(TITLE2);

        // Click clear favorited in menu
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.menu_clear)).perform(click());

        //Verify that favorited books are not shown
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        onView(withItemText(TITLE2)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createOneBook_deleteBook() {
        viewAllBooks();

        // Add active book
        createBook(TITLE1, DESCRIPTION);

        // Open it in details view
        onView(withText(TITLE1)).perform(click());

        // Click delete book in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify it was deleted
        viewAllBooks();
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createTwoBooks_deleteOneBook() {
        // Add 2 active books
        createBook(TITLE1, DESCRIPTION);
        createBook(TITLE2, DESCRIPTION);

        // Open the second book in details view
        onView(withText(TITLE2)).perform(click());

        // Click delete book in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify only one book was deleted
        viewAllBooks();
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(TITLE2)).check(doesNotExist());
    }

    @Test
    public void markBookAsCompleteOnDetailScreen_bookIsCompleteInList() {
        viewAllBooks();

        // Add 1 active book
        createBook(TITLE1, DESCRIPTION);

        // Click on the book on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in book details screen
        onView(withId(R.id.book_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the book is marked as favorited
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void markBookAsActiveOnDetailScreen_bookIsActiveInList() {
        viewAllBooks();

        // Add 1 favorited book
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);

        // Click on the book on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in book details screen
        onView(withId(R.id.book_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the book is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markBookAsAcompleteAndActiveOnDetailScreen_bookIsActiveInList() {
        viewAllBooks();

        // Add 1 active book
        createBook(TITLE1, DESCRIPTION);

        // Click on the book on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in book details screen
        onView(withId(R.id.book_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.book_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the book is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markBookAsActiveAndCompleteOnDetailScreen_bookIsCompleteInList() {
        viewAllBooks();

        // Add 1 favorited book
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);

        // Click on the book on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in book details screen
        onView(withId(R.id.book_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.book_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the book is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_FilterActivePersists() {

        // Add a favorited book
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);

        // when switching to active books
        viewActiveBooks();

        // then no books should appear
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));

        // when rotating the screen
        TestUtils.rotateOrientation(mBooksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void orientationChange_FilterCompletedPersists() {

        // Add a favorited book
        createBook(TITLE1, DESCRIPTION);
        clickCheckBoxForBook(TITLE1);

        // when switching to favorited books
        viewCompletedBooks();

        // the favorited book should be displayed
        onView(withText(TITLE1)).check(matches(isDisplayed()));

        // when rotating the screen
        TestUtils.rotateOrientation(mBooksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(R.string.label_completed)).check(matches(isDisplayed()));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_ChangePersists() throws Throwable {
        // Add a favorited book
        createBook(TITLE1, DESCRIPTION);

        // Open the book in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit book button
        onView(withId(R.id.fab_edit_book)).perform(click());

        // Change book title (but don't save)
        onView(withId(R.id.add_book_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new book title

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Verify book title is restored
        onView(withId(R.id.add_book_title)).check(matches(withText(TITLE2)));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_NoDuplicate() throws IllegalStateException {
        // Add a favorited book
        createBook(TITLE1, DESCRIPTION);

        // Open the book in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit book button
        onView(withId(R.id.fab_edit_book)).perform(click());

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit book title and link
        onView(withId(R.id.add_book_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new book title
        onView(withId(R.id.add_book_description)).perform(replaceText(DESCRIPTION),
                closeSoftKeyboard()); // Type new book link and close the keyboard

        // Save the book
        onView(withId(R.id.fab_edit_book_done)).perform(click());

        // Verify book is displayed on screen in the book list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));

        // Verify previous book is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void noBooks_AllBooksFilter_AddBookViewVisible() {
        // Given an empty list of books, make sure "All books" filter is on
        viewAllBooks();

        // Add book View should be displayed
        onView(withId(R.id.noBooksAdd)).check(matches(isDisplayed()));
    }

    @Test
    public void noBooks_CompletedBooksFilter_AddBookViewNotVisible() {
        // Given an empty list of books, make sure "All books" filter is on
        viewCompletedBooks();

        // Add book View should be displayed
        onView(withId(R.id.noBooksAdd)).check(matches(not(isDisplayed())));
    }

    @Test
    public void noBooks_ActiveBooksFilter_AddBookViewNotVisible() {
        // Given an empty list of books, make sure "All books" filter is on
        viewActiveBooks();

        // Add book View should be displayed
        onView(withId(R.id.noBooksAdd)).check(matches(not(isDisplayed())));
    }

    private void viewAllBooks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_all)).perform(click());
    }

    private void viewActiveBooks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_active)).perform(click());
    }

    private void viewCompletedBooks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_completed)).perform(click());
    }

    private void createBook(String title, String description) {
        // Click on the add book button
        onView(withId(R.id.fab_add_book)).perform(click());

        // Add book title and link
        onView(withId(R.id.add_book_title)).perform(typeText(title),
                closeSoftKeyboard()); // Type new book title
        onView(withId(R.id.add_book_description)).perform(typeText(description),
                closeSoftKeyboard()); // Type new book link and close the keyboard

        // Save the book
        onView(withId(R.id.fab_edit_book_done)).perform(click());
    }

    private void clickCheckBoxForBook(String title) {
        onView(allOf(withId(R.id.complete), hasSibling(withText(title)))).perform(click());
    }

    private String getText(int stringId) {
        return mBooksActivityTestRule.getActivity().getResources().getString(stringId);
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                mBooksActivityTestRule.getActivity(), R.id.toolbar);
    }
}
