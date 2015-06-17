package tw.binary.dipper;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

// Created by eason on 2015/1/29.
public class ResCalActivityDrawer extends DrawerBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_res_cal);
        //getActionBarToolbar();

        setupFragment();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_res_cal;
    }


    private void setupFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.calendar, new ResCalFragment());
        transaction.commit();
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_RES_CAL;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected void updateUi() {

    }
}
