package tw.binary.dipper.provider;/* Created by eason on 2015/5/22. */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import tw.binary.dipper.api.myResourceApi.model.MyResource;

public class MyResourceArrayAdapter extends ArrayAdapter<MyResource> {
    int mLayout;
    Context mContext = null;
    ArrayList<MyResource> mMyResources = null;
    int[] mViewList;
    private ViewBinder viewBinderListener;

    public interface ViewBinder {
        void setViewValue(View view, MyResource pMyResource);
    }

    public void setViewBinder(ViewBinder listener) {
        viewBinderListener = listener;
    }

    public MyResourceArrayAdapter(Context context, int layoutResourceId, ArrayList<MyResource> pMyResources, int[] pViewList) {
        super(context, layoutResourceId, pMyResources); //pMyResource要傳入給super
        mLayout = layoutResourceId;
        mContext = context;
        mViewList = pViewList;
        mMyResources = pMyResources;    //記憶起來供getView用
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mLayout, parent, false);
        }
        //呼叫Implement的Activity，已處理UI顯示
        for (int aMViewList : mViewList) {
            viewBinderListener.setViewValue(convertView.findViewById(aMViewList), mMyResources.get(position));
        }

        return convertView;
    }

    public void swapData(ArrayList<MyResource> pMyResources) {
        this.clear();
        if (pMyResources != null)
            this.addAll(pMyResources);
    }
}

