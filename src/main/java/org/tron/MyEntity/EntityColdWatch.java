package org.tron.MyEntity;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "coldwatch")
public class EntityColdWatch {
    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String address;

    public EntityColdWatch() {
    }

    public EntityColdWatch(int id, String address) {
        this.id = id;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
