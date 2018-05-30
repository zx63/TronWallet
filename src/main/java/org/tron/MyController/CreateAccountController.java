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

    public void initialize(boolean cold) {
        this.cold = cold;
        String password = ShareData.getPassword();

        Client client = Client.getInstance();
        client.registerWallet(password);
        client.login(password);

        String accountString = client.getAddress();

        String privateKeyString = client.backupWallet(ShareData.getPassword());
        account.setText(accountString);
        privateKey.setText(privateKeyString);

        ShareData.setAddress(accountString);
        ShareData.setPrivateKey(privateKeyString);

    }
}
