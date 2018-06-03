package org.tron.MyController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.spongycastle.util.encoders.Hex;
import org.tron.MyUiItem.MyVoteItem;
import org.tron.MyUtils.QRUtil;
import org.tron.MyUtils.ShareData;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.TransactionUtils;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletClient;

import java.util.List;

public class ColdSignVoteController {
    public ImageView qrcode;
    public Main.OverlayUI overlayUI;

    public TableColumn<MyVoteItem, String> addressCol;
    public TableColumn<MyVoteItem, String> myVoteCountCol;
    public TableView<MyVoteItem> myVoteView;

    public TextArea signedHexText;
    public TextArea unsignedHexText;
    public HBox unsignedHBox;

    Protocol.Transaction unSignedTransaction;
    private ObservableList<MyVoteItem> voteData = FXCollections.observableArrayList();

    public void initialize() {
        unsignedHexText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                unSignedTransaction = Protocol.Transaction.parseFrom(Hex.decode(newValue));
                if (unSignedTransaction.getRawData().getContractCount() > 0) {
                    for (Protocol.Transaction.Contract contract : unSignedTransaction.getRawData().getContractList()) {
                        if (contract.getType() == Protocol.Transaction.Contract.ContractType.VoteWitnessContract) {
                            List<Contract.VoteWitnessContract.Vote> voteList = contract.getParameter().unpack(Contract.VoteWitnessContract.class).getVotesList();
                            for (Contract.VoteWitnessContract.Vote vote : voteList) {
                                voteData.add(new MyVoteItem(WalletClient.encode58Check(vote.getVoteAddress().toByteArray()), String.valueOf(vote.getVoteCount())));
                            }
                        }
                    }
                    addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
                    myVoteCountCol.setCellValueFactory(new PropertyValueFactory<>("myVoteCount"));
                    myVoteView.setItems(voteData);
                    myVoteView.setEditable(true);
                    myVoteView.refresh();
                    qrcode.setImage(QRUtil.getImage(newValue));
                }
            } catch (Exception e) {

            }
        });
    }

    public void close(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void sign(ActionEvent actionEvent) {
        unsignedHBox.setVisible(false);
        signedHexText.setVisible(true);
        ECKey ecKey = ECKey.fromPrivate(Hex.decode(ShareData.getPrivateKey()));
        Protocol.Transaction signedTransaction = TransactionUtils.sign(unSignedTransaction, ecKey);
        byte[] contentByte = signedTransaction.toByteArray();
        String content = Hex.toHexString(contentByte);
        signedHexText.setText(content);
    }
}
