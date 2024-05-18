package connection.monitoring;

import java.util.function.Supplier;

public class StreamMonitorExecutor extends StreamMonitor{
    public void execute(Supplier<Boolean> performTask) {
        try {
            while(taskIsAlive()) {
                var shouldStop = performTask.get();
                if(shouldStop) this.fired.set(true);
                Thread.sleep(10);
            }
        }catch(InterruptedException e) {
            System.out.println("Worker passed away..");
            Thread.currentThread().interrupt();
        }
    }
}
