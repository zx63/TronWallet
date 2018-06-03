package org.tron.MyUiItem;

import javafx.beans.property.SimpleStringProperty;

public class TransactionItem {
    public SimpleStringProperty date;
    public SimpleStringProperty hash;
    public SimpleStringProperty flag;
    public SimpleStringProperty address;
    public SimpleStringProperty amount;
    public SimpleStringProperty type;

    public TransactionItem(String address, String amount, String type, String flag, String date, String hash) {
        this.address = new SimpleStringProperty(address);
        this.amount = new SimpleStringProperty(amount);
        this.date = new SimpleStringProperty(date);
        this.flag = new SimpleStringProperty(flag);
        this.type = new SimpleStringProperty(type);
        this.hash = new SimpleStringProperty(hash);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getAmount() {
        return amount.get();
    }

    public SimpleStringProperty amountProperty() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount.set(amount);
    }

    public String getDate() {
        return date.get();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getFlag() {
        return flag.get();
    }

    public SimpleStringProperty flagProperty() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag.set(flag);
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

    public String getHash() {
        return hash.get();
    }

    public SimpleStringProperty hashProperty() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash.set(hash);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TransactionItem{");
        sb.append("date=").append(date.get());
        sb.append(", hash=").append(hash.get());
        sb.append(", flag=").append(flag.get());
        sb.append(", address=").append(address.get());
        sb.append(", amount=").append(amount.get());
        sb.append(", type=").append(type.get());
        sb.append('}');
        return sb.toString();
    }
}
