package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.ShareData;

public class CreateWalletController {
    private static final Logger log = LoggerFactory.getLogger(CreateWalletController.class);
    public PasswordField pass1, pass2;
    public GridPane widgetGrid;
    public Button okButton;
    public Label explanationLabel;
    public Main.OverlayUI overlayUI;

    @FXML
    public void setPasswordClicked(ActionEvent event) {
        if (!pass1.getText().equals(pass2.getText())) {
            GuiUtils.informationalAlert("Passwords do not match", "Try re-typing your chosen passwords.");
            return;
        }
        String password = pass1.getText();
        if (password.length() < 6) {
            GuiUtils.informationalAlert("Password too short", "You need to pick a password at least 6 characters or longer.");
            return;
        }

        GuiUtils.fadeOut(widgetGrid);
        GuiUtils.fadeOut(explanationLabel);
        GuiUtils.fadeOut(okButton);
        ShareData.setPassword(password);
        Main.OverlayUI<CreateAccountController> screen = Main.instance.overlayUI("import_create.fxml");
    }

    public void closeClicked(ActionEvent event) {
        overlayUI.done();
    }
}
