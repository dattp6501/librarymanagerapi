package model;

import java.util.Date;

import global.Init;

public class MemberLogin {
    private Member member;
    private String session;
    private Date time = new Date(new Date().getTime() + Init.TIME);
    public MemberLogin() {
    }
    public MemberLogin(Member member, String session) {
        this.member = member;
        this.session = session;
    }
    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }
    public String getSession() {
        return session;
    }
    public void setSession(String session) {
        this.session = session;
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((session == null) ? 0 : session.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        MemberLogin other = (MemberLogin) obj;
        if(session.equals(other.session))
            return true;
        return false;
    }
}
