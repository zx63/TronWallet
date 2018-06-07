
package org.tron.MyUtils;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

public class ThrottledRunLater implements Runnable {
    private final Runnable runnable;
    private final AtomicBoolean pending = new AtomicBoolean();

    /** Created this way, the no-args runLater will execute this classes run method. */
    public ThrottledRunLater() {
        this.runnable = null;
    }

    /** Created this way, the no-args runLater will execute the given runnable. */
    public ThrottledRunLater(Runnable runnable) {
        this.runnable = runnable;
    }

    public void runLater(Runnable runnable) {
        if (!pending.getAndSet(true)) {
            Platform.runLater(() -> {
                pending.set(false);
                runnable.run();
            });
        }
    }

    public void runLater() {
        runLater(runnable != null ? runnable : this);
    }

    @Override
    public void run() {
    }
}
