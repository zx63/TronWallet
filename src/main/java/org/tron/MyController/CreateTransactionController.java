package org.tron.MyController;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.util.ArrayList;
import java.util.Base64;

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

    Protocol.Transaction unSignedTransaction;

    public void initialize() {
        if (unSignedText != null) {
            unSignedText.textProperty().addListener((observable, oldValue, newValue) -> {
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
            });
        }
        fromAddress.setText(ShareData.getAddress());
    }

    public void cancel(ActionEvent actionEvent) {
        overlayUI.done();
    }

    private Protocol.Transaction createTransaction(com.google.protobuf.Message message, Protocol.Transaction.Contract.ContractType contractType) {
        Protocol.Transaction.raw.Builder transactionBuilder = Protocol.Transaction.raw.newBuilder().addContract(
                Protocol.Transaction.Contract.newBuilder().setType(contractType).setParameter(
                        Any.pack(message)).build());
        Protocol.Transaction transaction = Protocol.Transaction.newBuilder().setRawData(transactionBuilder.build()).build();
        return transaction;
    }

    private Protocol.Transaction setReference(Protocol.Transaction transaction, long blockNum, byte[] blockHash) {
        byte[] refBlockNum = ByteArray.fromLong(blockNum);
        Protocol.Transaction.raw rawData = transaction.getRawData().toBuilder()
                .setRefBlockHash(ByteString.copyFrom(ByteArray.subArray(blockHash, 8, 16)))
                .setRefBlockBytes(ByteString.copyFrom(ByteArray.subArray(refBlockNum, 6, 8)))
                .build();
        return transaction.toBuilder().setRawData(rawData).build();
    }

    public void createClicked(ActionEvent actionEvent) {
        try {
            byte[] to = WalletClient.decodeFromBase58Check(toAddress.getText());
            byte[] from = WalletClient.decodeFromBase58Check(fromAddress.getText());
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
