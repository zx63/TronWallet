package org.tron.MyUiItem;

import javafx.beans.property.SimpleStringProperty;

public class BalanceItem {
    public SimpleStringProperty type;
    public SimpleStringProperty balance;
    public SimpleStringProperty freeze;

    public BalanceItem(String type, String balance, String freeze) {
        this.type = new SimpleStringProperty(type);
        this.balance = new SimpleStringProperty(balance);
        this.freeze = new SimpleStringProperty(freeze);
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getBalance() {
        return balance.get();
    }

    public SimpleStringProperty balanceProperty() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance.set(balance);
    }

    public String getFreeze() {
        return freeze.get();
    }

    public SimpleStringProperty freezeProperty() {
        return freeze;
    }

    public void setFreeze(String freeze) {
        this.freeze.set(freeze);
    }
}
