package org.tron.MyController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.ShareData;
import org.tron.walletcli.Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    public Main.OverlayUI overlayUI;
    @FXML
    public PasswordField password;
    public PasswordField newPassword;
    public PasswordField newPassword2;
    public TextField privateKey;

    public HBox exportChangeHBox;

    public Button export;
    public Button confirmChangePassword;

    public GridPane widgetGrid;
    public Label explanationLabel;

    public void initialize() {
        if (password != null) {
            Platform.runLater(password::requestFocus);
        }
    }

    public void resetClicked(ActionEvent actionEvent) {
        try {
            Files.copy(Paths.get(Config.WALLET_DB_FILE), Paths.get(Config.WALLET_DB_FILE_BAK), StandardCopyOption.REPLACE_EXISTING);
            GuiUtils.informationalAlert("Success", "The old wallet saved to " + Config.WALLET_DB_FILE_BAK);
            Main.instance.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportClicked(ActionEvent actionEvent) {
        privateKey.setText(ShareData.getPrivateKey());
    }

    public void confirmChangePasswordClicked(ActionEvent actionEvent) {
        if (!newPassword.getText().equals(newPassword2.getText())) {
            GuiUtils.informationalAlert("Passwords do not match", "Try re-typing your chosen passwords.");
            return;
        }
        String password = newPassword.getText();
        if (password.length() < Config.PASS_MIN_LENGTH) {
            GuiUtils.informationalAlert("Password too short", "You need to pick a password at least 6 characters or longer.");
            return;
        }

        Client client = Client.getInstance();
        if (client.changePassword(ShareData.getPassword(), password)) {
            GuiUtils.informationalAlert("Success", "Password changed");
            ShareData.setPassword(password);
            overlayUI.done();
        } else {
            GuiUtils.informationalAlert("Fail", "Password change failed");
        }
    }

    public void cancelClicked(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void nextClicked(ActionEvent actionEvent) {
        if (ShareData.getPassword().equals(password.getText())) {
            Main.OverlayUI<SettingsController> screen = Main.instance.overlayUI("settings.fxml");
        } else {
            GuiUtils.informationalAlert("Wrong password", "The password you entered is wrong.");
        }
    }
}
