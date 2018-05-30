package org.tron.MyController;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
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
import org.tron.api.GrpcAPI;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.util.ArrayList;
import java.util.Base64;

public class BroadcastTransactionController {
    public TextField toAddress;
    public TextField fromAddress;
    public TextField amount;
    public TextArea signedText;
    public CheckBox signed;

    public PasswordField password;
    public ImageView qrcode;
    public HBox contentHbox;

    public Main.OverlayUI overlayUI;
    Client client = Client.getInstance();

    Protocol.Transaction signedTransaction;

    public void initialize() {
        if (signedText != null) {
            signedText.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        signedTransaction = Protocol.Transaction.parseFrom(Hex.decode(newValue));
                        if (signedTransaction.getRawData().getContractCount() > 0) {
                            Protocol.Transaction.Contract contract = signedTransaction.getRawData().getContract(0);
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
                                signed.setSelected(signedTransaction.getSignatureCount() > 0);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            });
        }
    }

    public void cancel(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void broadcastClicked(ActionEvent actionEvent) {
        if (signed.isSelected()) {
            try {
                GrpcAPI.Return result = WalletClient.broadcastTransaction(signedTransaction.toByteArray());
                if (result.getResult()) {
                    GuiUtils.informationalAlert("Success", "Send");
                    signedText.setText("");
                    fromAddress.setText("");
                    toAddress.setText("");
                    amount.setText("");
                    signed.setSelected(false);
                } else {
                    GuiUtils.informationalAlert("Faild to send transaction", result.getCode().toString());
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

}
