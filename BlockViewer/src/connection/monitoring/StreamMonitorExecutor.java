package connection.monitoring;

import java.util.function.Supplier;

public class StreamMonitorExecutor extends StreamMonitor{
    public void execute(Supplier<Boolean> performTask, int timeout) {
        try {
            while(taskIsAlive()) {
                var shouldStop = performTask.get();
                if(shouldStop) this.cancelled.set(true);
                Thread.sleep(timeout);
            }
        }catch(InterruptedException e) {
            System.out.println("Worker passed away..");
            Thread.currentThread().interrupt();
        }
    }
}
