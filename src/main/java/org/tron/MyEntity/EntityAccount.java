package org.tron.MyEntity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "account")
public class EntityAccount {
    @DatabaseField(id = true)
    public String address;
    @DatabaseField
    public String pubKey;
    @DatabaseField
    public String priKey;

    public EntityAccount() {
    }

    public EntityAccount(String address, String pubKey, String priKey) {
        this.address = address;
        this.pubKey = pubKey;
        this.priKey = priKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }
}
