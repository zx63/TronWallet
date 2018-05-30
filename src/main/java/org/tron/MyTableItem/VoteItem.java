package org.tron.MyTableItem;

import javafx.beans.property.SimpleStringProperty;

public class VoteItem {
    public SimpleStringProperty address;
    public SimpleStringProperty voteCount;
    public SimpleStringProperty myVoteCount;
    public SimpleStringProperty url;
    public SimpleStringProperty totalProduced;
    public SimpleStringProperty totalMissed;
    public SimpleStringProperty latestBlockNum;

    public VoteItem(String address, String voteCount, String myVoteCount, String url, String totalProduced, String totalMissed, String latestBlockNum) {
        this.address = new SimpleStringProperty(address);
        this.voteCount = new SimpleStringProperty(voteCount);
        this.myVoteCount = new SimpleStringProperty(myVoteCount);
        this.url = new SimpleStringProperty(url);
        this.totalProduced = new SimpleStringProperty(totalProduced);
        this.totalMissed = new SimpleStringProperty(totalMissed);
        this.latestBlockNum = new SimpleStringProperty(latestBlockNum);
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

    public String getVoteCount() {
        return voteCount.get();
    }

    public SimpleStringProperty voteCountProperty() {
        return voteCount;
    }

    public void setVoteCount(String voteCount) {
        this.voteCount.set(voteCount);
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

    public String getUrl() {
        return url.get();
    }

    public SimpleStringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getTotalProduced() {
        return totalProduced.get();
    }

    public SimpleStringProperty totalProducedProperty() {
        return totalProduced;
    }

    public void setTotalProduced(String totalProduced) {
        this.totalProduced.set(totalProduced);
    }

    public String getTotalMissed() {
        return totalMissed.get();
    }

    public SimpleStringProperty totalMissedProperty() {
        return totalMissed;
    }

    public void setTotalMissed(String totalMissed) {
        this.totalMissed.set(totalMissed);
    }

    public String getLatestBlockNum() {
        return latestBlockNum.get();
    }

    public SimpleStringProperty latestBlockNumProperty() {
        return latestBlockNum;
    }

    public void setLatestBlockNum(String latestBlockNum) {
        this.latestBlockNum.set(latestBlockNum);
    }
}
