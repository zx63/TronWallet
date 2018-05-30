package org.tron.MyController;

import com.google.protobuf.ByteString;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.spongycastle.util.encoders.Hex;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.QRUtil;
import org.tron.MyUtils.ShareData;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.TransactionUtils;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.util.Base64;

public class SignTransactionController {
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

    Protocol.Transaction unSignedTransaction;

    public void initialize() {
        if (unSignedText != null) {
            unSignedText.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        unSignedTransaction = Protocol.Transaction.parseFrom(Hex.decode(newValue));
                        if (unSignedTransaction.getRawData().getContractCount() > 0) {
                            Protocol.Transaction.Contract contract = unSignedTransaction.getRawData().getContract(0);
                            if (contract.getType() == Protocol.Transaction.Contract.ContractType.TransferContract) {
                                ByteString from = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getOwnerAddress();
                                ByteString to = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getToAddress();
                                long amountValue = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getAmount() / Config.DROP_UNIT;

                                fromAddress.setText(WalletClient.encode58Check(from.toByteArray()));
                                toAddress.setText(WalletClient.encode58Check(to.toByteArray()));
                                amount.setText(String.valueOf(amountValue));
                                signed.setSelected(unSignedTransaction.getSignatureCount() > 0);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            });
        }
    }

    public void chooseCreate(ActionEvent actionEvent) {
        Main.OverlayUI<SignTransactionController> screen = Main.instance.overlayUI("sign_transaction_create.fxml");
    }

    public void chooseBroadcast(ActionEvent actionEvent) {
        Main.OverlayUI<SignTransactionController> screen = Main.instance.overlayUI("sign_transaction_broadcast.fxml");
    }

    public void signClicked(ActionEvent actionEvent) {
        if (!signed.isSelected()) {
            ECKey ecKey = ECKey.fromPrivate(Hex.decode(ShareData.getPrivateKey()));
            Protocol.Transaction signedTransaction = TransactionUtils.sign(unSignedTransaction, ecKey);
            byte[] contentByte = signedTransaction.toByteArray();
            String content = Hex.toHexString(contentByte);
            qrcode.setImage(QRUtil.getImage(content));
            qrcodeBox.setVisible(true);
            qrcodeString.setText(content);
            contentHbox.setVisible(false);
        }
    }

    public void cancel(ActionEvent actionEvent) {
        overlayUI.done();
    }
}
