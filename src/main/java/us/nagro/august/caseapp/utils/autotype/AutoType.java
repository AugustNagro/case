package us.nagro.august.caseapp.utils.autotype;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoType {

    private static final Logger LOG = Logger.getLogger(AutoType.class.getName());

    /**
     * Registers jnativehook. Unregisters the old hook if already setup.
     * @param keyListener may be null for initial registration
     */
    public static void register(NativeKeyListener keyListener) {
        unregister();

        // to use JavaFX's native event queue
        GlobalScreen.setEventDispatcher(new JFXDispatchService());

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            LOG.log(Level.WARNING, "Could not register native hook", e);
            return;
        }

        if (keyListener != null) GlobalScreen.addNativeKeyListener(keyListener);
    }

    public static void unregister() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            LOG.log(Level.SEVERE, "could not unregister native hook", e);
        }
    }
}
