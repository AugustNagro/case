package us.nagro.august.caseapp.utils.autotype;

import javafx.application.Platform;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

class JFXDispatchService extends AbstractExecutorService {
    private boolean running = true;

    @Override
    public void shutdown() {
        running = false;
    }

    @Override
    public List<Runnable> shutdownNow() {
        running = false;
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return !running;
    }

    @Override
    public boolean isTerminated() {
        return !running;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void execute(Runnable command) {
        Platform.runLater(command);
    }
}
