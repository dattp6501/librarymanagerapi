package model;

public class Type {
    private int id;
    private String name,note;
    public Type() {
    }
    public Type(int id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
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
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    @Override
    public String toString() {
        return "Type [id=" + id + ", name=" + name + ", note=" + note + "]";
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
        if (!(obj instanceof Type))
            return false;
        Type other = (Type) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
