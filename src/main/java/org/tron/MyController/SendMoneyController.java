package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.walletcli.Client;

import java.util.HashMap;

import static org.tron.MyUtils.ShareData.balanceTmpSimpleObjectProperty;

public class SendMoneyController {
    public Button sendBtn;
    public Button cancelBtn;
    public TextField toAddress;
    public TextField amount;
    public ComboBox<String> type;

    public Main.OverlayUI overlayUI;

    HashMap<String, String> balanceTableData = new HashMap<>();


    public void initialize() {
        if (ShareData.getAccount() == null) {
            return;
        }
        balanceTableData.put("TRX", String.valueOf(ShareData.getAccount().getBalance()));

        if (ShareData.getAccount().getAssetCount() > 0) {
            ShareData.getAccount().getAssetMap().forEach((type, balance) -> {
                balanceTableData.put(type, String.valueOf(balance));
            });
        }
        type.getItems().addAll(balanceTableData.keySet());
        type.setValue("TRX");
    }

    public void send(ActionEvent event) {
        sendBtn.setDisable(true);
        cancelBtn.setDisable(true);
        Client client = Client.getInstance();
        try {
            if (type.getValue().equals("TRX")) {
                GrpcAPI.Return result = client.sendCoin(ShareData.getPassword(), toAddress.getText(), (long) (Double.parseDouble(amount.getText()) * Config.DROP_UNIT));
                if (result.getResult()) {
                    GuiUtils.informationalAlert("Success", "Send " + amount.getText() + " TRX to " + toAddress.getText());
                } else {
                    GuiUtils.informationalAlert("Failed to send " + type.getValue(), result.getCode().toString());
                    return;
                }
            } else {
                GrpcAPI.Return result = client.transferAsset(ShareData.getPassword(), toAddress.getText(), type.getValue(), Long.parseLong(amount.getText()));
                if (result.getResult()) {
                    GuiUtils.informationalAlert("Success", "Send " + amount.getText() + " " + type.getValue() + " to " + toAddress.getText());
                } else {
                    GuiUtils.informationalAlert("Failed to send " + type.getValue(), result.getCode().toString());
                    return;
                }
            }
        } catch (Exception e) {
            GuiUtils.informationalAlert("Failed to send money", e.getMessage());
            return;
        } finally {
            sendBtn.setDisable(false);
            cancelBtn.setDisable(false);
        }
        ShareData.tabSimpleObjectProperty.set(1);
        balanceTmpSimpleObjectProperty.set(String.valueOf(Double.parseDouble(ShareData.getBalance()) - Double.parseDouble(amount.getText())));
        toAddress.setText("");
        amount.setText("");
        overlayUI.done();
    }

    public void close(ActionEvent actionEvent) {
        overlayUI.done();
    }
}
