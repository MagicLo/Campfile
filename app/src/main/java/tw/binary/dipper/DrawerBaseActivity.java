package tw.binary.dipper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

import tw.binary.dipper.consumer.WeatherActivity;
import tw.binary.dipper.message.CallerListActivity;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.LogUtils;
import tw.binary.dipper.util.LoginAndAuthHelper;

import static tw.binary.dipper.util.LoginAndAuthHelper.Callbacks;

// Created by eason on 2015/1/25.
public abstract class DrawerBaseActivity extends BaseActivity implements View.OnClickListener
        , View.OnLongClickListener, Callbacks {
    private static final String TAG = LogUtils.makeLogTag(DrawerBaseActivity.class);
    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth

    // Navigation drawer:
    private DrawerLayout mDrawerLayout;

    protected static final int NAVDRAWER_ITEM_HOME = 0;
    protected static final int NAVDRAWER_ITEM_RES_LIST = 1;
    protected static final int NAVDRAWER_ITEM_RES_CAL = 2;
    protected static final int NAVDRAWER_ITEM_PREFS = 3;
    protected static final int NAVDRAWER_ITEM_RES_EDIT = 4;
    //protected static final int NAVDRAWER_ITEM_GETMAPS = 5;
    protected static final int NAVDRAWER_ITEM_MESSAGE = 5;
    protected static final int NAVDRAWER_ITEM_WEATHER = 6;
    protected static final int NAVDRAWER_ITEM_ACCOUNT = 7;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_home,
            R.string.navdrawer_item_res_list,
            R.string.navdrawer_item_res_cal,
            R.string.navdrawer_item_prefs,
            R.string.navdrawer_item_res_edit,
            R.string.navdrawer_item_message,
            R.string.navdrawer_item_weather,
            R.string.navdrawer_item_account,
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_drawer_explore,       // Home
            R.drawable.ic_drawer_map,           //Resource List
            R.drawable.ic_drawer_my_schedule,   //Resource Calendar
            R.drawable.ic_drawer_settings,      //Prefs
            R.drawable.ic_drawer_experts,       //暫時測試用
            R.drawable.ic_drawer_experts,       //暫時測試用
            R.drawable.ic_drawer_experts,       //暫時測試用
            R.drawable.ic_drawer_account,
    };

    private boolean mActionBarShown = true;
    // Primary toolbar and drawer toggle
    protected Toolbar mActionBarToolbar;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();
    private ViewGroup mDrawerItemsListContainer;

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoginAndAuthHelper = new LoginAndAuthHelper(this, this);

        //轉場效果
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        //
        mHandler = new Handler();
        setContentView(getLayoutResource());
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
        getActionBarToolbar();

        // 確認是否還需再登入
        if (isAuthNeeded() && !mLoginAndAuthHelper.isUserLoggedIn()) {
            //啟動Google Identity Toolkit
            LogUtils.LOGD(TAG, "Creating and starting new Helper");
            mLoginAndAuthHelper.startSignIn();
        } else {
            updateUi();
        }
    }

    protected abstract int getLayoutResource();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mLoginAndAuthHelper.isUserLoggedIn()) setupAccountBox();  //畫面處理
        setupNavDrawer();
        //mDrawerToggle.syncState();
        /*
        trySetupSwipeRefresh();
        updateSwipeRefreshProgressBarTop(); //TODO CHeck it out

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            LOGW(TAG, "No view with ID main_content to fade in.");
        }
	*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupAccountBox() {
        ImageView profile_image = (ImageView) findViewById(R.id.profile_image);
        TextView profile_email_text = (TextView) findViewById(R.id.profile_email_text);
        TextView profile_name_text = (TextView) findViewById(R.id.profile_name_text);
        //調成圓形圖片
        Transformation transformation = new RoundedTransformationBuilder()
                .borderWidthDp(0)
                .cornerRadiusDp(50)
                .oval(false)
                .build();
        //Make sure PhotoURL is not empty
        String photoUrl = AccountUtils.getPhotoUrl(this);
        if (photoUrl != null && !photoUrl.equals("")) {
            Picasso.with(this)
                    .load(photoUrl)
                    .fit()
                    .transform(transformation)
                    .placeholder(R.drawable.person_image_empty)
                    .into(profile_image);
        } else if (mLoginAndAuthHelper.isUserLoggedIn()) {
            //No photo
            profile_image.setImageDrawable(getResources().getDrawable(R.drawable.person_image_empty));
        } else {
            profile_image.setVisibility(View.GONE);
        }
        profile_email_text.setText(AccountUtils.getEmail(this));
        profile_name_text.setText(AccountUtils.getDisplayName(this));
        // 顯示使用者Profile資訊區塊
        /*View chosenAccountView = findViewById(R.id.chosen_account_view);
        if(AccountUtils.getDisplayName(this) == null){
            chosenAccountView.setVisibility(View.GONE);
        }else{
            chosenAccountView.setVisibility(View.VISIBLE);
        }*/
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of DrawerBaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
     * different depending on whether the attendee indicated that they are attending the
     * event on-site vs. attending remotely.
     */
    private void setupNavDrawer() {
        // What nav drawer item should be selected?
        int selfItem = getSelfNavDrawerItem();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.theme_primary_dark));
        ScrollView navDrawer = (ScrollView) mDrawerLayout.findViewById(R.id.navdrawer);
        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            // do not show a nav drawer
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }
        //???
        if (navDrawer != null) {
            // 顯示使用者Profile資訊區塊
            final View chosenAccountView = findViewById(R.id.chosen_account_view);
            final View chosenAccountContentView = findViewById(R.id.chosen_account_content_view);
            final int navDrawerChosenAccountHeight = getResources().getDimensionPixelSize(
                    R.dimen.navdrawer_chosen_account_height);
        }

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.START);
                }
            });
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                //onNavDrawerStateChanged(false, false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        // populate the nav drawer with the correct items
        populateNavDrawer();

        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        /*
        if (!PrefUtils.isWelcomeDone(this)) {
            // first run of the app starts with the nav drawer open
            PrefUtils.markWelcomeDone(this);
            mDrawerLayout.openDrawer(Gravity.START);
        }
        */
    }

    /**
     * Populates the navigation drawer with the appropriate items.
     */
    private void populateNavDrawer() {
        //boolean attendeeAtVenue = PrefUtils.isAttendeeAtVenue(this);
        mNavDrawerItems.clear();

        mNavDrawerItems.add(NAVDRAWER_ITEM_HOME);
        //mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        mNavDrawerItems.add(NAVDRAWER_ITEM_RES_LIST);
        mNavDrawerItems.add(NAVDRAWER_ITEM_RES_CAL);
        mNavDrawerItems.add(NAVDRAWER_ITEM_PREFS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_RES_EDIT);
        mNavDrawerItems.add(NAVDRAWER_ITEM_MESSAGE);
        mNavDrawerItems.add(NAVDRAWER_ITEM_WEATHER);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ACCOUNT);

        createNavDrawerItems();
    }

    protected void goToNavDrawerItem(int item) {
        Intent intent;
        switch (item) {

            case NAVDRAWER_ITEM_HOME:
                intent = new Intent(this, HomeActivityDrawer.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_RES_LIST:
                intent = new Intent(this, ResListActivityDrawer.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_RES_CAL:
                intent = new Intent(this, ResCalActivityDrawer.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_PREFS:
                intent = new Intent(this, PrefsActivityDrawer.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_RES_EDIT:
                intent = new Intent(this, testGaeCursorAdapter.class);
                //intent = new Intent(this, ResListActivityDrawer.class);

                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_MESSAGE:
                intent = new Intent(this, CallerListActivity.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_WEATHER:
                intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
            case NAVDRAWER_ITEM_ACCOUNT:
                intent = new Intent(this, AccountActivityDrawer.class);
                startActivity(intent);
                finish();
                //System.exit(0);
                break;
        }
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        int layoutToInflate;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done，分隔線，所以可提前結束
            setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected abstract boolean isAuthNeeded();

    protected abstract void updateUi();

    //點選後圖&文會呈已選效果
    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);   //點選自己
            return;
        }

        // launch the target Activity after a short delay, to allow the close animation to play
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        // 效果非必要 change the active item on the list so the user can see the item changed
        setSelectedNavDrawerItem(itemId);
        // 效果非必要 fade out the main content
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        mDrawerLayout.closeDrawer(Gravity.START);
    }


    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START);
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    protected void onNavDrawerSlide(float offset) {
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (!mLoginAndAuthHelper.handleActivityResult(requestCode, resultCode, intent)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!mLoginAndAuthHelper.handleIntent(intent)) {
            // intent is handled by the GitkitClient.
            return;
        }
        super.onNewIntent(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    //Interface be called
    public void onAuthSuccess(IdToken idToken, GitkitUser gitkitUser) {
        setupAccountBox();
        //檢查是否已填基本資料
        if (AccountUtils.hasAccountInfo(this)) {
            updateUi();
        } else {
            Intent intent = new Intent(this, AccountActivityDrawer.class);
            startActivity(intent);
            finish();
        }
    }

    //Interface be called
    public void onAuthFailure() {
        setupAccountBox();
        goToNavDrawerItem(0);   //跳至首個Activity
        finish();
    }

    //顯示關於我資訊
    public void displayAbout() {
        // Build the about body view and append the link to see OSS licenses
        SpannableStringBuilder aboutBody = new SpannableStringBuilder();
        aboutBody.append(Html.fromHtml(getString(R.string.splash_dialog_body)));

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.dialog_about, null);
        aboutBodyView.setText(aboutBody);
        aboutBodyView.setMovementMethod(new LinkMovementMethod());
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.splash_dialog_title))
                .setView(aboutBodyView)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.finish:
                finish();
                break;
            case R.id.logout:
                AccountUtils.clearLoggedInUser(this);
                onAuthFailure();
                break;
            case R.id.about:
                displayAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //動態調整Menu顯示
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.logout);
        if (item != null) {
            if (mLoginAndAuthHelper.isUserLoggedIn()) {
                menu.findItem(R.id.logout).setVisible(true);
            } else {
                menu.findItem(R.id.logout).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
