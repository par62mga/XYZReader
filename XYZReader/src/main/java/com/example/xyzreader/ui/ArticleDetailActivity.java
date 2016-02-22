package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 *
 * Changed ActionBarActivity to AppCompatActivity to support AppCompat theme
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private String LOG_TAG = ArticleDetailActivity.class.getSimpleName();

    private Cursor mCursor;

    private long mStartId;          // starting database _ID
    private long mSelectedItemId;   // current database _ID

    private int  mStartPosition;    // cursor/pager position, used to handle transitions
    private int  mSelectedPosition; // current position

    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;

    private ArticleDetailFragment mSelectedDetailsFragment;

    private boolean mHandleTransition; // true if handling postponed transition
    private boolean mIsReturning;      // true when handling return transition

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mHandleTransition = false;
        } else {
            mHandleTransition = true;
            postponeEnterTransition();
            setEnterSharedElementCallback (new SharedElementCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (mIsReturning &&
                            mStartPosition != mSelectedPosition &&
                            mSelectedDetailsFragment != null) {
                        View sharedElement = mSelectedDetailsFragment.getPhotoView();

                        // If the user has swiped to a different ViewPager page, then we need to
                        // remove the old shared element and replace it with the new shared element
                        // that should be transitioned instead.
                        names.clear();
                        names.add(sharedElement.getTransitionName());
                        sharedElements.clear();
                        sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                    }
                }
            });
        }

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.page_margin), getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(R.color.page_margin)));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    mSelectedPosition = position;
                    mSelectedItemId   = mCursor.getLong(ArticleLoader.Query._ID);
                }
                updateUpButtonPosition();
            }
        });

        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // changing this to a back press, results in return animation working properly
                onBackPressed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    mTopInset = windowInsets.getSystemWindowInsetTop();
                    mUpButtonContainer.setTranslationY(mTopInset);
                    updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mSelectedItemId = mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }

        mSelectedDetailsFragment = null;
        mIsReturning = false;
    }

    @Override
    public void onEnterAnimationComplete () {
        super.onEnterAnimationComplete();
        Log.d(LOG_TAG, "onEnterAnimationComplete()");
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        Log.d (LOG_TAG, "finishAfterTransition() mStartPosition ==> " +
                String.valueOf(mStartPosition) + " mSelectedPosition ==> " +
                String.valueOf(mSelectedPosition));
        data.putExtra(ArticleListActivity.EXTRA_STARTING_ID, mStartPosition);
        data.putExtra(ArticleListActivity.EXTRA_SELECTED_ID, mSelectedPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    //prefetch image to speed up transition...
                    ImageLoaderHelper.getInstance(this).getImageLoader()
                            .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                                    new ImageLoader.ImageListener() {
                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                        }

                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                        }
                                    });
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    mStartPosition = mSelectedPosition = position;
                    //Log.d (LOG_TAG, "onLoadFinished() transition start/selected position ==> " +
                            //String.valueOf(position));
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {
        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mSelectedDetailsFragment = (ArticleDetailFragment) object;
            if (mSelectedDetailsFragment != null) {
                mSelectedItemUpButtonFloor = mSelectedDetailsFragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            Long itemId = mCursor.getLong(ArticleLoader.Query._ID);
            //Log.d (LOG_TAG, "getItem() transition" +
            //    " itemId ==> " + String.valueOf(itemId) +
            //    " mStartId ==> " + String.valueOf(mStartId) +
            //    " mHandleTransition == " + String.valueOf(mHandleTransition));
            return ArticleDetailFragment.newInstance(
                    itemId,
                    position,
                    mHandleTransition && (mStartId == itemId));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
