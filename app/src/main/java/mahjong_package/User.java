package mahjong_package;


/*
This class should be pushed t database for any list of users
 */
public class User {

    private String uname = "";
    private String uid = "";
    private String email = "";
    private String providerId = "";
    private boolean emailVerified = false;
    private String lastGameId = "NaN";
    private String userStatus = "inactive";     // inactive, joined, playing
    private int winTallies = 0;

    public User() { }

    public User(String uname, String uid, String email, boolean emailVerified, String userStatus, String providerId) {
        this();
        setUname(uname);
        setUid(uid);
        setEmail(email);
        setEmailVerified(emailVerified);
        setUserStatus(userStatus);
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

    public int getWinTallies() { return this.winTallies; }

    public void setWinTallies(int tallies) { this.winTallies = tallies; }

}
