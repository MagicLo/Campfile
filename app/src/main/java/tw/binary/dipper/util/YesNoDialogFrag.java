package tw.binary.dipper.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.binary.dipper.R;

public class YesNoDialogFrag extends DialogFragment {
    private Communicator communicator;

    @Override
    public void onAttach(Activity activity) {
        communicator = (Communicator) activity;
        super.onAttach(activity);
    }

    public static YesNoDialogFrag newInstance(String title, String message) {
        YesNoDialogFrag frag = new YesNoDialogFrag();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.alert_dialog_icon);
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("message"));

        builder.setNegativeButton(R.string.Dialog_Cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        communicator.onDialogResult(R.string.Dialog_Cancel);
                    }
                }
        ).setPositiveButton(R.string.Dialog_Yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        communicator.onDialogResult(R.string.Dialog_Yes);
                    }
                }
        );

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setCancelable(false);   //避免點Dialog意外關閉
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    //Dialog的結果回傳給呼叫的Activity
    public interface Communicator {                 //Interface 宣告
        void onDialogResult(int message);    //要實作的方法

    }
}