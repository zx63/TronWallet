package org.tron.MyController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyEntity.EntityColdWatch;
import org.tron.MyUiItem.EditCell;
import org.tron.MyUiItem.VoteItem;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.SQLiteUtil;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.util.HashMap;

public class VoteController {

    static final Logger logger = LoggerFactory.getLogger(VoteController.class);

    public Main.OverlayUI overlayUI;
    public TableView<VoteItem> voteTableView;
    private ObservableList<VoteItem> voteData = FXCollections.observableArrayList();

    public Protocol.Account account;
    public Client client = Client.getInstance();

    public TableColumn<VoteItem, String> addressCol;
    public TableColumn<VoteItem, String> voteCountCol;
    public TableColumn<VoteItem, String> myVoteCountCol;
    public TableColumn<VoteItem, String> urlCol;
    public TableColumn<VoteItem, String> totalProducedCol;
    public TableColumn<VoteItem, String> totalMissedCol;
    public TableColumn<VoteItem, String> latestBlockNumCol;

    // Called by FXMLLoader.
    public void initialize() {
        GrpcAPI.WitnessList witnessList = client.listWitnesses().get();
        if (witnessList == null) {
            return;
        }
        voteData.clear();
        for (Protocol.Witness witness : witnessList.getWitnessesList()) {
            String address = WalletClient.encode58Check(witness.getAddress().toByteArray());
            String voteCount = String.valueOf(witness.getVoteCount());
            String myVoteCount = String.valueOf(getMyCountByAddress(address));
            String url = witness.getUrl();
            String totalProduced = String.valueOf(witness.getTotalProduced());
            String totalMissed = String.valueOf(witness.getTotalMissed());
            String latestBlockNum = String.valueOf(witness.getLatestBlockNum());

            voteData.add(new VoteItem(address, voteCount, myVoteCount, url, totalProduced, totalMissed, latestBlockNum));
        }

        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        voteCountCol.setCellValueFactory(new PropertyValueFactory<>("voteCount"));
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        totalProducedCol.setCellValueFactory(new PropertyValueFactory<>("totalProduced"));
        totalMissedCol.setCellValueFactory(new PropertyValueFactory<>("totalMissed"));
        latestBlockNumCol.setCellValueFactory(new PropertyValueFactory<>("latestBlockNum"));

        myVoteCountCol.setCellValueFactory(new PropertyValueFactory<>("myVoteCount"));
        myVoteCountCol.setCellFactory(EditCell.forTableColumn());

        voteTableView.setItems(voteData);
        voteTableView.setEditable(true);
        voteTableView.refresh();
    }

    long getMyCountByAddress(String address) {
        account = ShareData.getAccount();
        if (account.getVotesCount() > 0) {
            for (Protocol.Vote vote : account.getVotesList()) {
                if (StringUtils.equals(address, WalletClient.encode58Check(vote.getVoteAddress().toByteArray()))) {
                    return vote.getVoteCount();
                }
            }
        }
        return 0;
    }

    public void reloadClicked(ActionEvent actionEvent) {
        initialize();
    }

    public void submitClicked(ActionEvent actionEvent) {
        submitClickedAndChecked();
    }

    public void submitClickedAndChecked() {
        account = ShareData.getAccount();
        HashMap<String, String> voteStatus = new HashMap<>();
        long totalVoteCount = 0;
        for (VoteItem voteDatum : voteData) {
            long count = Long.valueOf(voteDatum.myVoteCount.get());
            totalVoteCount += count;
            if (count > 0) {
                voteStatus.put(voteDatum.address.get(), String.valueOf(count));
            }
        }
        if (account.getFrozenCount() <= 0) {
            GuiUtils.informationalAlert("Not enought Tron Power", "Please frozen enought trx first");
            return;
        }
        long frozenCount = account.getFrozenList().get(0).getFrozenBalance() / Config.DROP_UNIT;
        if (totalVoteCount > frozenCount) {
            GuiUtils.informationalAlert("Not enought Tron Power", "My Tron Power:"
                    + (frozenCount)
                    + ", my vote count:"
                    + (totalVoteCount));
            return;
        }
        ShareData.tronPowerTmpSimpleObjectProperty.set(String.valueOf((frozenCount - totalVoteCount)));
        GrpcAPI.Return result = client.voteWitness(ShareData.getPassword(), voteStatus);
        if (result.getResult()) {
            GuiUtils.informationalAlert("Success", "Thank you");
        } else {
            GuiUtils.informationalAlert("Failed", result.getCode().toString());
            return;
        }
    }

    public void coldSubmitClicked(ActionEvent actionEvent) {
        EntityColdWatch coldWatch = SQLiteUtil.getEntityColdWatch();
        if (coldWatch == null || StringUtils.isEmpty(coldWatch.getAddress())) {
            GuiUtils.informationalAlert("No watched cold wallet address found", "Please add it in settings");
            return;
        }
        account = ShareData.getAccount();
        HashMap<String, String> voteStatus = new HashMap<>();
        long totalVoteCount = 0;
        for (VoteItem voteDatum : voteData) {
            long count = 0;
            try {
                count = Long.valueOf(voteDatum.myVoteCount.get());
            } catch (Exception e) {
            }
            totalVoteCount += count;
            if (count > 0) {
                voteStatus.put(voteDatum.address.get(), String.valueOf(count));
            }
        }
        if (account.getFrozenCount() <= 0) {
            GuiUtils.informationalAlert("Not enought Tron Power", "Please frozen enought trx first");
            return;
        }
        long frozenCount = account.getFrozenList().get(0).getFrozenBalance() / Config.DROP_UNIT;
        if (totalVoteCount > frozenCount) {
            GuiUtils.informationalAlert("Not enought Tron Power", "My Tron Power:"
                    + (frozenCount)
                    + ", my vote count:"
                    + (totalVoteCount));
            return;
        }
        ShareData.tronPowerTmpSimpleObjectProperty.set(String.valueOf((frozenCount - totalVoteCount)));
        try {
            Protocol.Transaction coldTransaction = client.createUnsignedVoteWitnessTransaction(coldWatch.getAddress(), voteStatus);
            Main.OverlayUI<ColdUnsignedVoteController> screen = Main.instance.overlayUI("cold_unsigned_vote.fxml");
            screen.controller.initialize(coldTransaction);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
