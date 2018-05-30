package org.tron.entity;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "password")
public class EntityPassword {
    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String salt0;
    @DatabaseField
    public String salt1;
    @DatabaseField
    public String password;
    @DatabaseField
    public String pubKey;
    @DatabaseField
    public String privKeyEnced;

    public EntityPassword() {
    }

    public EntityPassword(int id, String salt0, String salt1, String password, String pubKey, String privKeyEnced) {
        this.id = id;
        this.salt0 = salt0;
        this.salt1 = salt1;
        this.password = password;
        this.pubKey = pubKey;
        this.privKeyEnced = privKeyEnced;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSalt0() {
        return salt0;
    }

    public void setSalt0(String salt0) {
        this.salt0 = salt0;
    }

    public String getSalt1() {
        return salt1;
    }

    public void setSalt1(String salt1) {
        this.salt1 = salt1;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrivKeyEnced() {
        return privKeyEnced;
    }

    public void setPrivKeyEnced(String privKeyEnced) {
        this.privKeyEnced = privKeyEnced;
    }
}
