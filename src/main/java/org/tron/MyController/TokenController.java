package org.tron.MyController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUiItem.TokenItem;
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

    public Label balance;

    public TableView<TokenItem> tokenTableView;
    private ObservableList<TokenItem> tokenData = FXCollections.observableArrayList();

    public Protocol.Account account;
    public Client client = Client.getInstance();

    public TableColumn<TokenItem, String> nameCol;
    public TableColumn<TokenItem, String> urlCol;
    public TableColumn<TokenItem, String> issuerCol;
    public TableColumn<TokenItem, String> startTimeCol;
    public TableColumn<TokenItem, String> endTimeCol;
    public TableColumn<TokenItem, String> actionCol;

    // Called by FXMLLoader.
    public void initialize() {
        ShareData.tokenSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            show();
        });
        show();
    }

    public void createToken(ActionEvent actionEvent) {
        Main.OverlayUI<TokenCreateController> screen = Main.instance.overlayUI("token_create.fxml");
    }

    private String formatTimeString(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }

    private synchronized void show() {
        if (ShareData.getTokenList() != null) {
            ObservableList<TokenItem> newTokenData = FXCollections.observableArrayList();
            for (Contract.AssetIssueContract assetIssueContract : ShareData.getTokenList()) {
                long currTime = System.currentTimeMillis();
                long startTime = assetIssueContract.getStartTime();
                long endTime = assetIssueContract.getEndTime();
                boolean disable = false;
                String actionTips = "";
                if (currTime < startTime) {
                    disable = true;
                    actionTips = "Not started yet";
                } else if (currTime > endTime) {
                    disable = true;
                    actionTips = "Finished";
                } else {
                    disable = false;
                    actionTips = "Participate";
                }
                newTokenData.add(new TokenItem(
                        new String(assetIssueContract.getName().toByteArray()),
                        WalletClient.encode58Check(assetIssueContract.getOwnerAddress().toByteArray()),
                        String.valueOf(assetIssueContract.getTotalSupply()),
                        new String(assetIssueContract.getUrl().toByteArray()),
                        formatTimeString(assetIssueContract.getStartTime()),
                        formatTimeString(assetIssueContract.getEndTime()),
                        new String(assetIssueContract.getDescription().toByteArray()),
                        String.valueOf(assetIssueContract.getTrxNum()),
                        String.valueOf(assetIssueContract.getNum()),
                        disable,
                        actionTips
                ));
            }
            newTokenData = newTokenData.sorted(Comparator.comparing(o -> o.getName().toLowerCase()));
            if (tokenData == newTokenData) {
                return;
            }
            if (newTokenData.isEmpty()) {
                return;
            }
            boolean needUpdate = false;
            if (newTokenData.size() == tokenData.size()) {
                for (int i = 0; i < newTokenData.size(); i++) {
                    if (!newTokenData.get(i).equals(tokenData.get(i))) {
                        needUpdate = true;
                    }
                }
            } else {
                needUpdate = true;
            }
            if (!needUpdate) {
                return;
            }


            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
            issuerCol.setCellValueFactory(new PropertyValueFactory<>("issuer"));
            startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
            actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
            Callback<TableColumn<TokenItem, String>, TableCell<TokenItem, String>> actionCellFactory
                    = //
                    new Callback<TableColumn<TokenItem, String>, TableCell<TokenItem, String>>() {
                        @Override
                        public TableCell call(final TableColumn<TokenItem, String> param) {
                            final TableCell<TokenItem, String> cell = new TableCell<TokenItem, String>() {
                                final Button btn = new Button();

                                @Override
                                public void updateItem(String item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setGraphic(null);
                                        setText(null);
                                    } else {
                                        TokenItem person = getTableView().getItems().get(getIndex());
                                        btn.setOnAction(event -> {
                                            Main.OverlayUI<TokenParticipateController> screen = Main.instance.overlayUI("token_participate.fxml");
                                            screen.controller.initialize(person);
                                        });
                                        btn.setDisable(person.disabled.get());
                                        btn.setText(person.actionTips.get());
                                        btn.setMinWidth(120);
                                        setGraphic(btn);
                                        setText(null);
                                    }
                                }
                            };
                            return cell;
                        }
                    };
            actionCol.setCellFactory(actionCellFactory);

            tokenData = newTokenData;
            tokenTableView.setItems(tokenData);
            tokenTableView.refresh();
        }
    }

}
