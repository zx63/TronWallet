package org.tron.MyController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.SQLiteUtil;
import org.tron.MyUtils.ShareData;
import org.tron.MyEntity.EntityMeta;
import org.tron.walletcli.Client;

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    public Main.OverlayUI overlayUI;
    @FXML
    HBox buttonsBox;
    @FXML
    PasswordField password;
    @FXML
    ImageView padlockImage;
    @FXML
    GridPane widgetGrid;
    @FXML
    Label explanationLabel;

    public void initialize() {
        Platform.runLater(password::requestFocus);
    }

    @FXML
    void confirmClicked(ActionEvent event) {
        String passwordString = this.password.getText();
        if (passwordString.isEmpty() || passwordString.length() < Config.PASS_MIN_LENGTH) {
            GuiUtils.informationalAlert("Bad password", "The password you entered is empty or too short.");
            return;
        }

        Client client = Client.getInstance();
        if (client.login(passwordString)) {
//            ShareData.setPassword(passwordString);
//            ShareData.setPrivateKey(client.backupWallet(passwordString));
            GuiUtils.fadeOut(widgetGrid);
            GuiUtils.fadeOut(explanationLabel);
            GuiUtils.fadeOut(buttonsBox);
            String address = client.getAddress();
            if (address == null) {
                Main.OverlayUI<CreateAccountController> screen = Main.instance.overlayUI("create_account.fxml");
            } else {
                ShareData.setAddress(address);
                ShareData.addressSimpleObjectProperty.set(address);
                EntityMeta entityMeta = SQLiteUtil.getMetaEntity();
                if (entityMeta != null && entityMeta.cold != 0) {
                    ShareData.isCold.set(true);
                }

                overlayUI.done();
            }
        } else {
            GuiUtils.informationalAlert("Wrong password", "The password you entered is wrong.");
        }
    }

    public void closeClicked(ActionEvent actionEvent) {
        Main.instance.stop();
    }
}
