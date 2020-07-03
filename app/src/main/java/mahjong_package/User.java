package mahjong_package;


/*
This class should be pushed t database for any list of users
 */
public class User {

    private String uname;
    private String uid;
    private String email;
    private String providerId;
    private boolean emailVerified;
    private String lastGameId;
    private String userStatus;     // inactive, joined, playing

    public User() {
        this.setUname("");
        this.setUid("");
        this.setEmail("");
        this.setProviderId("");
        this.setUserStatus("inactive");
        this.setEmailVerified(false);
        this.setLastGameId("NaN");
    }

    public User(String uname, String uid, String email, boolean emailVerified, String userStatus, String providerId) {
        setUname(uname);
        setUid(uid);
        setEmail(email);
        setEmailVerified(emailVerified);
        setUserStatus(userStatus);
        setLastGameId("NaN");
        setProviderId(providerId);
    }


    public boolean userExists() { return !this.uid.equals(""); };

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailVerified(boolean logic) {
        this.emailVerified = logic;
    }

    public void setUserStatus(String status) {
        this.userStatus = status;
    }

    public void setProviderId(String id) {
        this.providerId = id;
    }

    public void setLastGameId(String id) { this.lastGameId = id; }

    public String getUid() { return this.uid; }

    public String getUname() { return this.uname; }

    public String getEmail() { return this.email; }

    public boolean getEmailVerified() { return this.emailVerified; }

    public String getUserStatus() { return this.userStatus; }

    public String getProviderId() { return this.providerId; }

    public String getLastGameId() { return this.lastGameId; }

}
