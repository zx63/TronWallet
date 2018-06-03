package org.tron.MyEntity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "meta")
public class EntityMeta {
    @DatabaseField(id = true)
    public String address;
    @DatabaseField
    public int cold;

    public EntityMeta() {
    }

    public EntityMeta(String address, int cold) {
        this.address = address;
        this.cold = cold;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCold() {
        return cold;
    }

    public void setCold(int cold) {
        this.cold = cold;
    }
}
