package model;

public class Booked {
    private Book book;
    private float price;
    private int number;
    private String note;
    public Booked(Book book, float price, int number, String note) {
        this.book = book;
        this.price = price;
        this.number = number;
        this.note = note;
    }
    public Booked() {
    }
    public Book getBook() {
        return book;
    }
    public void setBook(Book book) {
        this.book = book;
    }
    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price = price;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    @Override
    public String toString() {
        return "Booked [book=" + book + ", price=" + price + ", number=" + number + ", note=" + note + "]";
    }
}
