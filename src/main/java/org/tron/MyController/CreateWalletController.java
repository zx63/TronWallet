package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.CheckPassword;
import org.tron.MyUtils.Config;
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
        if (password.length() < Config.PASS_MIN_LENGTH) {
            GuiUtils.informationalAlert("Password too short", "You need to pick a password at least 8 characters or longer.");
            return;
        }
        CheckPassword.LEVEL level = CheckPassword.getPasswordLevel(password);
        if (level == CheckPassword.LEVEL.EASY) {
            GuiUtils.informationalAlert("Password too weak", "You need to pick a stronger password contains uppercase letters, numbers and symbols.");
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
