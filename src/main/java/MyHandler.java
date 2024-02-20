import interfaces.Client;
import interfaces.Handler;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyHandler implements Handler {
    private static final int N_THREADS = 4;
    private static final int TIMEOUT = 100;

    private final Client client;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final BlockingQueue<RejectedShipment> rejectedShipments = new LinkedBlockingQueue<>();

    public MyHandler(Client client) {
        this(client, N_THREADS);
    }

    public MyHandler(Client client, int nThreads) {
        this.client = client;
        this.executorService = Executors.newFixedThreadPool(nThreads);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(nThreads);

        this.scheduledExecutorService.scheduleWithFixedDelay(
                () -> rejectedShipments.removeIf(rejected ->
                        client.sendData(rejected.dest, rejected.payload) == Client.Result.ACCEPTED),
                timeout().toMillis(),
                timeout().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public Duration timeout() {
        return Duration.ofMillis(TIMEOUT);
    }

    @Override
    public void performOperation() {
        final Client.Event event = client.readData();
        if (event == null) return;
        final Client.Payload payload = event.payload();
        executorService.submit(() -> event.recipients().forEach(address -> {
            Client.Result result = client.sendData(address, payload);
            if (result == Client.Result.REJECTED) {
                rejectedShipments.offer(new RejectedShipment(address, payload));
            }
        }));
    }

    public void stop() {
        executorService.shutdownNow();
        scheduledExecutorService.shutdownNow();
    }

    public record RejectedShipment(Client.Address dest, Client.Payload payload) {
    }
}
