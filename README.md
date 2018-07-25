# Zeelo Books app

This sample app is called Zeelo Books. It follows a mvvm architecture and it uses some Architecture Components like ViewModel, LiveData, and the Data Binding Library to display data and bind UI elements to actions.

## Project Setup

In order to build and run the app, you should clone from the master branch and make sure you have installed the following dependencies:


Android SDK 28
The Zeelo Books app uses the following dependencies:

* [Android Studio 3.2 or above](https://developer.android.com/studio/preview/)
* Android SDK 28

The app uses the following dependencies:

* [Common Android support libraries](https://developer.android.com/topic/libraries/support-library/index.html) -  Packages in the com.android.support.* namespace provide backwards compatibility and other features.
* [Android Testing Support Library](https://developer.android.com/topic/libraries/testing-support-library/index.html) -  Framework used to support UI tests, using both Espresso, and AndroidJUnitRunner.
* [Mockito](http://site.mockito.org/) - A mocking framework used to implement unit tests.
* [Guava](https://github.com/google/guava) - A set of core libraries for Java by Google, commonly used in Android apps.

## Designing the app

The app consists of four UI screens:

* Books - Used to manage a list of books.
* BookDetail - Used to read or delete a book.
* AddEditBook - Used to create or edit books.

The app uses [product flavors](https://developer.android.com/studio/build/build-variants.html) to replace modules at compile time, providing fake data for both manual and automated testing.
The data is stored locally in a SQLite database, using [Room](https://developer.android.com/topic/libraries/architecture/room.html).

This app uses fragments, and this is for two reasons:

* The use of both [activities](https://developer.android.com/guide/components/activities/index.html) and [fragments](https://developer.android.com/guide/components/fragments.html) allows for a better separation of concerns which complements this implementation of MVP. In this version of the app, the Activity is the overall controller which creates and connects views and presenters.
* The use of fragments supports tablet layouts or UI screens with multiple views.

The app includes a number of unit tests which cover presenters, repositories, and data sources. Also includes UI tests, that rely on fake data, and are facilitated by dependency injection to provide fake modules. For more information on using dependency injection to facilitate testing, see Leveraging product flavors in Android Studio for hermetic testing.

### Live events

The class `SingleLiveEvent`(which extends `MutableLiveData` so it's lifecycle-aware), it's used for communication between ViewModels and UI views (activities and fragments).

Instead of holding data, it dispatches data once. This is important to prevent events being fired after a rotation, for example.

There is no reference to the View from a ViewModel so the communication between them must happen via a subscription. ViewModels expose
events like `openTaskEvent` and views subscribe to them. For example:

```java
private void subscribeToNavigationChanges(BookDetailViewModel viewModel) {
    // The activity observes the navigation commands in the ViewModel
    viewModel.getEditBookCommand().observe(this, new Observer<Void>() {
        @Override
        public void onChanged(@Nullable Void _) {
            BookDetailActivity.this.onStartEditBook();
        }
    });
    viewModel.getDeleteBookCommand().observe(this, new Observer<Void>() {
        @Override
        public void onChanged(@Nullable Void _) {
            BookDetailActivity.this.onBookDeleted();
        }
    });
}
```
### Using ViewModels in bindings with the Data Binding Library
ViewModels are used to show data of a particular screen, but they don't handle user actions. For that it's much more convenient to create user actions listeners or even presenters
that hold no state during configuration changes and hence are easy to recreate. See `TaskItemUserActionsListener` for an example.

## Oportunities for improvement/Tech debt

### Repository does not use LiveData
For simplicity, the repository does not use LiveData to expose its data(hopefully will be added later).

### Caching Strategy and paging
For time constraints, the app deos not take advantage of the latest Android Paging Architecture Component which helps to implement efficient paging from cache and network.
