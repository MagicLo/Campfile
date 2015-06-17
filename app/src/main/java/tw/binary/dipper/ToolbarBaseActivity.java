package tw.binary.dipper;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

// Created by eason on 2015/2/21.
public abstract class ToolbarBaseActivity extends BaseActivity {
    protected Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        setContentView(getLayoutResource());
        getActionBarToolbar();
    }

    protected abstract int getLayoutResource();

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null)
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (mActionBarToolbar != null)
            setSupportActionBar(mActionBarToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
        return mActionBarToolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
