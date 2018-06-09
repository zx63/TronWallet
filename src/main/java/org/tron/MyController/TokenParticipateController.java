package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUiItem.TokenItem;
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
    public PasswordField password;

    public Protocol.Account account;

    TokenItem tokenItem;

    float priceCalc;
    public Client client = Client.getInstance();


    // Called by FXMLLoader.
    public void initialize(TokenItem item) {
        tokenItem = item;
        balanceSimpleObjectProperty.addListener((observable, oldValue, newValue) -> balance.setText(newValue));
        freezeSimpleObjectProperty.addListener((observable, oldValue, newValue) -> frozen.setText(newValue));
        frozen.setText(ShareData.getFrozenBalance());
        balance.setText(ShareData.getBalance());

        name.setText(item.getName());
        issuer.setText(item.getIssuer());
        totalSupply.setText(new DecimalFormat(",###.######").format(Long.valueOf(item.getTotalSupply())));
        desc.setText(item.getDesc());
        priceCalc = 1.0f * (Long.valueOf(item.getTrxNum()) / Config.DROP_UNIT) / Long.valueOf(item.getNum());
        price.setText(new DecimalFormat(",###.######").format(priceCalc));
        amount.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                long count = Long.parseLong(newValue);
                agree.setText("Confirmed to spend " + (priceCalc * count) + " TRX and get " + newValue + " tokens.");
            } catch (Exception e) {
                GuiUtils.informationalAlert("Invalid amount", "The amount:" + newValue);
                amount.setText("");
            }
        });

    }

    public void cancelClicked(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void okClicked(ActionEvent actionEvent) {
        okClickedAndChecked();
    }

    public void okClickedAndChecked() {
        if (!client.checkPassword(password.getText())) {
            GuiUtils.informationalAlert("Failed", "Wrong password");
            return;
        }
        if (agree.isSelected()) {
            long count = Long.parseLong(amount.getText());
            if (count == 0) {
                return;
            }
            GrpcAPI.Return result = client.participateAssetIssue(password.getText(), tokenItem.getIssuer(), tokenItem.getName(), count * Config.DROP_UNIT);
            if (result.getResult()) {
                GuiUtils.informationalAlert("Success", "Get " + count + " " + tokenItem.getName());
            } else {
                GuiUtils.informationalAlert("Failed", result.getCode().toString());
                return;
            }
        }
    }

}
