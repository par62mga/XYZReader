# XYZReader

## Background

XYZ Reader is Project #5 in the Android Developer Nanodegree program.

The app is a mock RSS feed reader featuring banner photos and shocking headlines!

The project was to take the user feedback in the UI Review node, and implement changes that will improve the UI and make it conform to Material Design.

## Required Behavior

- App uses the Design Support library and its provided widget types (FloatingActionButton, AppBarLayout, SnackBar, etc). **See build.gradle and layout xml files**
	
- App uses CoordinatorLayout for the main activity.**See activity-article-list.xml and ArticleListActivity.java**

- App theme extends from AppCompat. **See AndroidManifest.xml, styles.xml, ArticleListActivity.jaga and ArticleDetailActivity.java**

- App uses an app bar and associated toolbars. **See activity-article-list.xml and ArticleListActivity.java**

- App provides a Floating Action Button (FAB) for the most common action(s). **See fragment-article-detail.xml and ArticleDetailFragment.java**

- App properly specifies elevations for app bars, FABs, and other elements specified in the Material Design specification. **See layout xml files and ArticleListActivity.java**

- App provides sufficient space between text and surrounding elements. **See layout xml files**

- App has a consistent color theme defined in styles.xml. **See styles.xml**

- Color theme does not impact usability of the app.**As defined in styles.xml**

- App uses images that are high quality, specific, and full bleed. **See fragment-article-detail.xml and ArticleDetailFragment.java**

- App uses fonts that are either the Android defaults, are complementary, and aren't otherwise distracting. **Removed use of non-standard font in ArticleDetailFragment.java**

- App conforms to common standards found in the Android Nanodegree General Project Guidelines

## Other Notes

- Implemented shared element transitions between the list activity and the detail activity. This implementation was based on information found in the Udacity discussion forum and the very useful post below:

[http://stackoverflow.com/questions/27304834/viewpager-fragments-shared-element-transitions/27321077#27321077](http://stackoverflow.com/questions/27304834/viewpager-fragments-shared-element-transitions/27321077#27321077 "ViewPager Fragments - Shared Element Transitions")

- Used Sunshine as a reference to help develop the CoordinatorLayout and to also update Toolbar elevation independently of the recycler view