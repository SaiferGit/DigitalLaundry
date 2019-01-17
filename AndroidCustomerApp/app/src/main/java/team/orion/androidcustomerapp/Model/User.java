package team.orion.androidcustomerapp.Model;

public class User {
    private String username, fullname, address, phoneNo;

    public User() {
    }

    public User(String username, String fullname, String address, String phoneNo) {
        this.username = username;
        this.fullname = fullname;
        this.address = address;
        this.phoneNo = phoneNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
