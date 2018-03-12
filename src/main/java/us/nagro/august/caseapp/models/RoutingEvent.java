package us.nagro.august.caseapp.models;

import javafx.event.Event;
import javafx.event.EventType;

public class RoutingEvent extends Event {

    public static final EventType<RoutingEvent> HOME        = new EventType<>("HOME");
    public static final EventType<RoutingEvent> LOG_OUT     = new EventType<>("LOG_OUT");
    public static final EventType<RoutingEvent> PREFERENCES = new EventType<>("PREFERENCES");

    public final KdbxContext context;

    public RoutingEvent(EventType<RoutingEvent> eventType, KdbxContext context) {
        super(eventType);
        this.context = context;
    }
}
