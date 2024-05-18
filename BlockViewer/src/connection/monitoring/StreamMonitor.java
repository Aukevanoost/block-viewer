package connection.monitoring;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public abstract class StreamMonitor {
    protected final AtomicBoolean fired = new AtomicBoolean(false);
    public void fire() {
        fired.set(true);
    }

    protected boolean taskIsAlive() {
        return !fired.get()
            && !Thread.currentThread().isInterrupted()
            && Thread.currentThread().isAlive();
    }

    public void log(String type, String cmd, int size) {
        System.out.format(
            "[%s] %s: %s (%d bytes)\n",
            new SimpleDateFormat("HH.mm.ss").format(new java.util.Date()),
            type,
            cmd,
            size
        );
    }
}
