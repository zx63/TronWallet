package org.tron.MyUiItem;

import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditCell<S, T> extends TextFieldTableCell<S, T> {

    static final Logger logger = LoggerFactory.getLogger(EditCell.class);
    private TextField textField;
    private boolean escapePressed = false;
    private TablePosition<S, ?> tablePos = null;

    public EditCell(final StringConverter<T> converter) {
        super(converter);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final StringConverter<T> converter) {
        return list -> new EditCell<S, T>(converter);
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable()
                || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = getTextField();
            }
            escapePressed = false;
            startEdit(textField);
            final TableView<S> table = getTableView();
            tablePos = table.getEditingCell();
        }
    }

    private String getNumber(String value) {
        try {
            return String.valueOf(Long.parseLong(value));
        } catch (Exception e) {
            return "0";
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void commitEdit(T newValue) {
        if (!isEditing())
            return;
        final TableView<S> table = getTableView();
        if (table != null) {
            // Inform the TableView of the edit being ready to be committed.
            CellEditEvent editEvent = new CellEditEvent(table, tablePos,
                    TableColumn.editCommitEvent(), newValue);

            Event.fireEvent(getTableColumn(), editEvent);
        }
        // we need to setEditing(false):
        super.cancelEdit(); // this fires an invalid EditCancelEvent.
        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);
        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        if (escapePressed) {
            // this is a cancel event after escape key
            super.cancelEdit();
            setText(getItemText()); // restore the original text in the view
        } else {
            // this is not a cancel event after escape key
            // we interpret it as commit.
            String newText = textField.getText();
            // commit the new text to the model
            this.commitEdit(getConverter().fromString(getNumber(newText)));
        }
        setGraphic(null); // stop editing with TextField
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateItem();
    }

    private TextField getTextField() {

        final TextField textField = new TextField(getItemText());

        // Use onAction here rather than onKeyReleased (with check for Enter),
        textField.setOnAction(event -> {
            logger.info("setOnAction {}", event);
            if (getConverter() == null) {
                throw new IllegalStateException("StringConverter is null.");
            }

            String newText = textField.getText();
            this.commitEdit(getConverter().fromString(getNumber(newText)));
            event.consume();
        });

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("focusedProperty {} {}", newValue, textField.getText());
            if (!newValue) {
                String newText = textField.getText();
                commitEdit(getConverter().fromString(getNumber(newText)));
            }
        });

        textField.setOnKeyPressed(t -> {
            logger.debug("setOnKeyPressed {}", t);
            if (t.getCode() == KeyCode.ESCAPE)
                escapePressed = true;
            else
                escapePressed = false;
        });
        textField.setOnKeyReleased(t -> {
            logger.debug("setOnKeyReleased {}", t);
            if (t.getCode() == KeyCode.ESCAPE) {
                throw new IllegalArgumentException(
                        "did not expect esc key releases here.");
            }
        });

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            logger.info("addEventFilter {}", event);
            if (event.getCode() == KeyCode.ESCAPE) {
                textField.setText(getConverter().toString(getItem()));
                cancelEdit();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT
                    || event.getCode() == KeyCode.TAB) {
                getTableView().getSelectionModel().selectNext();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                getTableView().getSelectionModel().selectPrevious();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                getTableView().getSelectionModel().selectAboveCell();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                getTableView().getSelectionModel().selectBelowCell();
                event.consume();
            }
        });

        return textField;
    }

    private String getItemText() {
        return getConverter() == null
                ? getItem() == null ? "" : getItem().toString()
                : getConverter().toString(getItem());
    }

    private void updateItem() {
        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItemText());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItemText());
                setGraphic(null);
            }
        }
    }

    private void startEdit(final TextField textField) {
        if (textField != null) {
            textField.setText(getItemText());
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
        // requesting focus so that key input can immediately go into the
        // TextField
        textField.requestFocus();
    }
}