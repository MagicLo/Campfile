package tw.binary.dipper.api;/* Created by eason on 2015/6/11. */

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
public class ResourceImage implements Serializable {
    //也是Images Filename
    @Id
    private String Id;
    //資源的Id
    @Index
    private String MyResourceId;
    //排序流水號
    private String Seq;
    //Owner's Id
    @Index
    private String LocalId;
    //吸引人的描述
    private String Comment;
    private static final long serialVersionUID = 0L;

    public ResourceImage() {
    }
}
