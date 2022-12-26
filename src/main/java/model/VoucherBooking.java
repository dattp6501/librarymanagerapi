package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VoucherBooking {
    private int id,active;
    private String name,type,note;
    private float value;
    private Date createdDate;
    public VoucherBooking() {
    }
    public VoucherBooking(int id, String name, String type, String note, float value, int active, String createdDate) throws ParseException {
        this.id = id;
        this.name = name;
        this.type = type;
        this.note = note;
        this.value = value;
        this.active = active;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.createdDate = format.parse(createdDate);
    }
    public VoucherBooking(int id, String name, String type, String note, float value, int active, Date createdDate) throws ParseException {
        this.id = id;
        this.name = name;
        this.type = type;
        this.note = note;
        this.value = value;
        this.active = active;
        this.createdDate = createdDate;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public float getValue() {
        return value;
    }
    public void setValue(float value) {
        this.value = value;
    }
    public int getActive() {
        return active;
    }
    public void setActive(int active) {
        this.active = active;
    }
    public Date getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public void setCreatedDate(String createdDate) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.createdDate = format.parse(createdDate);
    }
    public String getCreatedDateStr(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createdDate);
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
        if (!(obj instanceof VoucherBooking))
            return false;
        VoucherBooking other = (VoucherBooking) obj;
        if (id != other.id)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "VoucherBooking [id=" + id + ", active=" + active + ", name=" + name + ", type=" + type + ", note="
                + note + ", value=" + value + ", createdDate=" + getCreatedDateStr() + "]";
    }
    
}
