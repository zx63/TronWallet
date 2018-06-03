package org.tron.MyController;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;

public class TokenCreateController {

    static final Logger logger = LoggerFactory.getLogger(TokenCreateController.class);

    public Main.OverlayUI overlayUI;

    public TextField tokenName;
    public TextField tokenAbbr;
    public TextField supply;
    public TextField description;
    public TextField url;
    public TextField trxAmount;
    public TextField tokenAmount;
    public DatePicker startDate;
    public DatePicker endDate;
    public TextField forzenAmount;
    public TextField forzenDay;
    public Label price;
    public Button ok;

    public Protocol.Account account;

    float priceCalc;
    public Client client = Client.getInstance();

    // Called by FXMLLoader.
    public void initialize() {
        trxAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                long trxAmountValue = Long.parseLong(trxAmount.getText());
                long tokenAmountValue = Long.parseLong(tokenAmount.getText());
                priceCalc = 1.0f * trxAmountValue / tokenAmountValue;
                String priceString = new DecimalFormat(",###.######").format(priceCalc);
                price.setText(String.format("Token Price: 1 Token = %s TRX", priceString));
            } catch (Exception e) {
                price.setText("Token Price: 0 Token = 0 TRX");
            }
        });
        tokenAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                long trxAmountValue = Long.parseLong(trxAmount.getText());
                long tokenAmountValue = Long.parseLong(tokenAmount.getText());
                priceCalc = 1.0f * trxAmountValue / tokenAmountValue;
                String priceString = new DecimalFormat(",###.######").format(priceCalc);
                price.setText(String.format("Token Price: 1 Token = %s TRX", priceString));
            } catch (Exception e) {
                price.setText("Token Price: 0 Token = 0 TRX");
            }
        });
        new Thread(() -> {
            try {
                Thread.sleep(1000 * 10);
                Platform.runLater(() -> {
                    Main.instance.dump(startDate);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void cancelClicked(ActionEvent actionEvent) {
        overlayUI.done();
    }

    public void okClicked(ActionEvent actionEvent) {
        SimpleBooleanProperty checkPasswordProperty = new SimpleBooleanProperty(false);
        checkPasswordProperty.addListener((observable, oldValue, newValue) -> {
            okClickedAndChecked();
        });
        GuiUtils.checkPasswordAlert(checkPasswordProperty);
    }

    private void okClickedAndChecked() {
        if (StringUtils.isEmpty(tokenName.getText())
                || (StringUtils.length(tokenName.getText()) < 3)
                || (StringUtils.length(tokenName.getText()) > 8)) {
            GuiUtils.informationalAlert("Error", "Token name length should be between 3 and 8");
            return;
        }
        if (StringUtils.isEmpty(tokenAbbr.getText())
                || (StringUtils.length(tokenAbbr.getText()) < 3)
                || (StringUtils.length(tokenName.getText()) > 8)) {
            GuiUtils.informationalAlert("Error", "Token abbreviation length should be between 3 and 8");
            return;
        }
        if (StringUtils.isEmpty(supply.getText())) {
            GuiUtils.informationalAlert("Error", "Supply is empty");
            return;
        } else {
            try {
                long supplyValue = Long.parseLong(supply.getText());
                if (supplyValue <= 0) {
                    GuiUtils.informationalAlert("Error", "Total supply should greater than 0");
                    return;
                }
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }
        if (StringUtils.isEmpty(description.getText())
                || (StringUtils.length(description.getText()) < 8)
                || (StringUtils.length(description.getText()) > 128)) {
            GuiUtils.informationalAlert("Error", "Description length should be between 8 and 128");
            return;
        }
        if (StringUtils.isEmpty(url.getText())
                || (StringUtils.length(url.getText()) < 8)
                || (StringUtils.length(url.getText()) > 128)) {
            GuiUtils.informationalAlert("Error", "URL length should be between 8 and 128");
            return;
        }
        if (StringUtils.isEmpty(trxAmount.getText())) {
            GuiUtils.informationalAlert("Error", "TRX amount is empty");
            return;
        } else {
            try {
                long supplyValue = Long.parseLong(trxAmount.getText());
                if (supplyValue <= 0) {
                    GuiUtils.informationalAlert("Error", "TRX amount should greater than 0");
                    return;
                }
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }
        if (StringUtils.isEmpty(tokenAmount.getText())) {
            GuiUtils.informationalAlert("Error", "Token amount is empty");
            return;
        } else {
            try {
                long supplyValue = Long.parseLong(tokenAmount.getText());
                if (supplyValue <= 0) {
                    GuiUtils.informationalAlert("Error", "Token amount should greater than 0");
                    return;
                }
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }
        LocalDate startDateLocalDate = null;
        String startDateString = null;
        long startDateLong = 0;
        if (StringUtils.isEmpty(startDate.getEditor().getText())) {
            GuiUtils.informationalAlert("Error", "Start date is empty");
            return;
        } else {
            try {
                startDateLocalDate = startDate.getValue();
                Calendar c = Calendar.getInstance();
                c.set(startDateLocalDate.getYear(), startDateLocalDate.getMonthValue() - 1, startDateLocalDate.getDayOfMonth());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                startDateString = sdf.format(c.getTime());
                startDateLong = c.getTimeInMillis();
                if (startDateLong <= System.currentTimeMillis()) {
                    GuiUtils.informationalAlert("Error", "Start date should greater than today");
                    return;
                }
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }
        LocalDate endDateLocalDate = null;
        String endDateString = null;
        long endDateLong = 0;
        if (StringUtils.isEmpty(endDate.getEditor().getText())) {
            GuiUtils.informationalAlert("Error", "End date is empty");
            return;
        } else {
            try {
                endDateLocalDate = endDate.getValue();
                Calendar c = Calendar.getInstance();
                c.set(endDateLocalDate.getYear(), endDateLocalDate.getMonthValue() - 1, endDateLocalDate.getDayOfMonth());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                endDateString = sdf.format(c.getTime());
                endDateLong = c.getTimeInMillis();
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }
        if (endDateLocalDate.isBefore(startDateLocalDate)) {
            GuiUtils.informationalAlert("Error", "End date shoud greater than start date");
            return;
        }


        if (StringUtils.isEmpty(forzenAmount.getText())) {
            GuiUtils.informationalAlert("Error", "Frozen amount is empty");
            return;
        } else {
            try {
                long forzenValue = Long.parseLong(forzenAmount.getText());
                if (forzenValue < 0) {
                    GuiUtils.informationalAlert("Error", "Frozen amount should greater or equal 0");
                    return;
                }
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }
        if (StringUtils.isEmpty(forzenDay.getText())) {
            GuiUtils.informationalAlert("Error", "Frozen day is empty");
            return;
        } else {
            try {
                long forzenValue = Long.parseLong(forzenDay.getText());
                if (forzenValue < 0) {
                    GuiUtils.informationalAlert("Error", "Frozen day should greater or equal 0");
                    return;
                }
            } catch (Exception e) {
                GuiUtils.informationalAlert("Error", e.getMessage());
                return;
            }
        }


        try {
            HashMap<String, String> frozenSupply = new HashMap<>();
            frozenSupply.put(forzenAmount.getText(), forzenDay.getText());

            GrpcAPI.Return result = client.assetIssue(ShareData.getPassword(),
                    tokenName.getText(),
                    Long.parseLong(supply.getText()),
                    Integer.parseInt(trxAmount.getText()),
                    Integer.parseInt(tokenAmount.getText()),
                    startDateLong,
                    endDateLong,
                    0,
                    description.getText(),
                    url.getText(),
                    org.tron.core.config.Parameter.ChainConstant.ONE_DAY_NET_LIMIT / 10,
                    org.tron.core.config.Parameter.ChainConstant.ONE_DAY_NET_LIMIT / 10,
                    frozenSupply);
            if (result.getResult()) {
                GuiUtils.informationalAlert("Success", "Asset issue success");
                tokenName.setText("");
                tokenAbbr.setText("");
                description.setText("");
                supply.setText("");
                url.setText("http://");
                trxAmount.setText("1");
                tokenAmount.setText("1");
                forzenAmount.setText("0");
                forzenDay.setText("1");
                startDate.getEditor().setText("");
                endDate.getEditor().setText("");
                return;
            } else {
                GuiUtils.informationalAlert("Failed", result.getCode().toString());
                return;
            }
        } catch (Exception e) {
            GuiUtils.informationalAlert("Failed", e.getMessage());
            return;
        }
    }

}
