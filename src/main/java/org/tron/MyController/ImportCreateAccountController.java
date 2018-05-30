package org.tron.MyController;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyUtils.ShareData;

public class ImportCreateAccountController {

    private static final Logger log = LoggerFactory.getLogger(ImportCreateAccountController.class);

    public Main.OverlayUI overlayUI;
    public CheckBox cold;

    public void createClicked(ActionEvent actionEvent) {
        Main.OverlayUI<CreateAccountController> screen = Main.instance.overlayUI("create_account.fxml");
        screen.controller.initialize(cold.isSelected());
    }

    public void importClicked(ActionEvent actionEvent) {
        Main.OverlayUI<ImportAccountController> screen = Main.instance.overlayUI("import_account.fxml");
        screen.controller.initialize(cold.isSelected());
    }
}
