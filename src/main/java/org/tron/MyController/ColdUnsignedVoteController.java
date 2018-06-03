package org.tron.MyController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.spongycastle.util.encoders.Hex;
import org.tron.MyUiItem.MyVoteItem;
import org.tron.MyUtils.QRUtil;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletClient;

import java.util.List;

public class ColdUnsignedVoteController {
    public ImageView qrcode;
    public TextArea myVoteHexText;
    public Main.OverlayUI overlayUI;
    Protocol.Transaction coldTransaction;


    public TableColumn<MyVoteItem, String> addressCol;
    public TableColumn<MyVoteItem, String> myVoteCountCol;
    public TableView<MyVoteItem> myVoteView;

    private ObservableList<MyVoteItem> voteData = FXCollections.observableArrayList();

    public void initialize(Protocol.Transaction coldTransaction) {
        String content = Hex.toHexString(coldTransaction.toByteArray());
        this.coldTransaction = coldTransaction;
        myVoteHexText.setText(content);
        qrcode.setImage(QRUtil.getImage(content));
        try {
            for (Protocol.Transaction.Contract contract : coldTransaction.getRawData().getContractList()) {
                if (contract.getType() == Protocol.Transaction.Contract.ContractType.VoteWitnessContract) {
                    List<Contract.VoteWitnessContract.Vote> voteList = contract.getParameter().unpack(Contract.VoteWitnessContract.class).getVotesList();
                    for (Contract.VoteWitnessContract.Vote vote : voteList) {
                        voteData.add(new MyVoteItem(WalletClient.encode58Check(vote.getVoteAddress().toByteArray()), String.valueOf(vote.getVoteCount())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        myVoteCountCol.setCellValueFactory(new PropertyValueFactory<>("myVoteCount"));
        myVoteView.setItems(voteData);
        myVoteView.setEditable(true);
        myVoteView.refresh();
    }

    public void close(ActionEvent actionEvent) {
        overlayUI.done();
    }
}
