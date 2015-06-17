package tw.binary.dipper.message;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.HashMap;

import tw.binary.dipper.R;
import tw.binary.dipper.util.AccountUtils;
import tw.binary.dipper.util.MyUtils;
import tw.binary.dipper.util.MyUtilsDate;

/* Created by eason on 2015/5/11. */
/* 本Fragment是為了顯示Chat內容用的 */
public class MessageFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;

    //Parent Activity需要實作的Method，傳入寄件者的LocalID
    public interface OnFragmentInteractionListener {
        HashMap getFromLocalInfo();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;   //
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.chat_list_item,    //此Fragment用的Layout
                null,
                new String[]{MessageDbHelper.MESSAGE_COL_MSG    //真實對應
                        , MessageDbHelper.MESSAGE_COL_SENT      //真實對應
                        , MessageDbHelper.MESSAGE_COL_PHOTOURL  //對應欄位隨便給
                        , MessageDbHelper.MESSAGE_COL_PHOTOURL
                        , MessageDbHelper.MESSAGE_COL_MSG},//對應欄位隨便給
                new int[]{R.id.tvMessage
                        , R.id.tvSentTime
                        , R.id.ivPhotoLeft
                        , R.id.ivPhotoRight
                        , R.id.llMessage},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                Transformation transformation = new RoundedTransformationBuilder()
                        .borderWidthDp(0)
                        .cornerRadiusDp(50)
                        .oval(false)
                        .build();

                switch (view.getId()) {
                    case R.id.llMessage:
                        //訊息窗格
                        RelativeLayout.LayoutParams llMessageLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        if (cursor.getString(cursor.getColumnIndex(MessageDbHelper.MESSAGE_COL_DISPLAYNAME)) == null) {
                            //Owner Side : Right
                            llMessageLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.ivPhotoRight);
                            llMessageLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
                        } else {
                            //Customer Side : Left
                            llMessageLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.ivPhotoLeft);
                            llMessageLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);
                        }
                        view.setLayoutParams(llMessageLayoutParams);
                        return true;
                    case R.id.tvMessage:
                        //訊息內容
                        ((TextView) view).setText(cursor.getString(columnIndex));
                        //RelativeLayout root = (RelativeLayout) view.getParent().getParent();
                        RelativeLayout.LayoutParams tvMessageLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        //判斷靠左或靠右
                        if (cursor.getString(cursor.getColumnIndex(MessageDbHelper.MESSAGE_COL_DISPLAYNAME)) == null) {
                            tvMessageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            tvMessageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                            //檢查版本
                            int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.message_box_origin));
                            } else {
                                view.setBackground(getResources().getDrawable(R.drawable.message_box_origin));
                            }
                        } else {
                            tvMessageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            tvMessageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                            //檢查版本
                            int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.message_box_origin));
                            } else {
                                view.setBackground(getResources().getDrawable(R.drawable.message_box));
                            }
                        }
                        view.setLayoutParams(tvMessageLayoutParams);
                        return true;    //true由App處理，fasle由OS處理
                    case R.id.tvSentTime:
                        //傳送時間
                        TextView tvSentTime = (TextView) view;
                        tvSentTime.setText(MyUtilsDate.getDisplayTime(cursor.getString(columnIndex)));
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tvSentTime.getLayoutParams();
                        if (cursor.getString(cursor.getColumnIndex(MessageDbHelper.MESSAGE_COL_DISPLAYNAME)) == null) {
                            //Owner Side : Right
                            layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.tvMessage);
                            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
                        } else {
                            //Customer Side : Left
                            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.tvMessage);
                            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
                        }
                        tvSentTime.setLayoutParams(layoutParams);
                        return true;    //true由App處理，fasle由OS處理
                    case R.id.ivPhotoLeft:
                        //左邊照片
                        //from
                        String fromPhotoUrl = cursor.getString(columnIndex);
                        RelativeLayout.LayoutParams leftPhotoLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        if (cursor.getString(cursor.getColumnIndex(MessageDbHelper.MESSAGE_COL_DISPLAYNAME)) != null) {
                            leftPhotoLayoutParams.height = MyUtils.dpToPx(getActivity(), 36);
                            leftPhotoLayoutParams.width = MyUtils.dpToPx(getActivity(), 36);
                            view.setVisibility(View.VISIBLE);
                            if (fromPhotoUrl != null && !fromPhotoUrl.isEmpty()) {
                                Picasso.with((Context) mListener)    //fragment下也可用getActivity代替
                                        .load(fromPhotoUrl)
                                        .fit().centerCrop()
                                        .transform(transformation)
                                        .placeholder(R.drawable.person_image_empty_48)
                                        .into((ImageView) view);
                            }
                        } else {
                            leftPhotoLayoutParams.height = 0;
                            leftPhotoLayoutParams.width = 0;
                        }
                        view.setLayoutParams(leftPhotoLayoutParams);
                        return true;   //true由App處理，fasle由OS處理
                    case R.id.ivPhotoRight:
                        //右邊照片
                        //to
                        String toPhotoUrl = AccountUtils.getPhotoUrl(getActivity());//自己的照片
                        RelativeLayout.LayoutParams rightPhotoLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        if (cursor.getString(cursor.getColumnIndex(MessageDbHelper.MESSAGE_COL_DISPLAYNAME)) == null) {
                            rightPhotoLayoutParams.height = MyUtils.dpToPx(getActivity(), 36);
                            rightPhotoLayoutParams.width = MyUtils.dpToPx(getActivity(), 36);
                            view.setVisibility(View.VISIBLE);
                            if (toPhotoUrl != null && !toPhotoUrl.isEmpty()) {
                                Picasso.with(getActivity())    //fragment下也可用getActivity代替
                                        .load(toPhotoUrl)
                                        .fit().centerCrop()
                                        .transform(transformation)
                                        .placeholder(R.drawable.person_image_empty_48)
                                        .into((ImageView) view);
                            }
                        } else {
                            rightPhotoLayoutParams.height = 0;
                            rightPhotoLayoutParams.width = 0;
                        }
                        view.setLayoutParams(rightPhotoLayoutParams);
                        return true;   //true由App處理，fasle由OS處理
                }
                return false;   //為定義的傳fasle由OS處理
            }
        });
        setListAdapter(adapter);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //移動到最後一筆資料
        getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getListView().setStackFromBottom(true);
        //移除分隔線
        getListView().setDivider(null);

        Bundle args = new Bundle();
        HashMap fromLocalInfo = mListener.getFromLocalInfo();
        //用Caller的LocalId去Local DB查Message，以便Render出ListView
        args.putString(MessageDbHelper.MESSAGE_COL_FROM, (String) fromLocalInfo.get(MessageDbHelper.MESSAGE_COL_FROM));   //傳入寄件者的id
        getLoaderManager().initLoader(0, args, this);
        updateSentTime((String) fromLocalInfo.get(MessageDbHelper.MESSAGE_COL_FROM));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void updateSentTime(String fromLocalId) {
        ContentValues values = new ContentValues(1);
        values.put(MessageDbHelper.MESSAGE_COL_READ, MyUtilsDate.CurrentDateTime());
        getActivity().getContentResolver().update(MessageContentProvider.CONTENT_URI_MESSAGES,
                values,
                MessageDbHelper.MESSAGE_COL_FROM + "=? and " + MessageDbHelper.MESSAGE_COL_DISPLAYNAME + " <> '' ",
                new String[]{fromLocalId});
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String fromLocalId = args.getString(MessageDbHelper.MESSAGE_COL_FROM);
        return new CursorLoader(
                getActivity(),
/*URI       */  MessageContentProvider.CONTENT_URI_MESSAGES,
/*Select *  */  null,   //全部欄位的意思 => select *
/*where     */  MessageDbHelper.MESSAGE_COL_FROM + " = ? ",
/*where Args*/  new String[]{fromLocalId},
/*Order     */  MessageDbHelper.MESSAGE_COL_SENT + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
