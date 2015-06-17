package tw.binary.dipper;

import android.app.Fragment;
import android.os.Bundle;

public abstract class BaseFragment extends Fragment {
    //The fragment argument representing the section number for this fragment.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);    //如果有Menu要啟用
        super.onCreate(savedInstanceState);
    }


}
