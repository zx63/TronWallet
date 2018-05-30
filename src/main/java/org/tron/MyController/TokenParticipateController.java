package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;

import java.text.DecimalFormat;

import static org.tron.MyUtils.ShareData.balanceSimpleObjectProperty;
import static org.tron.MyUtils.ShareData.freezeSimpleObjectProperty;

public class TokenParticipateController {

    static final Logger logger = LoggerFactory.getLogger(TokenParticipateController.class);

    public Main.OverlayUI overlayUI;

    public Label frozen;
    public Label balance;

    public Label name;
    public Label totalSupply;
    public Label issuer;
    public Label desc;
    public Label price;
    public TextField amount;
    public CheckBox agree;

    public Protocol.Account account;

    TokenController.XCellData xCellData;

    float priceCalc;
    public Client client = Client.getInstance();


    // Called by FXMLLoader.
    public void initialize(TokenController.XCellData item) {
        xCellData = item;
        balanceSimpleObjectProperty.addListener((observable, oldValue, newValue) -> balance.setText(newValue));
        freezeSimpleObjectProperty.addListener((observable, oldValue, newValue) -> frozen.setText(newValue));
        frozen.setText(ShareData.getFrozenBalance());
        balance.setText(ShareData.getBalance());

        name.setText(xCellData.name);
        issuer.setText(xCellData.issuer);
        price.setText(new DecimalFormat(",###.######").format(xCellData.totalSupply));
        desc.setText(xCellData.desc);
        priceCalc = 1.0f * (xCellData.trxNum / Config.DROP_UNIT) / xCellData.num;
        price.setText(new DecimalFormat(",###.######").format(priceCalc));
        amount.setText("0");
        amount.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                long count = Long.parseLong(newValue);
                agree.setText("Confirmed to spend " + (priceCalc * count) + " TRX and get " + newValue + " tokens.");
            } catch (Exception e) {
                GuiUtils.informationalAlert("Invalid vote myVote", "The myVote:" + newValue);
                amount.setText("0");
            }
        });

    }

    public void cancelClicked(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void okClicked(ActionEvent actionEvent) {
        if (agree.isSelected()) {
            long count = Long.parseLong(amount.getText());
            GrpcAPI.Return result = client.participateAssetIssue(ShareData.getPassword(), xCellData.issuer, xCellData.name, count);
            if (result.getResult()) {
                GuiUtils.informationalAlert("Success", "Get " + count + " " + xCellData.name);
            } else {
                GuiUtils.informationalAlert("Failed", result.getCode().toString());
                return;
            }
        }
    }

}
