package us.nagro.august.caseapp.models;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * States of EntryPane requiring its Parent take action.
 */
public class EntryFinishedEvent extends Event {
    public static final EventType<EntryFinishedEvent> DELETED = new EventType<>("DELETED");
    public static final EventType<EntryFinishedEvent> BACK    = new EventType<>("BACK");

    public EntryFinishedEvent(EventType<EntryFinishedEvent> eventType) {
        super(eventType);
    }
}
