package org.tron.MyController;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.walletcli.Client;

import static org.tron.MyUtils.ShareData.balanceSimpleObjectProperty;
import static org.tron.MyUtils.ShareData.freezeSimpleObjectProperty;

public class FreezeController {
    private static final Logger log = LoggerFactory.getLogger(FreezeController.class);
    public Main.OverlayUI overlayUI;
    public TextField count;
    public TextField days;
    public PasswordField password;
    public Label frozen;
    public Label balance;

    public void initialize() {
        Platform.runLater(count::requestFocus);
        balanceSimpleObjectProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                balance.setText(newValue);
            }
        });
        freezeSimpleObjectProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                frozen.setText(newValue);
            }
        });
        frozen.setText(ShareData.getFrozenBalance());
        balance.setText(ShareData.getBalance());
    }


    public void cancelClicked(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void okClicked(ActionEvent actionEvent) {
        Client client = Client.getInstance();
        if (!client.checkPassword(password.getText())) {
            GuiUtils.informationalAlert("Failed", "Wrong password");
            return;
        }
        GrpcAPI.Return result = client.freezeBalance(password.getText(), Long.valueOf(count.getText()) * Config.DROP_UNIT, Long.valueOf(days.getText()));
        if (result.getResult()) {
            GuiUtils.informationalAlert("Success", "Add newly " + count.getText() + " TRX to frozon for " + days.getText() + " days");
            overlayUI.done();
        } else {
            GuiUtils.informationalAlert("Failed to freeze", result.getCode().toString());
            return;
        }
    }
}
