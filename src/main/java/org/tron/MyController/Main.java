package org.tron.MyController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.tron.MyUtils.Config;
import org.tron.MyUtils.SQLiteUtil;
import org.tron.MyUtils.ShareData;
import org.tron.controls.NotificationBarPane;
import org.tron.MyEntity.EntityMeta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class Main extends Application {
    public static String APP_NAME = "TronWallet";
    public static Main instance;
    public NotificationBarPane notificationBar;
    public Stage mainWindow;
    public StackPane uiStack;
    private Pane mainUI;
    private Node stopClickPane = new Pane();
    @Nullable
    private OverlayUI currentOverlay;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage mainWindow) throws Exception {
        File dir = new File(Config.WALLET_PATH);
        dir.mkdirs();
        try {
            realStart(mainWindow);
        } catch (Throwable e) {
            GuiUtils.crashAlert(e);
            throw e;
        }
    }

    private void realStart(Stage mainWindow) throws IOException, SQLException {
        this.mainWindow = mainWindow;
        instance = this;
        // Show the crash dialog for any exceptions that we don't handle and that hit the main loop.
        GuiUtils.handleCrashesOnThisThread();

        // Load the GUI. The MainController class will be automagically created and wired up.

//        SQLiteJDBC.init(Config.WALLET_DB_FILE_BAK);
        SQLiteUtil.init(Config.WALLET_DB_FILE);
        EntityMeta entityMeta = SQLiteUtil.getMetaEntity();

        URL location = getClass().getResource("main.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        mainUI = loader.load();
        Object controller = loader.getController();

        notificationBar = new NotificationBarPane(mainUI);
        mainWindow.setTitle(APP_NAME);

        uiStack = new StackPane();

        Scene scene = new Scene(uiStack);
        scene.getStylesheets().add(getClass().getResource("wallet.css").toString());
        uiStack.getChildren().add(notificationBar);

        mainWindow.setScene(scene);
        mainWindow.initStyle(StageStyle.UNDECORATED);
//        scene.setFill(Color.TRANSPARENT);

        mainWindow.show();

        if (entityMeta != null) {
            ShareData.isCold.set(entityMeta.cold != 0);
            ShareData.setAddress(entityMeta.address);
            overlayUI("login.fxml");
        } else {
            overlayUI("create_wallet.fxml");
        }
        notificationBar.pushItem(Config.WALLET_DB_FILE, null);
    }

    public <T> OverlayUI<T> overlayUI(Node node, T controller) {
        GuiUtils.checkGuiThread();
        OverlayUI<T> pair = new OverlayUI<T>(node, controller);
        // Auto-magically set the overlayUI member, if it's there.
        try {
            controller.getClass().getField("overlayUI").set(controller, pair);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        pair.show();
        return pair;
    }

    /**
     * Loads the FXML file with the given name, blurs out the main UI and puts this one on top.
     */
    public <T> OverlayUI<T> overlayUI(String name) {
        try {
            GuiUtils.checkGuiThread();
            // Load the UI from disk.
            URL location = GuiUtils.getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();
            T controller = loader.getController();
            OverlayUI<T> pair = new OverlayUI<T>(ui, controller);
            // Auto-magically set the overlayUI member, if it's there.
            try {
                if (controller != null)
                    controller.getClass().getField("overlayUI").set(controller, pair);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
                ignored.printStackTrace();
            }
            pair.show();
            return pair;
        } catch (IOException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }

    public void max() {
        mainWindow.setMaximized(true);
    }

    @Override
    public void stop() {
        Runtime.getRuntime().exit(0);
    }

    public void reset() {
        ShareData.reset();
        overlayUI("create_wallet.fxml");
    }

    public class OverlayUI<T> {
        public Node ui;
        public T controller;

        public OverlayUI(Node ui, T controller) {
            this.ui = ui;
            this.controller = controller;
        }

        public synchronized void show() {
            GuiUtils.checkGuiThread();
            if (currentOverlay == null) {
                uiStack.getChildren().add(stopClickPane);
                uiStack.getChildren().add(ui);
                GuiUtils.blurOut(mainUI);
                GuiUtils.fadeIn(ui);
                GuiUtils.zoomIn(ui);
            } else {
                GuiUtils.explodeOut(currentOverlay.ui);
                GuiUtils.fadeOutAndRemove(uiStack, currentOverlay.ui);
                uiStack.getChildren().add(ui);
                ui.setOpacity(0.0);
                GuiUtils.fadeIn(ui, 100);
                GuiUtils.zoomIn(ui, 100);
            }
            currentOverlay = this;
        }

        public void outsideClickDismisses() {
            stopClickPane.setOnMouseClicked((ev) -> done());
        }

        public synchronized void done() {
            GuiUtils.checkGuiThread();
            if (ui == null) return;  // In the middle of being dismissed and got an extra click.
            GuiUtils.explodeOut(ui);
            GuiUtils.fadeOutAndRemove(Duration.ZERO, uiStack, ui, stopClickPane);
            GuiUtils.blurIn(mainUI);
            this.ui = null;
            this.controller = null;
            currentOverlay = null;
        }
    }

    public void dump(Node n) {
        dump(n, 0);
    }

    private void dump(Node n, int depth) {
        for (int i = 0; i < depth; i++) System.out.print("  ");
        System.out.println(n);
        if (n instanceof Parent)
            for (Node c : ((Parent) n).getChildrenUnmodifiable())
                dump(c, depth + 1);
    }
}
