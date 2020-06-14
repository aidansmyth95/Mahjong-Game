package mahjong_package;


import java.io.Serializable;

/*
This class should be pushed t database for any list of users
 */
public class User implements Serializable {

    private String uname;
    private String uid;
    private String email;
    private String providerID;
    private boolean email_verified;
    private String last_game_id;
    private String user_status;     // inactive, joined, playing

    public User() {
        this.set_uname("");
        this.set_uid("");
        this.set_email("");
        this.set_provider_id("");
        this.set_user_status("inactive");
        this.set_email_verified(false);
        this.set_last_game_id("NaN");
    }


    public User(String uname, String uid, String email, boolean email_verified, String user_status) {
        set_uname(uname);
        set_uid(uid);
        set_email(email);
        set_email_verified(email_verified);
        set_user_status(user_status);
        set_last_game_id("NaN");
    }

    public boolean userExists() { return !this.uid.equals(""); };

    public void set_uname(String uname) {
        this.uname = uname;
    }

    public void set_uid(String uid) {
        this.uid = uid;
    }

    public void set_email(String email) {
        this.email = email;
    }

    public void set_email_verified(boolean logic) {
        this.email_verified = logic;
    }

    public void set_user_status(String status) {
        this.user_status = status;
    }

    public void set_provider_id(String id) {
        this.providerID = id;
    }

    public void set_last_game_id(String id) { this.last_game_id = id; }

    public String get_uid() { return this.uid; }

    public String get_uname() { return this.uname; }

    public String get_email() { return this.email; }

    public boolean get_email_verified() { return this.email_verified; }

    public String get_user_status() { return this.user_status; }

    public String get_provider_id() { return this.providerID; }

    public String get_last_game_id() { return this.last_game_id; }

}
