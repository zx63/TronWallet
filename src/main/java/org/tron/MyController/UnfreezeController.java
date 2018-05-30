package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.walletcli.Client;

import static org.tron.MyUtils.ShareData.balanceSimpleObjectProperty;
import static org.tron.MyUtils.ShareData.freezeSimpleObjectProperty;

public class UnfreezeController {
    private static final Logger log = LoggerFactory.getLogger(UnfreezeController.class);
    public Main.OverlayUI overlayUI;

    public Label frozen;
    public Label balance;

    String count = "0";

    public void initialize() {
        balanceSimpleObjectProperty.addListener((observable, oldValue, newValue) -> balance.setText(newValue));
        freezeSimpleObjectProperty.addListener((observable, oldValue, newValue) -> {
            count = newValue;
            frozen.setText(newValue);
        });
        frozen.setText(ShareData.getFrozenBalance());
        balance.setText(ShareData.getBalance());
    }


    public void cancelClicked(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void okClicked(ActionEvent actionEvent) {
        Client client = Client.getInstance();
        GrpcAPI.Return result = client.unfreezeBalance(ShareData.getPassword());
        if (result.getResult()) {
            GuiUtils.informationalAlert("Success", "Unfreeze " + count + " TRX");
        } else {
            GuiUtils.informationalAlert("Failed to unfreeze", result.getCode().toString());
            return;
        }
        overlayUI.done();
    }
}
