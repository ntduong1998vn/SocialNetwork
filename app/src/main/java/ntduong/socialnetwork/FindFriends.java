package ntduong.socialnetwork;

public class FindFriends {

    private String profileimage,fullName,status;

    public FindFriends(){

    }

    public FindFriends(String profile_image, String fullname, String status) {
        this.profileimage = profile_image;
        this.fullName = fullname;
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
