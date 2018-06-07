package org.tron.MyController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.ShareData;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import sun.security.provider.SHA;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User interface for entering a password on demand, e.g. to send money. Also used when encrypting a wallet. Shows a
 * progress meter as we scrypt the password.
 */
public class ImportAccountController {
    public static final String TAG = ImportAccountController.class.getName() + ".target-time";
    private static final Logger log = LoggerFactory.getLogger(ImportAccountController.class);
    public Main.OverlayUI overlayUI;
    @FXML
    HBox buttonsBox;
    @FXML
    TextField privateKey;
    @FXML
    GridPane widgetGrid;
    @FXML
    Label explanationLabel;

    boolean cold;

    private String passwordTmp;

    private void clearPassword() {
        passwordTmp = "";
    }

    public void initialize(String passwordTmp) {

    }
    public void initialize(boolean cold, String passwordTmp) {
        Platform.runLater(privateKey::requestFocus);
        this.cold = cold;
        this.passwordTmp = passwordTmp;
    }

    @FXML
    public void confirmClicked(ActionEvent event) {
        ShareData.isCold.set(cold);
        String privateKeyString = privateKey.getText();
        if (privateKeyString.isEmpty()) {
            GuiUtils.informationalAlert("Bad private key", "The private key you entered is empty.");
            return;
        }

        Client client = Client.getInstance();
        if(client.importWallet(passwordTmp, privateKeyString)) {
            client.login(passwordTmp);
            Protocol.Account account = client.queryAccount();
//            ShareData.setBalance(String.valueOf(account.getBalance() / Config.DROP_UNIT));
//            ShareData.setPrivateKey(privateKeyString);
            ShareData.setAddress(client.getAddress());

            GuiUtils.fadeOut(widgetGrid);
            GuiUtils.fadeOut(explanationLabel);
            GuiUtils.fadeOut(buttonsBox);
            overlayUI.done();
            clearPassword();
            return;
        }
        GuiUtils.informationalAlert("Bad private key", "The private key you entered is incorrect.");
    }
}
