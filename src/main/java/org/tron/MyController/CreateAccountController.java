package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.ShareData;
import org.tron.walletcli.Client;

public class CreateAccountController {
    private static final Logger log = LoggerFactory.getLogger(CreateAccountController.class);
    public TextField account, privateKey;
    public GridPane widgetGrid;
    public Button closeButton;
    public Label explanationLabel;
    public Main.OverlayUI overlayUI;

    boolean cold;
    private String passwordTmp;

    private void clearPassword() {
        passwordTmp = "";
    }

    public void initialize(boolean cold, String passwordTmp) {
        this.cold = cold;
        this.passwordTmp = passwordTmp;

        Client client = Client.getInstance();
        client.registerWallet(passwordTmp);
        client.login(passwordTmp);

        String accountString = client.getAddress();

        String privateKeyString = client.backupWallet(passwordTmp);
        account.setText(accountString);
        privateKey.setText(privateKeyString);

        ShareData.setAddress(accountString);
        clearPassword();
    }

    @FXML
    public void okClicked(ActionEvent event) {
        overlayUI.done();
        ShareData.isCold.set(cold);
    }

    public void copyPrivateKeyClicked(ActionEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(privateKey.getText());
        clipboard.setContent(clipboardContent);
    }

    public void backClicked(ActionEvent actionEvent) {
        Main.OverlayUI<FreezeController> screen = Main.instance.overlayUI("import_create.fxml");
    }
}
