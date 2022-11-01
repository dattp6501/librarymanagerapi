package model;

public class Member {
    private int id;
    private String email,fullName,userName,passWord;
    public Member() {
    }
    public Member(int id, String email, String fullName, String userName, String passWord) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userName = userName;
        this.passWord = passWord;
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
    @Override
    public String toString() {
        return "Member [id=" + id + ", email=" + email + ", fullName=" + fullName + ", userName=" + userName
                + ", passWord=" + passWord + "]";
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
}
