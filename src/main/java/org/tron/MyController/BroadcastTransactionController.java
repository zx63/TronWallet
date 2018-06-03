package org.tron.MyController;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.spongycastle.util.encoders.Hex;
import org.tron.MyUiItem.MyVoteItem;
import org.tron.MyUtils.Config;
import org.tron.api.GrpcAPI;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.util.List;

public class BroadcastTransactionController {
    public TextField toAddress;
    public TextField fromAddress;
    public TextField amount;
    public TextArea signedText;
    public CheckBox signed;

    public PasswordField password;
    public ImageView qrcode;
    public VBox myTransactionBox;


    public Main.OverlayUI overlayUI;
    Client client = Client.getInstance();


    public TableColumn<MyVoteItem, String> addressCol;
    public TableColumn<MyVoteItem, String> myVoteCountCol;
    public TableView<MyVoteItem> myVoteView;
    private ObservableList<MyVoteItem> voteData = FXCollections.observableArrayList();

    Protocol.Transaction signedTransaction;

    public void initialize() {
        if (signedText != null) {
            signedText.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        signedTransaction = Protocol.Transaction.parseFrom(Hex.decode(newValue));
                        myVoteView.setVisible(false);
                        myTransactionBox.setVisible(false);
                        if (signedTransaction.getRawData().getContractCount() > 0) {
                            Protocol.Transaction.Contract contract = signedTransaction.getRawData().getContract(0);
                            if (contract.getType() == Protocol.Transaction.Contract.ContractType.TransferContract) {
                                ByteString from = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getOwnerAddress();
                                ByteString to = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getToAddress();
                                long amountValue = contract.getParameter().unpack(Contract.TransferContract.class)
                                        .getAmount() / Config.DROP_UNIT;

                                myTransactionBox.setVisible(true);
                                fromAddress.setText(WalletClient.encode58Check(from.toByteArray()));
                                toAddress.setText(WalletClient.encode58Check(to.toByteArray()));
                                amount.setText(String.valueOf(amountValue));
                            } else if (contract.getType() == Protocol.Transaction.Contract.ContractType.VoteWitnessContract) {
                                try {
                                    for (Protocol.Transaction.Contract signedContract : signedTransaction.getRawData().getContractList()) {
                                        if (signedContract.getType() == Protocol.Transaction.Contract.ContractType.VoteWitnessContract) {
                                            List<Contract.VoteWitnessContract.Vote> voteList = contract.getParameter().unpack(Contract.VoteWitnessContract.class).getVotesList();
                                            for (Contract.VoteWitnessContract.Vote vote : voteList) {
                                                voteData.add(new MyVoteItem(WalletClient.encode58Check(vote.getVoteAddress().toByteArray()), String.valueOf(vote.getVoteCount())));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                myVoteView.setVisible(true);
                                addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
                                myVoteCountCol.setCellValueFactory(new PropertyValueFactory<>("myVoteCount"));
                                myVoteView.setItems(voteData);
                                myVoteView.setEditable(true);
                                myVoteView.refresh();
                            }
                            signed.setSelected(signedTransaction.getSignatureCount() > 0);
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
