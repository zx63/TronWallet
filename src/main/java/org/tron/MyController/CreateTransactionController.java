package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.spongycastle.util.encoders.Hex;
import org.tron.MyEntity.EntityColdWatch;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.QRUtil;
import org.tron.MyUtils.SQLiteUtil;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

public class CreateTransactionController {
    public TextField toAddress;
    public TextField fromAddress;
    public TextField amount;
    public TextArea unSignedText;
    public CheckBox signed;

    public PasswordField password;
    public HBox qrcodeBox;
    public ImageView qrcode;
    public TextArea qrcodeString;
    public HBox contentHbox;

    public Main.OverlayUI overlayUI;
    Client client = Client.getInstance();

    public void initialize() {
        EntityColdWatch coldWatch = SQLiteUtil.getEntityColdWatch();
        fromAddress.setText(coldWatch.getAddress());
    }

    public void cancel(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void createClicked(ActionEvent actionEvent) {
        try {
            byte[] from = WalletClient.decodeFromBase58Check(fromAddress.getText());
            byte[] to = WalletClient.decodeFromBase58Check(toAddress.getText());
            Contract.TransferContract contract = WalletClient.createTransferContract(to, from, Long.parseLong(amount.getText()) * Config.DROP_UNIT);
            Protocol.Transaction transaction = WalletClient.createTransaction4Transfer(contract);

            byte[] contentByte = transaction.toByteArray();
            String content = Hex.toHexString(contentByte);
            qrcode.setImage(QRUtil.getImage(content));
            qrcodeString.setText(content);
            qrcodeBox.setVisible(true);
            contentHbox.setVisible(false);
        } catch (Exception e) {
            GuiUtils.informationalAlert("Faild to generate transaction", "Invalid input");
        }
    }

}
