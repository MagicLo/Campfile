package tw.binary.dipper.api;

import java.io.Serializable;

// Created by eason on 2015/1/22.

public class ResourceImg implements Serializable {

    private Long Id;
    private String Filename;   //Local Images Filename
    private boolean Uploaded;
    //private String ImageStr;//Base64編碼過的字串，給後端使用
    private String Comment; //吸引人的描述
    private static final long serialVersionUID = 0L;

    public ResourceImg() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long pId) {
        Id = pId;
    }

    public String getFilename() {
        return Filename;
    }

    public void setFilename(String pFilename) {
        Filename = pFilename;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String pComment) {
        Comment = pComment;
    }
}
