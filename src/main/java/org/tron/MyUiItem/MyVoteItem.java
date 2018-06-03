package org.tron.MyUiItem;

import javafx.beans.property.SimpleStringProperty;

public class MyVoteItem {
    public SimpleStringProperty address;
    public SimpleStringProperty myVoteCount;

    public MyVoteItem(String address, String myVoteCount) {
        this.address = new SimpleStringProperty(address);
        this.myVoteCount = new SimpleStringProperty(myVoteCount);
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

    public String getMyVoteCount() {
        return myVoteCount.get();
    }

    public SimpleStringProperty myVoteCountProperty() {
        return myVoteCount;
    }

    public void setMyVoteCount(String myVoteCount) {
        this.myVoteCount.set(myVoteCount);
    }
}
