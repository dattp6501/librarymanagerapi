package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import utils.ImageCustom;

public class Book {
    private int id,pageNumber;
    private String title,author,type,image;
    private Date releaseDate;
    public Book() {
    }
    public Book(int id, int pageNumber, String title, String author, String type, String releaseDate, String image) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
        this.image = image;
    }
    public Book(int id, int pageNumber, String title, String author, String type, String releaseDate, byte[] image) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
        this.image = ImageCustom.bytesToB64(image);
    }
    public Book(int id, int pageNumber, String title, String author, String type, Date releaseDate, String image) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        this.releaseDate = releaseDate;
        this.image = image;
    }
    public Book(int id, int pageNumber, String title, String author, String type, Date releaseDate, byte[] image) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        this.releaseDate = releaseDate;
        this.image = ImageCustom.bytesToB64(image);
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Date getReleaseDate() {
        return releaseDate;
    }
    public String getReleaseDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd").format(releaseDate);
    }
    public void setReleaseDate(String releaseDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
    }
    public String getImage() {
        return image;
    }
    public byte[] getImageBytes(){
        return ImageCustom.B64ToBytes(image);
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setImageBytes(byte[] image) {
        this.image = ImageCustom.bytesToB64(image);
    }
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
    @Override
    public String toString() {
        return "Book [id=" + id + ", pageNumber=" + pageNumber + ", title=" + title + ", author=" + author + ", type="
                + type + ", image=" + image + ", releaseDate=" + releaseDate + "]";
    }
}
