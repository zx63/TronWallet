package org.tron.MyController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.ShareData;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class TokenController {

    static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    public Main.OverlayUI overlayUI;
    public ListView<XCellData> listView;
    public ObservableList<XCellData> listData = FXCollections.observableArrayList();

    public Label balance;
    public Protocol.Account account;
    public Client client = Client.getInstance();

    boolean initFinish = false;

    // Called by FXMLLoader.
    public void initialize() {
        ShareData.tokenSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            show();
        });
        show();
    }

    private synchronized void show() {
        if (ShareData.getTokenList() != null) {
            ObservableList<XCellData> newListData = FXCollections.observableArrayList();
            for (Contract.AssetIssueContract assetIssueContract : ShareData.getTokenList()) {
                XCellData xCellData = new XCellData(
                        new String(assetIssueContract.getName().toByteArray()),
                        WalletClient.encode58Check(assetIssueContract.getOwnerAddress().toByteArray()),
                        assetIssueContract.getTotalSupply(),
                        assetIssueContract.getStartTime(),
                        assetIssueContract.getEndTime(),
                        new String(assetIssueContract.getDescription().toByteArray()),
                        assetIssueContract.getTrxNum(),
                        assetIssueContract.getNum()
                );
                newListData.add(xCellData);
            }
            newListData = newListData.sorted(Comparator.comparing(o -> o.name.toLowerCase()));
            if (listData == newListData) {
                return;
            }
            if (newListData.isEmpty()) {
                return;
            }
            boolean needUpdate = false;
            if (listData != null) {
                if (newListData.size() == listData.size()) {
                    for (int i = 0; i < newListData.size(); i++) {
                        if (!newListData.get(i).equals(listData.get(i))) {
                            needUpdate = true;
                        }
                    }
                } else {
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                listData = newListData;
                listView.setItems(listData);
                listView.setCellFactory(item -> new XCell());
            }
        }
    }

    static class XCellData {
        String name;
        String issuer;
        long totalSupply;
        long startTime;
        long endTime;
        String desc;
        int trxNum;
        int num;
        boolean disable;
        String actionTips;

        public XCellData(String name, String issuer, long totalSupply, long startTime, long endTime, String desc, int trxNum, int num) {
            this.name = name;
            this.issuer = issuer;
            this.totalSupply = totalSupply;
            this.startTime = startTime;
            this.endTime = endTime;
            this.desc = desc;
            this.trxNum = trxNum;
            this.num = num;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            XCellData xCellData = (XCellData) o;

            if (totalSupply != xCellData.totalSupply) return false;
            if (startTime != xCellData.startTime) return false;
            if (endTime != xCellData.endTime) return false;
            if (trxNum != xCellData.trxNum) return false;
            if (num != xCellData.num) return false;
            if (name != null ? !name.equals(xCellData.name) : xCellData.name != null) return false;
            if (issuer != null ? !issuer.equals(xCellData.issuer) : xCellData.issuer != null) return false;
            return desc != null ? desc.equals(xCellData.desc) : xCellData.desc == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (issuer != null ? issuer.hashCode() : 0);
            result = 31 * result + (int) (totalSupply ^ (totalSupply >>> 32));
            result = 31 * result + (int) (startTime ^ (startTime >>> 32));
            result = 31 * result + (int) (endTime ^ (endTime >>> 32));
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            result = 31 * result + trxNum;
            result = 31 * result + num;
            return result;
        }
    }

    static class XCell extends ListCell<XCellData> {
        HBox hbox = new HBox();

        Label nameLabel = new Label("");
        Label issuerLabel = new Label("");
        Label totalSupplyLabel = new Label("");
        Label startTimeLabel = new Label("");
        Label endTimeLabel = new Label("");
        Button action = new Button("");

        XCellData lastItem;

        public XCell() {
            super();
            totalSupplyLabel.setPrefWidth(150);
            nameLabel.setPrefWidth(300);
            issuerLabel.setPrefWidth(350);
            startTimeLabel.setPrefWidth(160);
            endTimeLabel.setPrefWidth(160);
            action.setPrefWidth(200);

            hbox.getChildren().addAll(nameLabel, issuerLabel, startTimeLabel, endTimeLabel, action);
            hbox.setSpacing(2);
            hbox.setAlignment(Pos.CENTER_LEFT);
            action.setOnAction(event -> {
                Main.OverlayUI<TokenParticipateController> screen = Main.instance.overlayUI("token_participate.fxml");
                screen.controller.initialize(getItem());
                logger.info(event.toString());
            });
        }

        @Override
        protected void updateItem(XCellData item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                nameLabel.setText(item.name);
                issuerLabel.setText(item.issuer);
                totalSupplyLabel.setText(String.valueOf(item.totalSupply));
                startTimeLabel.setText(formatTimeString(item.startTime));
                endTimeLabel.setText(formatTimeString(item.endTime));

                long currTime = System.currentTimeMillis();
                if (currTime < item.startTime) {
                    item.disable = true;
                    item.actionTips = "Not started yet";
                } else if (currTime > item.endTime) {
                    item.disable = true;
                    item.actionTips = "Finished";
                } else {
                    item.disable = false;
                    item.actionTips = "Participate";
                }

                action.setDisable(item.disable);
                action.setText(item.actionTips);

                setGraphic(hbox);
            }
        }

        String formatTimeString(long time) {
            Date date = new Date(time);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateFormatted = formatter.format(date);
            return dateFormatted;
        }
    }

}
