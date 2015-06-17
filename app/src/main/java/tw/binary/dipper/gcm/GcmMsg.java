package tw.binary.dipper.gcm;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import tw.binary.dipper.message.MessageDbHelper;

/**
 * Created by eason on 2015/5/4.
 */
public class GcmMsg implements Serializable {

    private List<String> registration_ids;
    private HashMap<String, String> data;
    //TODO 可加payload

    public void addRegId(String regId) {
        if (registration_ids == null)
            registration_ids = new LinkedList<String>();
        registration_ids.add(regId);
    }

    public void createData(String msg,
                           String callerLocalId,
                           String callergcmid,
                           String receiverLocalId,
                           String receivergcmid,
                           String displayName,
                           String photoUrl,
                           String sentTime) {
        if (data == null)
            data = new HashMap<String, String>();

        data.put(MessageDbHelper.MESSAGE_COL_MSG, msg);
        data.put(MessageDbHelper.MESSAGE_COL_FROM, callerLocalId);
        data.put(MessageDbHelper.MESSAGE_COL_FROM_GCMID, callergcmid);
        data.put(MessageDbHelper.MESSAGE_COL_TO, receiverLocalId);
        data.put(MessageDbHelper.MESSAGE_COL_TO_GCMID, receivergcmid);
        data.put(MessageDbHelper.MESSAGE_COL_DISPLAYNAME, displayName);
        data.put(MessageDbHelper.MESSAGE_COL_PHOTOURL, photoUrl);
        data.put(MessageDbHelper.MESSAGE_COL_SENT, sentTime);
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}