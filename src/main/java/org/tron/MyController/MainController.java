package org.tron.MyController;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyEntity.EntityColdWatch;
import org.tron.MyUiItem.BalanceItem;
import org.tron.MyUiItem.TransactionItem;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.SQLiteUtil;
import org.tron.MyUtils.ShareData;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.core.config.Parameter;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletClient;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    public TextField address;

    public TableView<TransactionItem> transactionTableView;
    public TableView<BalanceItem> balanceTableView;
    private ObservableList<TransactionItem> transactionData = FXCollections.observableArrayList();
    private ObservableList<BalanceItem> balanceTableData = FXCollections.observableArrayList();

    public TableColumn<BalanceItem, String> typeCol;
    public TableColumn<BalanceItem, String> balanceCol;
    public TableColumn<BalanceItem, String> freezeCol;


    public TableColumn<TransactionItem, String> addressCol;
    public TableColumn<TransactionItem, String> amountCol;
    public TableColumn<TransactionItem, String> dateCol;
    public TableColumn<TransactionItem, String> flagCol;
    public TableColumn<TransactionItem, String> type2Col;
    public TableColumn<TransactionItem, String> hashCol;


    public VBox coldBox;
    public VBox hotBox;

    public TabPane tabPane;
    public Tab voteTab;
    public Tab tokenTab;
    public Tab toolTab;

    public Label power;
    public Label entropy;
    public Label balance;

    public ImageView close;
    public ImageView min;
    public ImageView settings;

    public MenuItem copyTransactionItem;

    public TextField watchAddress;

    public Pane titlePane;
    private double xOffset;
    private double yOffset;
    private double xInit;
    private double yInit;

    // Called by FXMLLoader.
    public void initialize(URL location, ResourceBundle resources) {
        ShareData.tabSimpleObjectProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() == 6) {
                    switchTab(newValue.intValue());
                    ShareData.tabSimpleObjectProperty.set(-1);
                }
            }
        });
        close.setOnMouseEntered(e -> close.setImage(new Image("images/close_hover.png")));
        close.setOnMousePressed(e -> close.setImage(new Image("images/close_press.png")));
        close.setOnMouseExited(e -> close.setImage(new Image("images/close.png")));
        close.setOnMouseClicked(e -> Main.instance.stop());

        min.setOnMouseEntered(e -> min.setImage(new Image("images/min_hover.png")));
        min.setOnMousePressed(e -> min.setImage(new Image("images/min_press.png")));
        min.setOnMouseExited(e -> min.setImage(new Image("images/min.png")));
        min.setOnMouseClicked(e -> Main.instance.mainWindow.setIconified(true));

        settings.setOnMouseEntered(e -> settings.setImage(new Image("images/settings_hover.png")));
        settings.setOnMousePressed(e -> settings.setImage(new Image("images/settings_press.png")));
        settings.setOnMouseExited(e -> settings.setImage(new Image("images/settings.png")));
        settings.setOnMouseClicked(e -> {
            Main.OverlayUI<SettingsController> screen = Main.instance.overlayUI("settings_password.fxml");
        });

        titlePane.setOnMousePressed(e -> {
            xOffset = e.getScreenX();
            yOffset = e.getScreenY();
            xInit = Main.instance.mainWindow.getX();
            yInit = Main.instance.mainWindow.getY();
        });
        titlePane.setOnMouseDragged(e -> {
            Main.instance.mainWindow.setX(xInit + e.getScreenX() - xOffset);
            Main.instance.mainWindow.setY(yInit + e.getScreenY() - yOffset);
        });
        ShareData.accountSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            if (voteTab == null) {
                try {
                    voteTab = tabPane.getTabs().get(3);
                    voteTab.setContent(FXMLLoader.load(this.getClass().getResource("vote.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tokenTab == null) {
                try {
                    tokenTab = tabPane.getTabs().get(4);
                    tokenTab.setContent(FXMLLoader.load(this.getClass().getResource("token.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (toolTab == null) {
                try {
                    toolTab = tabPane.getTabs().get(5);
                    toolTab.setContent(FXMLLoader.load(this.getClass().getResource("sign_transaction_choice.fxml")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ShareData.isCold.addListener((observable, oldValue, newValue) -> {
            coldBox.setVisible(newValue);
            hotBox.setVisible(!newValue);
        });
        ShareData.addressSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            try {
                address.setText(newValue);
            } catch (Exception e) {

            }
        });
        ShareData.balanceSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            try {
                balance.setText(new DecimalFormat(",###.######").format(Long.parseLong(newValue)));
            } catch (Exception e) {

            }
        });
        ShareData.balanceTmpSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            try {
                balance.setText(new DecimalFormat(",###.######").format(Long.parseLong(newValue)));
                tabPane.getSelectionModel().select(1);
            } catch (Exception e) {

            }
        });
        ShareData.tronPowerTmpSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            try {
                power.setText(new DecimalFormat(",###.######").format(Long.parseLong(newValue)));
            } catch (Exception e) {

            }
        });
        ShareData.tronPowerSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            try {
                power.setText(new DecimalFormat(",###.######").format(Long.parseLong(newValue)));
            } catch (Exception e) {

            }
        });
        ShareData.bandWidthSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            try {
                entropy.setText(new DecimalFormat(",###.######").format(Long.parseLong(newValue)));
            } catch (Exception e) {

            }
        });
        ShareData.accountSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            balanceTableData.clear();
            long frozenCount = 0;
            if (newValue.getFrozenCount() > 0) {
                for (Protocol.Account.Frozen frozen : newValue.getFrozenList()) {
                    frozenCount += frozen.getFrozenBalance();
                }
            }
            balanceTableData.addAll(new BalanceItem("TRX", String.valueOf(newValue.getBalance() / Config.DROP_UNIT), String.valueOf(frozenCount / Config.DROP_UNIT)));

            if (newValue.getAssetCount() > 0) {
                newValue.getAssetMap().forEach((type, balance) -> {
                    balanceTableData.addAll(new BalanceItem(type, String.valueOf(balance), ""));
                });
            }
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
            freezeCol.setCellValueFactory(new PropertyValueFactory<>("freeze"));
            balanceTableView.setItems(balanceTableData);
            balanceTableView.refresh();
        });

        ShareData.transactionSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            List<XCellData> cellDataList = getTransactionDetailList(newValue, ShareData.getAddress());
            transactionData.clear();

            for (int i = 0; i < cellDataList.size(); i++) {
                XCellData xCellData = cellDataList.get(i);
                if (i < ShareData.pendingTransaction.size()) {
                    xCellData.flag = "Pending";
                }
                transactionData.add(new TransactionItem(xCellData.address, xCellData.count, xCellData.type, xCellData.flag, xCellData.date, xCellData.hash));
            }
            addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            flagCol.setCellValueFactory(new PropertyValueFactory<>("flag"));
            type2Col.setCellValueFactory(new PropertyValueFactory<>("type"));
            hashCol.setCellValueFactory(new PropertyValueFactory<>("hash"));
            transactionTableView.setItems(transactionData);
            transactionTableView.refresh();
        });

        address.setMinWidth(Region.USE_PREF_SIZE);
        address.setMaxWidth(Region.USE_PREF_SIZE);
        address.textProperty().addListener((ov, prevText, currText) -> {
            // Do this in a Platform.runLater because of Textfield has no padding at first time and so on
            Platform.runLater(() -> {
                Text text = new Text(currText);
                text.setFont(address.getFont()); // Set the same font, so the size is the same
                double width = text.getLayoutBounds().getWidth() // This big is the Text in the TextField
                        + address.getPadding().getLeft() + address.getPadding().getRight() // Add the padding of the TextField
                        + 2d; // Add some spacing
                address.setPrefWidth(width); // Set the width
                address.positionCaret(address.getCaretPosition()); // If you remove this line, it flashes a little bit
            });
        });


        XCellData xCellData = new XCellData("loading...", "", "", "", "", "");
        transactionData.add(new TransactionItem(xCellData.address, xCellData.count, xCellData.type, xCellData.flag, xCellData.date, xCellData.hash));
        transactionTableView.setItems(transactionData);

        copyTransactionItem.setOnAction(t -> {
            // Do something with current row
            TransactionItem curr = transactionTableView.getItems().get(transactionTableView.getFocusModel().getFocusedIndex());
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(curr.toString());
            clipboard.setContent(clipboardContent);
        });

        EntityColdWatch coldWatch = SQLiteUtil.getEntityColdWatch();
        if (coldWatch != null && StringUtils.isNotEmpty(coldWatch.getAddress())) {
            watchAddress.setText(coldWatch.getAddress());
        }
    }

    public void sendMoneyOutClicked(ActionEvent event) {
        if (checkAccount()) {
            Main.OverlayUI<SendMoneyController> screen = Main.instance.overlayUI("send_money.fxml");
        }
    }

    public void receiveClicked(ActionEvent actionEvent) {
        if (checkAccount()) {
            Main.OverlayUI<ImportAccountController> screen = Main.instance.overlayUI("receive_money.fxml");
        }
    }

    public void signClicked(ActionEvent actionEvent) {
        if (checkAccount()) {
            Main.OverlayUI<ImportAccountController> screen = Main.instance.overlayUI("sign_transaction_choice.fxml");
        }
    }

    public void freezeClicked(ActionEvent actionEvent) {
        if (checkAccount()) {
            Main.OverlayUI<FreezeController> screen = Main.instance.overlayUI("freeze.fxml");
        }
    }

    public void unfreezeClicked(ActionEvent actionEvent) {
        if (checkAccount()) {
            Main.OverlayUI<UnfreezeController> screen = Main.instance.overlayUI("unfreeze.fxml");
        }
    }

    public void tokenClicked(ActionEvent actionEvent) {
        if (checkAccount()) {
            Main.OverlayUI<SettingsController> screen = Main.instance.overlayUI("token.fxml");
        }
    }

    public void offlineSignClicked(ActionEvent actionEvent) {
        Main.OverlayUI<SignTransactionController> screen = Main.instance.overlayUI("sign_transaction_sign.fxml");
    }

    public void offlineSignVoteClicked(ActionEvent actionEvent) {
        Main.OverlayUI<SignTransactionController> screen = Main.instance.overlayUI("sign_cold_vote.fxml");
    }

    public boolean checkAccount() {
        Protocol.Account account = ShareData.getAccount();
        if (account == null) {
            GuiUtils.informationalAlert("Checking account", "Please wait");
            return false;
        }
        return true;
    }

    public List<XCellData> getTransactionDetailList(List<Protocol.Transaction> transactionList, String address) {
        List<XCellData> returnData = new ArrayList<>();
        if (transactionList == null) {
            return returnData;
        }
        List<Protocol.Transaction> tList = new ArrayList<>();
        tList.addAll(transactionList);
        for (int i = 0; i < tList.size(); i++) {
            Protocol.Transaction transaction = transactionList.get(i);
            ByteString from = null;
            ByteString to = null;
            long date = transaction.getRawData().getTimestamp();
            if (String.valueOf(date).length() > String.valueOf(System.currentTimeMillis()).length()) {
                date = (long) (date / 1e6);
            }
            String amount = null;
            ByteString typeByteString;
            String type = "";
            String hash = ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
            int ccount = transaction.getRawData().getContractCount();
            if (ccount > 0) {
                for (int j = 0; j < ccount; j++) {
                    Protocol.Transaction.Contract contract = transaction.getRawData().getContract(j);
                    try {
                        switch (transaction.getRawData().getContract(j).getType()) {
                            case TransferContract:
                                from = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getOwnerAddress();
                                to = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getToAddress();
                                amount = new DecimalFormat(",###.######")
                                        .format(contract.getParameter().unpack(Contract.TransferContract.class).getAmount() * 1.0f / Config.DROP_UNIT);
                                type = "TRX";
                                break;
                            case TransferAssetContract:
                                from = contract.getParameter().unpack(Contract.TransferAssetContract.class)
                                        .getOwnerAddress();
                                to = contract.getParameter().unpack(Contract.TransferAssetContract.class)
                                        .getToAddress();
                                amount = new DecimalFormat(",###.######").format(contract.getParameter().unpack(Contract.TransferAssetContract.class)
                                        .getAmount());
                                typeByteString = contract.getParameter().unpack(Contract.TransferAssetContract.class)
                                        .getAssetName();
                                type = new String(typeByteString.toByteArray());
                                break;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }


                    if (amount != null) {
                        String fromAddress = WalletClient.encode58Check(from.toByteArray());
                        String toAddress = WalletClient.encode58Check(to.toByteArray());
                        if (StringUtils.equals(fromAddress, address)) {
                            XCellData dataItem = new XCellData(toAddress, String.valueOf(amount), type, "Send", String.valueOf(date), hash);
                            returnData.add(dataItem);
                        } else {
                            XCellData dataItem = new XCellData(fromAddress, String.valueOf(amount), type, "Receive", String.valueOf(date), hash);
                            returnData.add(dataItem);
                        }
                    }
                }
            }

        }

        return returnData;
    }

    public void setWatch(ActionEvent actionEvent) {
        if (StringUtils.length(watchAddress.getText()) != Parameter.CommonConstant.BASE58CHECK_ADDRESS_SIZE) {
            GuiUtils.informationalAlert("Fail", "Not a tron address");
            return;
        }
        EntityColdWatch entityColdWatch = new EntityColdWatch(0, watchAddress.getText());
        SQLiteUtil.setColdWatchEntity(entityColdWatch);
        GuiUtils.informationalAlert("Success", "");
    }

    static class XCellData {
        String address;
        String count;
        String type;
        String flag;
        String date;
        String hash;

        public XCellData(String address, String count, String type, String flag, String date, String hash) {
            this.address = address;
            this.count = count;
            this.type = type;
            this.flag = flag;
            this.hash = hash;
            try {
                this.date = formatTimeString(Long.valueOf(date));
            } catch (Exception e) {
                this.date = formatTimeString(System.currentTimeMillis());
            }
        }

        String formatTimeString(long time) {
            Date date = new Date(time);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateFormatted = formatter.format(date);
            return dateFormatted;
        }
    }

    public void switchTab(int index) {
        tabPane.getSelectionModel().select(index);
    }

}
