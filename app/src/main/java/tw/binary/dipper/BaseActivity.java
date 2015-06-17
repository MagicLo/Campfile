package tw.binary.dipper;

import android.support.v7.app.ActionBarActivity;

import tw.binary.dipper.util.LoginAndAuthHelper;


/**
 * Created by eason on 2015/4/13.
 */
public class BaseActivity extends ActionBarActivity {
    protected static final int CustMode = 1;
    protected static final int OwnerMode = 2;
    protected static final int AppMode = OwnerMode;
    protected LoginAndAuthHelper mLoginAndAuthHelper;
}
