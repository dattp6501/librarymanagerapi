package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Commemt {
    private Member member;
    private String content;
    private int star;
    private Date date;
    public Commemt(Member member, String content, int star, Date date) {
        this.member = member;
        this.content = content;
        this.star = star;
        this.date = date;
    }
    public Commemt(Member member, String content, int star, String dateStr) throws ParseException {
        this.member = member;
        this.content = content;
        this.star = star;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.date = format.parse(dateStr);
    }
    public Commemt() {
    }
    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getStar() {
        return star;
    }
    public void setStar(int star) {
        this.star = star;
    }
    public Date getDate() {
        return date;
    }
    public String getDateStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setDate(String dateStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.date = format.parse(dateStr);
    }
    @Override
    public String toString() {
        return "Commemt [member=" + member + ", content=" + content + ", star=" + star + ", date=" + getDateStr() + "]";
    }
}