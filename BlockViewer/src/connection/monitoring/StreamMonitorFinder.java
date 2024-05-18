package connection.monitoring;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class StreamMonitorFinder extends StreamMonitor{
    public <T> T find(Supplier<Optional<T>> findNeedle) throws TimeoutException{
        try {
            Optional<T> needle;
            while(taskIsAlive()) {
                needle = findNeedle.get();
                if(needle.isPresent()) {
                    this.fired.set(true);
                    return needle.get();
                }
                Thread.sleep(10);
            }
        }catch(InterruptedException e) {
            System.out.println("TrackedListener passed away..");
            Thread.currentThread().interrupt();
        }
        throw new TimeoutException("Could not find message");
    }

}
