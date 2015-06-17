package tw.binary.dipper;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

// Created by eason on 2015/1/29.
public class PrefsActivityDrawer extends DrawerBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_prefs);
        //getActionBarToolbar();

        setupFragment();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_prefs;
    }

    private void setupFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, new PrefsFragment());
        transaction.commit();
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_PREFS;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected void updateUi() {

    }
}
