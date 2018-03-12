package us.nagro.august.caseapp.elements;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper;
import us.nagro.august.caseapp.models.EntryFinishedEvent;
import us.nagro.august.caseapp.models.EntryModel;
import us.nagro.august.caseapp.models.KdbxContext;
import us.nagro.august.caseapp.models.RoutingEvent;
import us.nagro.august.caseapp.utils.SearchEngine;
import us.nagro.august.caseapp.prefs.UserPreferences;
import us.nagro.august.caseapp.utils.ViewBuilderFactory;

import java.io.IOException;

public class HomePaneDesktop extends HomePane {

    @FXML private TextField                 search;
    @FXML private TreeView<DomGroupWrapper> groupListTV;
    @FXML private ListView<EntryModel>      entryListView;

    @FXML private VBox selectedEntryVbox;

    private KdbxContext kdbxContext;
    private EntryModel  selectedEntry;

    private EntryPane                  entryPane       = ViewBuilderFactory.getViewBuilder().entry();
    private ObservableList<EntryModel> entries;
    private FilteredList<EntryModel>   filteredEntries;

    // todo refactor initialization to helper methods
    public HomePaneDesktop(KdbxContext kdbxContext) {
        this.kdbxContext = kdbxContext;

        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/desktop/home.fxml"));
        fxml.setRoot(this);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        entries = kdbxContext.entries;
        filteredEntries = new FilteredList<>(entries);

        DomGroupWrapper rootGroup = kdbxContext.db.getRootGroup();
        TreeItem<DomGroupWrapper> rootNode = new TreeItem<>(rootGroup);
        rootNode.setExpanded(true);
        buildTreeView(rootGroup, rootNode);

        groupListTV.setCellFactory(treeView -> new GroupTreeCell());
        groupListTV.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                filteredEntries.setPredicate(entry -> {
                    if (newSelection == null) return true;

                    if (UserPreferences.getShowEntriesFromAllSubgroups()) {
                        if (newSelection.getValue().isRootGroup()) return true;
                        DomGroupWrapper group = entry.groupProperty().get();
                        while (!group.isRootGroup()) {
                            if (group.equals(newSelection.getValue())) return true;
                            group = group.getParent();
                        }
                        return false;
                    }

                    return entry.groupProperty().get().equals(newSelection.getValue());
                }));
        groupListTV.setRoot(rootNode);

        entryListView.setCellFactory(listView -> new EntryModelListCell());
        entryListView.setItems(filteredEntries);
        entryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) showEntry(newSelection);
        });

        showRootEntries();

        addEventHandler(EntryFinishedEvent.DELETED, event -> {
            if (selectedEntry.groupProperty().get().isRecycleBin()) entries.remove(selectedEntry);
            // re-filter the entries
            filteredEntries.setPredicate(filteredEntries.getPredicate());
        });

        search.textProperty().addListener((obs, oldSearch, newSearch) -> {
            if (newSearch == null) showRootEntries();
            else SearchEngine.filter(newSearch, filteredEntries);
        });

        scheduleLockoutTimer();
    }

    @Override
    public void lock(ActionEvent actionEvent) {
        fireEvent(new RoutingEvent(RoutingEvent.LOG_OUT, kdbxContext));
    }

    @Override
    public void newEntry(ActionEvent actionEvent) {
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setContentText("Enter entry title");
        titleDialog.showAndWait().ifPresent(title -> {
            DomGroupWrapper selectedGroup = groupListTV.getSelectionModel().getSelectedItem().getValue();
            DomEntryWrapper newEntry = selectedGroup.addEntry(kdbxContext.db.newEntry(title));
            EntryModel entryModel = new EntryModel(newEntry, kdbxContext);
            entries.add(entryModel);
            showEntry(entryModel);

            // selection model listener has already fired; need to filter again
            filteredEntries.setPredicate(filteredEntries.getPredicate());
            kdbxContext.save();
        });
    }

    @Override
    public void newGroup(ActionEvent actionEvent) {
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setContentText("Enter group title");
        titleDialog.showAndWait().ifPresent(title -> {
            TreeItem<DomGroupWrapper> selectedTreeItem = groupListTV.getSelectionModel().getSelectedItem();
            DomGroupWrapper newGroup = selectedTreeItem.getValue().addGroup(kdbxContext.db.newGroup(title));
            TreeItem<DomGroupWrapper> newGroupTreeItem = new TreeItem<>(newGroup);
            selectedTreeItem.getChildren().add(newGroupTreeItem);

            // now select created group
            groupListTV.getSelectionModel().select(newGroupTreeItem);
            kdbxContext.save();
        });
    }

    @Override
    public void goPreferences(ActionEvent ae) {
        fireEvent(new RoutingEvent(RoutingEvent.PREFERENCES, kdbxContext));
    }

    @Override
    public void selectSearch(ActionEvent ae) {
        search.requestFocus();
    }

    @Override
    public void recycleBin(ActionEvent ae) {
        //todo show recycle bin contents
    }

    private void scheduleLockoutTimer() {
        if (UserPreferences.getisAutoLockoutEnabled()) {
            int lockoutMin = UserPreferences.getAutoLockoutMinutes();
            Timeline autoLockout = new Timeline(new KeyFrame(Duration.minutes(lockoutMin), this::lock));
            autoLockout.play();
        }
    }

    private void buildTreeView(DomGroupWrapper group, TreeItem<DomGroupWrapper> node) {
        for (DomGroupWrapper subGroup : group.getGroups()) {
            if (subGroup.getName().length() > 0) {
                TreeItem<DomGroupWrapper> subNode = new TreeItem<>(subGroup);
                subNode.setExpanded(true);
                node.getChildren().add(subNode);
                buildTreeView(subGroup, subNode);
            }
        }
    }

    private void showEntry(EntryModel entry) {
        if (entryPane.hasDiverged()) {
            new Alert(Alert.AlertType.WARNING, "Unsaved Changes").showAndWait();
            return;
        }
        selectedEntry = entry;
        entryPane.initialize(entry);

        // add entry view to scenegraph if not already present
        if (selectedEntryVbox.getChildren().size() == 0) selectedEntryVbox.getChildren().setAll(entryPane);
    }

    private void showRootEntries() {
        groupListTV.getSelectionModel().selectFirst();
    }

}
