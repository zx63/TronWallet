package org.tron.MyEntity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "balance")
public class EntityBalance {
    @DatabaseField(id = true)
    public String type;
    @DatabaseField
    public long balance;


    public EntityBalance() {
    }

    public EntityBalance(String type, long balance) {
        this.type = type;
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
