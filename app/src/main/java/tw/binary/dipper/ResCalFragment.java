package tw.binary.dipper;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static tw.binary.dipper.R.drawable.tent;

public class ResCalFragment extends BaseFragment {
    private boolean undo = false;
    private CaldroidFragment caldroidFragment;
    private FragmentActivity mContext;

    //未來會拿掉newInstance
    public ResCalFragment newInstance() {
        ResCalFragment fragment = new ResCalFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ResCalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

        View rootView = inflater.inflate(R.layout.fragment_res_cal, container, false);
        // Setup caldroid fragment
        caldroidFragment = new CaldroidFragment();
        // Setup arguments

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);

            // Uncomment this to customize startDayOfWeek
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.SUNDAY); // Tuesday

            // Uncomment this line to use Caldroid in compact mode
            //args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            caldroidFragment.setArguments(args);
        }
        setCustomResourceForDates();

        // Attach to the activity
        FragmentManager fragmentManager = mContext.getSupportFragmentManager();
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        // Caldroid inform clients via CaldroidListener.
        // TODO 點選後顯示預約營地列表頁，要用AsyncTask處理
        caldroidFragment.setCaldroidListener(new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                Toast.makeText(mContext, formatter.format(date), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Toast.makeText(mContext, "Long click " + formatter.format(date), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
                Toast.makeText(mContext, "Caldroid view is created", Toast.LENGTH_SHORT).show();
            }

        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setCustomResourceForDates() {
        Calendar cal = Calendar.getInstance();

        // Min date is last 7 days
        cal.add(Calendar.DATE, -18);
        Date blueDate = cal.getTime();

        // Max date is next 7 days
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 16);
        Date greenDate = cal.getTime();

        if (caldroidFragment != null) {
            caldroidFragment.setBackgroundResourceForDate(R.color.blue, blueDate);
            caldroidFragment.setBackgroundResourceForDate(tent, greenDate);
            //caldroidFragment.setBackgroundResourceForDate(R.color.green, greenDate);
            caldroidFragment.setTextColorForDate(R.color.white, blueDate);
            caldroidFragment.setTextColorForDate(R.color.white, greenDate);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }
}
