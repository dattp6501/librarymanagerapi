package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utils.ImageCustom;

public class Book {
    private int id,pageNumber,number;
    private String title,author,type,image,description;
    private Date releaseDate,createDate;
    private ArrayList<Commemt> commemts;
    private float price;
    private Type typeo;
    public Book() {
    }
    public Book(int id, int pageNumber, String title, String author, String type, String releaseDate, String image, String description, float price, int number, String createDate, Type typeo) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
        this.createDate = format.parse(createDate);
        this.image = image;
        this.description = description;
        this.price = price;
        this.number = number;
        this.typeo = typeo;
    }
    public Book(int id, int pageNumber, String title, String author, String type, String releaseDate, String image, String description, float price, int number, Date createDate, Type typeo) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
        this.createDate = createDate;
        this.image = image;
        this.description = description;
        this.price = price;
        this.number = number;
        this.typeo = typeo;
    }
    public Book(int id, int pageNumber, String title, String author, String type, String releaseDate, byte[] image, String description, float price, int number,String createDate, Type typeo) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
        this.createDate = format.parse(createDate);
        this.image = ImageCustom.bytesToB64(image);
        this.description = description;
        this.price = price;
        this.number = number;
        this.typeo = typeo;
    }
    public Book(int id, int pageNumber, String title, String author, String type, String releaseDate, byte[] image, String description, float price, int number,Date createDate, Type typeo) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.releaseDate = format.parse(releaseDate);
        this.createDate = createDate;
        this.image = ImageCustom.bytesToB64(image);
        this.description = description;
        this.price = price;
        this.number = number;
        this.typeo = typeo;
    }
    public Book(int id, int pageNumber, String title, String author, String type, Date releaseDate, String image, String description, float price, int number, Date createDate, Type typeo) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        this.releaseDate = releaseDate;
        this.createDate = createDate;
        this.image = image;
        this.description = description;
        this.price = price;
        this.number = number;
        this.typeo = typeo;
    }
    public Book(int id, int pageNumber, String title, String author, String type, Date releaseDate, byte[] image, String description, float price, int number, Date createDate, Type typeo) throws ParseException {
        this.id = id;
        this.pageNumber = pageNumber;
        this.title = title;
        this.author = author;
        this.type = type;
        this.releaseDate = releaseDate;
        this.createDate = createDate;
        this.image = ImageCustom.bytesToB64(image);
        this.description = description;
        this.price = price;
        this.number = number;
        this.typeo = typeo;
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
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getCreateDate() {
        return createDate;
    }
    public String getCreateDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createDate);
    }
    public void setCreateDate(String createDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.createDate = format.parse(createDate);
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public ArrayList<Commemt> getCommemts(){
        return commemts;
    }
    public void setComments(ArrayList<Commemt> commemts){
        this.commemts = commemts;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public void setCommemts(ArrayList<Commemt> commemts) {
        this.commemts = commemts;
    }
    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price = price;
    }
    public Type getTypeo() {
        return typeo;
    }
    public void setTypeo(Type typeo) {
        this.typeo = typeo;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Book))
            return false;
        Book other = (Book) obj;
        if (id != other.id)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "Book [id=" + id + ", pageNumber=" + pageNumber + ", number=" + number + ", title=" + title + ", author="
                + author + ", type=" + type + ", image=" + image + ", description=" + description + ", releaseDate="
                + getReleaseDateFormat() + ", createDate=" + getCreateDateFormat() + ", commemts=" + commemts + ", price=" + price
                + ", typeo=" + typeo + "]";
    }
}