package model;

import utils.ImageCustom;

public class Member {
    private int id;
    private String email,fullName,userName,passWord,address;
    private Group group;
    private String image;
    public Member() {
    }
    public Member(int id, String email, String fullName, String userName, String passWord, Group group, String image, String address) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userName = userName;
        this.passWord = passWord;
        this.group = group;
        this.image = image;
        this.address = address;
    }
    public Member(int id, String email, String fullName, String userName, String passWord, Group group, byte[] image, String address) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userName = userName;
        this.passWord = passWord;
        this.group = group;
        if(image==null || image.equals("")){
            this.image = null;
        }else{
            this.image = ImageCustom.bytesToB64(image);
        }
        this.address = address;
    }
    public String getImage() {
        return image;
    }
    public byte[] getImageBytes(){
        if(image==null || image.equals("")) return null;
        return ImageCustom.B64ToBytes(image);
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setImageBytes(byte[] image) {
        if(image==null){
            this.image = null;
            return;
        }
        this.image = ImageCustom.bytesToB64(image);
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassWord() {
        return passWord;
    }
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
    public Group getGroup() {
        return group;
    }
    public void setGroup(Group group) {
        this.group = group;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        Member other = (Member) obj;
        if (id == other.id)
            return true;
        if (email.equals(other.email))
            return true;
        if (userName.equals(other.userName)) 
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "Member [id=" + id + ", email=" + email + ", fullName=" + fullName + ", userName=" + userName
                + ", passWord=" + passWord + ", address=" + address + ", group=" + group + ", image=" + "base64" + "]";
    }
}
