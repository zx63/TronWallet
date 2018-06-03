package org.tron.MyUiItem;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class TokenItem {
    public SimpleStringProperty name;
    public SimpleStringProperty issuer;
    public SimpleStringProperty url;
    public SimpleStringProperty totalSupply;
    public SimpleStringProperty startTime;
    public SimpleStringProperty endTime;
    public SimpleStringProperty desc;
    public SimpleStringProperty trxNum;
    public SimpleStringProperty num;
    public SimpleBooleanProperty disabled;
    public SimpleStringProperty actionTips;

    public TokenItem(String name, String issuer, String totalSupply, String url, String startTime, String endTime, String desc, String trxNum, String num, boolean disabled, String actionTips) {
        this.name = new SimpleStringProperty(name);
        this.issuer = new SimpleStringProperty(issuer);
        this.totalSupply = new SimpleStringProperty(totalSupply);
        this.url = new SimpleStringProperty(url);
        this.startTime = new SimpleStringProperty(startTime);
        this.endTime = new SimpleStringProperty(endTime);
        this.desc = new SimpleStringProperty(desc);
        this.trxNum = new SimpleStringProperty(trxNum);
        this.num = new SimpleStringProperty(num);
        this.disabled = new SimpleBooleanProperty(disabled);
        this.actionTips = new SimpleStringProperty(actionTips);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getIssuer() {
        return issuer.get();
    }

    public SimpleStringProperty issuerProperty() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer.set(issuer);
    }

    public String getTotalSupply() {
        return totalSupply.get();
    }

    public SimpleStringProperty totalSupplyProperty() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply.set(totalSupply);
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

    public String getStartTime() {
        return startTime.get();
    }

    public SimpleStringProperty startTimeProperty() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public String getEndTime() {
        return endTime.get();
    }

    public SimpleStringProperty endTimeProperty() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }

    public String getDesc() {
        return desc.get();
    }

    public SimpleStringProperty descProperty() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc.set(desc);
    }

    public String getTrxNum() {
        return trxNum.get();
    }

    public SimpleStringProperty trxNumProperty() {
        return trxNum;
    }

    public void setTrxNum(String trxNum) {
        this.trxNum.set(trxNum);
    }

    public String getNum() {
        return num.get();
    }

    public SimpleStringProperty numProperty() {
        return num;
    }

    public void setNum(String num) {
        this.num.set(num);
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public SimpleBooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    public String getActionTips() {
        return actionTips.get();
    }

    public SimpleStringProperty actionTipsProperty() {
        return actionTips;
    }

    public void setActionTips(String actionTips) {
        this.actionTips.set(actionTips);
    }
}
