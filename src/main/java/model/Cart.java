package model;

import java.util.ArrayList;

public class Cart {
    private Member member;
    private ArrayList<Book> books;
    public Cart() {
    }
    public Cart(Member member, ArrayList<Book> books) {
        this.member = member;
        this.books = books;
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
    @Override
    public String toString() {
        return "Cart [member=" + member + ", books=" + books + "]";
    }
}
