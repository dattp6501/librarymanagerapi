package model;

import java.util.ArrayList;

public class Cart {
    private int id;
    private Member member;
    private ArrayList<Book> books;
    public Cart() {
    }
    public Cart(Member member, ArrayList<Book> books, int id) {
        this.member = member;
        this.books = books;
        this.id = id;
    }
    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }
    public ArrayList<Book> getBooks() {
        return books;
    }
    public void setBooks(ArrayList<Book> books) {
        this.books = books;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "Cart [id=" + id + ", member=" + member + ", books=" + books + "]";
    }
}
