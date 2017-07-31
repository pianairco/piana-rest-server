package ir.piana.dev.webtool2.server.model;

/**
 * Created by SYSTEM on 7/31/2017.
 */
public class UserModel {
    private String fname;
    private String lname;

    public UserModel() {
    }

    public UserModel(String fname, String lname) {
        this.fname = fname;
        this.lname = lname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }
}
