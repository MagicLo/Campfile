package tw.binary.dipper.api;

// Created by eason on 2015/1/21.

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@Entity
public class MyResource implements Serializable {
    @Id
    private String id = "";     //使用者的系統帳號
    private String userIdType = ""; //G:G+ F:fb P:polaris
    @Index
    private String userId = "";     //使用者帳號
    private String resType = "C";    //資源類別，可作為未來擴充用 如民宿/露營...
    private String title = "";
    private String desc = "";    //資源描述
    private ArrayList<ResourceImg> Images;  //資源照片
    private String imageFilename;   //主要照片
    private String imageComment;    //照片備註
    private Double lat, lng = 0d;
    @Index
    private String country = "";  //國家
    private String zipCode = "";  //郵遞區號
    @Index
    private String province = ""; //州 省 縣
    @Index
    private String city = "";     //市 鎮 區
    @Index
    private String road = "";     //路 街 巷 弄...
    private String modifiedTime = "";   //Upload就清除，存檔就設定
    private String publishedTime = "";  //Upload就設定，存檔就清除
    private static final long serialVersionUID = 0L;

    public MyResource() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public String getUserIdType() {
        return userIdType;
    }

    public void setUserIdType(String pUserIdType) {
        userIdType = pUserIdType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String pUserId) {
        userId = pUserId;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String pResType) {
        resType = pResType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String pDesc) {
        desc = pDesc;
    }

    public ArrayList<ResourceImg> getImages() {
        return Images;
    }

    public void setImages(ArrayList<ResourceImg> pImages) {
        Images = pImages;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String pImageFilename) {
        imageFilename = pImageFilename;
    }

    public String getImageComment() {
        return imageComment;
    }

    public void setImageComment(String pImageComment) {
        imageComment = pImageComment;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double pLat) {
        lat = pLat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double pLng) {
        lng = pLng;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String pCountry) {
        country = pCountry;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String pZipCode) {
        zipCode = pZipCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String pProvince) {
        province = pProvince;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String pCity) {
        city = pCity;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String pRoad) {
        road = pRoad;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String pModifiedTime) {
        modifiedTime = pModifiedTime;
    }

    public String getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(String pPublishedTime) {
        publishedTime = pPublishedTime;
    }

}
