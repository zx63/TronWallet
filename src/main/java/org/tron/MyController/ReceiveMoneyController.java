package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.QRUtil;
import org.tron.MyUtils.ShareData;

public class ReceiveMoneyController {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveMoneyController.class);
    public Main.OverlayUI overlayUI;
    @FXML
    TextField address;
    @FXML
    ImageView qrcode;


    public void closeClicked(ActionEvent event) {
        overlayUI.done();
    }

    public void initialize() {
        ShareData.addressSimpleObjectProperty.addListener((observable, oldValue, newValue) -> address.setText(newValue));
        qrcode.setImage(QRUtil.getImage(ShareData.getAddress()));
        address.setText(ShareData.getAddress());
    }

}
