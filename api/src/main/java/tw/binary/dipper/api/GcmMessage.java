package tw.binary.dipper.api;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by eason on 2015/5/15.
 */
@Entity
public class GcmMessage {
    @Id
    public String Id;
    @Index
    public String FromLocalId;
    public String ToLocalId;
    public String Msg;
    @Index
    public String SentTime; //加Index以便排序用
    public String ReadTime;

    public GcmMessage() {
    }

    public GcmMessage(String pId, String pFromLocalId, String pToLocalId, String pMsg, String pSentTime) {
        Id = pId;
        FromLocalId = pFromLocalId;
        ToLocalId = pToLocalId;
        Msg = pMsg;
        SentTime = pSentTime;
    }
}
