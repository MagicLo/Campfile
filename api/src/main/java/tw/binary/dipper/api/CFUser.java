package tw.binary.dipper.api;

// Created by eason on 2015/4/13.

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
public class CFUser implements Serializable {
    @Id
    private String Id;
    private String DisplayName;
    @Index
    private String Email;
    private String PhotoURL;
    private String IDProvider;
    private String Password;
    private String Address;
    @Index
    private String PhoneNumber;
    private String LastLoginTime;  //Upload就設定，存檔就清除
    private String LastModifyTime;
    private String GcmRegId;    //GCM RegId

    public CFUser() {

        //this.Id = UUID.randomUUID().toString();
    }

    public String getId() {
        return Id;
    }

    public void setId(String pId) {
        Id = pId;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String pDisplayName) {
        DisplayName = pDisplayName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String pEmail) {
        Email = pEmail;
    }

    public String getPhotoURL() {
        return PhotoURL;
    }

    public void setPhotoURL(String pPhotoURL) {
        PhotoURL = pPhotoURL;
    }

    public String getIDProvider() {
        return IDProvider;
    }

    public void setIDProvider(String pIDProvider) {
        IDProvider = pIDProvider;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String pPassword) {
        Password = pPassword;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String pAddress) {
        Address = pAddress;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String pPhoneNumber) {
        PhoneNumber = pPhoneNumber;
    }

    public String getLastLoginTime() {
        return LastLoginTime;
    }

    public void setLastLoginTime(String pLastLoginTime) {
        LastLoginTime = pLastLoginTime;
    }

    public String getLastModifyTime() {
        return LastModifyTime;
    }

    public void setLastModifyTime(String pLastModifyTime) {
        LastModifyTime = pLastModifyTime;
    }

    public String getGcmRegId() {
        return GcmRegId;
    }

    public void setGcmRegId(String pGcmRegId) {
        GcmRegId = pGcmRegId;
    }

    /*public IdToken getSavedToken() {
        if (Token != null) {
            IdToken idToken = IdToken.parse(Token);
            if (IdToken != null && !IdToken.isExpired()) {
                return idToken;
            }
        }
        return null;
    }

    public GitkitUser getSavedGitkitUser() {
        if (GitkitUserKey != null) {
            return GitkitUser.fromJsonString(GitkitUserKey);
        }
        return null;
    }*/

    /*public boolean isUserLoggedIn() {
        return getSavedToken() != null && getSavedGitkitUser() != null;
    }*/

    /*public void clearLoggedInUser() {
        mPrefs.edit()
                .remove(ID_TOKEN_KEY)
                .remove(GITKIT_USER_KEY)
                .remove(ACC_TOKEN)
                .apply();
    }*/
}


//User function

// findUser Lookup the user (This can be called from a Transaction)
// createUser will create the initial datastore entry for the user
// initialSetup will add the initial things in a somewhat reasonable way.
// copyUserPhoto will copy the photo from, will likey be called from delayFunc

//Token function
// Login - see if the token is valid
// Refresh will refresh an Access Token (ATok)
// GetSecretKey will send our key in a way that we should only be called once.
// Expired tells us if we have a valid AuthToken
// ID accessor func for UserID
// Aauth validates a given AccessToken
// decodeSegment decodes the Base64 encoding segment of the JWT token.
// It pads the string if necessary.