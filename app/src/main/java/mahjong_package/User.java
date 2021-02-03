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
    private int winTallies;

    public User() {
        setUname("NaN");
        setUid("NaN");
        setEmail("NaN");
        setProviderId("NaN");
        setEmailVerified(false);
        setLastGameId("NaN");
        setUserStatus("inactive");
        setWinTallies(0);
    }

    public User(String uname, String uid, String email, boolean emailVerified, String userStatus, String providerId, int winTallies) {
        //this();
        setUname(uname);
        setUid(uid);
        setEmail(email);
        setEmailVerified(emailVerified);
        setUserStatus(userStatus);
        setProviderId(providerId);
        setWinTallies(winTallies);
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
