package us.nagro.august.caseapp.models;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.util.StringConverter;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper;
import us.nagro.august.caseapp.utils.DateConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.beans.binding.Bindings.createBooleanBinding;

/**
 * This class provides bindings to Entry properties, and the ability to persist changes to the keystore.
 * Each property is only allocated when accessed.
 *
 * Methods of interest include {@link EntryModel#save()}, {@link EntryModel#resetChanges()}, and the
 * read-only {@link EntryModel#hasDiverged}, indicating whether this EntryModel has changed from its underlying Entry.
 *
 * todo can probably remove the lazy initialization... search requires 3 fields at minimum
 */
public class EntryModel {

    public static final StringConverter<DomGroupWrapper> GROUP_STRING_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(DomGroupWrapper group) {
            if (group.isRootGroup()) return group.getDatabase().getName();
            return group.getName();
        }

        @Override
        public DomGroupWrapper fromString(String string) {
            return null;
        }
    };

    private ObjectProperty<DomGroupWrapper> group;

    // standard properties
    private StringProperty            title;
    private StringProperty            username;
    private StringProperty            password;
    private StringProperty            website;
    private StringProperty            notes;
    private ObjectProperty<LocalDate> expires;

    private ObservableMap<String, StringProperty>     customProperties;
    private MapChangeListener<String, StringProperty> mcl;

    // hasDiverged is true if the number of properties has changed,
    // a standard property's value changes, or a custom property's
    // value changed. This is useful for resetting the entry.
    private SimpleBooleanProperty           propLengthChanged;
    private BooleanBinding                  standardPropDivergenceBinding;
    private HashMap<String, BooleanBinding> customPropDivergenceBindings;

    private final ReadOnlyBooleanWrapper  hasDivergedWrapper;
    public final  ReadOnlyBooleanProperty hasDiverged;

    private final DomEntryWrapper entry;
    private final KdbxContext     ctx;

    public EntryModel(KdbxContext ctx) {
        this(ctx.db.newEntry(), ctx);
    }

    public EntryModel(DomEntryWrapper entry, KdbxContext ctx) {
        this.entry = entry;
        this.ctx = ctx;
        propLengthChanged = new SimpleBooleanProperty(false);
        standardPropDivergenceBinding = createBooleanBinding(() -> false);
        customPropDivergenceBindings = new HashMap<>();
        hasDivergedWrapper = new ReadOnlyBooleanWrapper(false);
        hasDiverged = hasDivergedWrapper.getReadOnlyProperty();
    }

    public KdbxContext getContext() {
        return ctx;
    }

    public ObjectProperty<DomGroupWrapper> groupProperty() {
        if (group == null) {
            group = new SimpleObjectProperty<>(entry.getParent());
            addStandardPropDivergenceBinding(group.isNotEqualTo(entry.getParent()));
        }
        return group;
    }

    public LocalDate getCreated() {
        return DateConverter.convert(entry.getCreationTime());
    }

    public LocalDate getUpdated() {
        return DateConverter.convert(entry.getLastModificationTime());
    }

    public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty(entry.getTitle());
            addStandardPropDivergenceBinding(title.isNotEqualTo(entry.getTitle()));
        }
        return title;
    }

    public StringProperty usernameProperty() {
        if (username == null) {
            username = new SimpleStringProperty(entry.getUsername());
            addStandardPropDivergenceBinding(username.isNotEqualTo(entry.getUsername()));
        }
        return username;
    }

    public StringProperty passwordProperty() {
        if (password == null) {
            password = new SimpleStringProperty(entry.getPassword());
            addStandardPropDivergenceBinding(password.isNotEqualTo(entry.getPassword()));
        }
        return password;
    }

    public StringProperty websiteProperty() {
        if (website == null) {
            website = new SimpleStringProperty(entry.getUrl());
            addStandardPropDivergenceBinding(website.isNotEqualTo(entry.getUrl()));
        }
        return website;
    }

    public StringProperty notesProperty() {
        if (notes == null) {
            notes = new SimpleStringProperty(entry.getNotes());
            addStandardPropDivergenceBinding(notes.isNotEqualTo(entry.getNotes()));
        }
        return notes;
    }

    public ObjectProperty<LocalDate> expiresProperty() {
        if (expires == null) {
            LocalDate expiryTime = entry.getExpires() ? DateConverter.convert(entry.getExpiryTime()) : null;
            expires = new SimpleObjectProperty<>(expiryTime);
            addStandardPropDivergenceBinding(expires.isNotEqualTo(expiryTime));
        }
        return expires;
    }

    public ObservableMap<String, StringProperty> getCustomProperties() {
        if (customProperties == null) {
            customProperties = FXCollections.observableHashMap();
            buildCustomProperties();
        }
        return customProperties;
    }

    public void save() {
        if (!hasDiverged.get()) return;

        // we need to re-evaluate the standard divergence bindings
        hasDivergedWrapper.unbind();
        standardPropDivergenceBinding = createBooleanBinding(() -> false);

        // todo usagecount, lastmodifiedTime, other properties
        if (group != null && !group.get().equals(entry.getParent())) {
            group.get().addEntry(entry);
            addStandardPropDivergenceBinding(group.isNotEqualTo(group.get()));
        }
        if (title != null) {
            entry.setTitle(title.get());
            addStandardPropDivergenceBinding(title.isNotEqualTo(title.get()));
        }
        if (username != null) {
            entry.setUsername(username.get());
            addStandardPropDivergenceBinding(username.isNotEqualTo(username.get()));
        }
        if (password != null) {
            entry.setPassword(password.get());
            addStandardPropDivergenceBinding(password.isNotEqualTo(password.get()));
        }
        if (website != null) {
            entry.setUrl(website.get());
            addStandardPropDivergenceBinding(website.isNotEqualTo(website.get()));
        }
        if (notes != null) {
            entry.setNotes(notes.get());
            addStandardPropDivergenceBinding(notes.isNotEqualTo(notes.get()));
        }
        if (expires != null) {
            if (expires.get() == null) {
                entry.setExpires(false);
            } else {
                entry.setExpires(true);
                entry.setExpiryTime(DateConverter.convert(expires.get()));
            }
            addStandardPropDivergenceBinding(expires.isNotEqualTo(expires.get()));
        }

        if (customProperties != null) {
            // set all custom properties
            customProperties.forEach((k, v) -> entry.setProperty(k, v.get()));
            // and remove those no longer present, except for standard properties
            List<String> propNames = entry.getPropertyNames();
            propNames.removeAll(Entry.STANDARD_PROPERTY_NAMES);
            for (String propName : propNames) {
                if (!customProperties.containsKey(propName)) entry.removeProperty(propName);
            }
        }

        ctx.save();
        if (customProperties != null) buildCustomProperties();
    }

    /**
     * Resets changes made after last save.
     */
    public void resetChanges() {
        if (group != null) group.set(entry.getParent());

        if (title != null) title.set(entry.getTitle());
        if (username != null) username.set(entry.getUsername());
        if (password != null) password.set(entry.getPassword());
        if (website != null) website.set(entry.getUrl());
        if (notes != null) notes.set(entry.getNotes());
        if (expires != null) expires.set(entry.getExpires() ? DateConverter.convert(entry.getExpiryTime()) : null);

        if (customProperties != null) buildCustomProperties();
    }

    /**
     * Moves this entry to the Recycle Bin, if the Recycle Bin is enabled.
     * If the entry's group is the recycle bin, then permanently delete.
     * This EntryModel is invalid after calling delete().
     */
    public void delete() {
        DomGroupWrapper parent = entry.getParent();
        parent.removeEntry(entry);
        if (ctx.db.isRecycleBinEnabled()) {
            ctx.db.getRecycleBin().addEntry(entry);
        }
        ctx.entries.remove(this);
    }

    private void buildCustomProperties() {
        if (mcl != null) customProperties.removeListener(mcl);
        customPropDivergenceBindings.clear();
        propLengthChanged.set(false);
        customProperties.clear();

        List<String> propertyNames = entry.getPropertyNames();
        propertyNames.removeAll(Entry.STANDARD_PROPERTY_NAMES);

        for (String propertyName : propertyNames) {
            String propValue = entry.getProperty(propertyName);
            SimpleStringProperty customProp = new SimpleStringProperty(propValue);
            addCustomPropDivergenceBinding(propertyName, customProp.isNotEqualTo(propValue));
            customProperties.put(propertyName, customProp);
        }

        mcl = change -> {
            propLengthChanged.set(change.getMap().size() != propertyNames.size());
            if (change.wasAdded()) {
                addCustomPropDivergenceBinding(change.getKey(), change.getValueAdded().isNotEqualTo(change.getValueAdded().get()));
            } else {
                removeCustomDivergenceBinding(change.getKey());
            }
        };

        customProperties.addListener(mcl);
    }

    /**
     * Rebinds hasDiverged to include this BooleanBinding
     */
    private void addStandardPropDivergenceBinding(BooleanBinding binding) {
        hasDivergedWrapper.unbind();
        standardPropDivergenceBinding = standardPropDivergenceBinding.or(binding);
        BooleanBinding nonStandardBinding = buildCustomPropDivergenceBinding();
        hasDivergedWrapper.bind(standardPropDivergenceBinding.or(nonStandardBinding).or(propLengthChanged));
    }

    /**
     * Rebinds hasDiverged to include the divergence binding for this custom property
     */
    private void addCustomPropDivergenceBinding(String propName, BooleanBinding binding) {
        hasDivergedWrapper.unbind();
        customPropDivergenceBindings.put(propName, binding);
        BooleanBinding nonStandardBinding = buildCustomPropDivergenceBinding();
        hasDivergedWrapper.bind(standardPropDivergenceBinding.or(nonStandardBinding).or(propLengthChanged));
    }

    /**
     * Rebinds hasDiverged to remove the divergence binding for this custom property
     */
    private void removeCustomDivergenceBinding(String propName) {
        hasDivergedWrapper.unbind();
        customPropDivergenceBindings.remove(propName);
        BooleanBinding nonStandardBinding = buildCustomPropDivergenceBinding();
        hasDivergedWrapper.bind(standardPropDivergenceBinding.or(nonStandardBinding).or(propLengthChanged));
    }

    private BooleanBinding buildCustomPropDivergenceBinding() {
        return customPropDivergenceBindings.entrySet().stream()
                .map(Map.Entry::getValue)
                .reduce(BooleanBinding::or)
                .orElse(createBooleanBinding(() -> false));
    }
}
