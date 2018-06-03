package org.tron.MyController;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.tron.walletcli.Client;


public class AlertWindowController {
    public Label messageLabel;
    public Label detailsLabel;
    public Button okButton;
    public Button cancelButton;
    public Button actionButton;


    public Label passwordTips;
    public PasswordField password;
    public VBox msgBox;
    public VBox passwordBox;
    public ImageView msgIcon;
    public ImageView lockIcon;

    /**
     * Initialize this alert dialog for information about a crash.
     */
    public void crashAlert(Stage stage, String crashMessage) {
        messageLabel.setText("Unfortunately, we screwed up and the app crashed. Sorry about that!");
        detailsLabel.setText(crashMessage);

        cancelButton.setVisible(false);
        actionButton.setVisible(false);
        okButton.setOnAction(actionEvent -> stage.close());
    }

    /**
     * Initialize this alert for general information: OK button only, nothing happens on dismissal.
     */
    public void informational(Stage stage, String message, String details) {
        messageLabel.setText(message);
        detailsLabel.setText(details);
        cancelButton.setVisible(false);
        actionButton.setVisible(false);
        okButton.setOnAction(actionEvent -> stage.close());
    }

    /**
     * Initialize this alert for general information: OK button only, nothing happens on dismissal.
     */
    public void passwordCheck(Stage stage, SimpleBooleanProperty checkBooleanProperty) {
        msgBox.setVisible(false);
        msgIcon.setVisible(false);
        passwordBox.setVisible(true);
        lockIcon.setVisible(true);
        messageLabel.setVisible(false);
        detailsLabel.setVisible(false);
        actionButton.setVisible(false);
        okButton.setOnAction(actionEvent -> {
            if(StringUtils.isEmpty(Client.getInstance().backupWallet(password.getText()))) {
                passwordTips.setText("Wrong password");
                return;
            }
            stage.close();
            checkBooleanProperty.set(true);
        });
        cancelButton.setVisible(true);
        cancelButton.setOnAction(actionEvent -> {
            stage.close();
        });
    }
}
