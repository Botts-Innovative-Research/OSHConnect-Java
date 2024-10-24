package org.sensorhub.oshconnect.time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

public class TimeSynchronizer<T> {
    private static final int TIME_BETWEEN_UPDATES_MS = 10;
    private final List<TimeSynchronizerEvent<T>> receivedEvents = new ArrayList<>();
    private final Consumer<T> eventConsumer;
    private ScheduledExecutorService executorService;
    private boolean timeSynchronizationEnabled;
    private long lastUpdateTimestamp;
    @Getter
    @Setter
    private int bufferTimeMS = 1000;
    @Getter
    @Setter
    private boolean discardOutdatedEvents = true;

    public TimeSynchronizer(Consumer<T> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public void enableTimeSynchronization() {
        if (bufferTimeMS <= 0) {
            throw new IllegalArgumentException("Buffer time must be greater than 0.");
        }

        this.timeSynchronizationEnabled = true;

        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
        executorService.scheduleWithFixedDelay(this::processBufferedEvents, 0, TIME_BETWEEN_UPDATES_MS, TimeUnit.MILLISECONDS);
    }

    public void disableTimeSynchronization(boolean discardBuffer) {
        this.timeSynchronizationEnabled = false;

        if (executorService != null) {
            executorService.shutdown();
        }

        if (!discardBuffer) {
            for (TimeSynchronizerEvent<T> event : receivedEvents) {
                eventConsumer.accept(event.getEvent());
            }
        }
        receivedEvents.clear();
    }

    public void addEvent(long timestamp, T event) {
        if (timeSynchronizationEnabled) {
            TimeSynchronizerEvent<T> timeSynchronizerEvent = new TimeSynchronizerEvent<>(timestamp, event);
            receivedEvents.add(timeSynchronizerEvent);
        } else {
            eventConsumer.accept(event);
        }
    }

    private void processBufferedEvents() {
        if (!timeSynchronizationEnabled || receivedEvents.isEmpty()) return;

        long bufferTime = System.currentTimeMillis() - bufferTimeMS;
        List<TimeSynchronizerEvent<T>> toServe = new ArrayList<>();

        for (TimeSynchronizerEvent<T> timeSynchronizerEvent : receivedEvents) {
            if (timeSynchronizerEvent.getTimestamp() <= bufferTime) {
                toServe.add(timeSynchronizerEvent);
            }
        }
        if (toServe.isEmpty()) return;

        toServe.sort(Comparator.comparingLong(TimeSynchronizerEvent::getTimestamp));

        for (TimeSynchronizerEvent<T> timeSynchronizerEvent : toServe) {
            if (discardOutdatedEvents && timeSynchronizerEvent.getTimestamp() < lastUpdateTimestamp) {
                System.out.println("Discarding outdated event: " + timeSynchronizerEvent.getTimestamp());
                receivedEvents.remove(timeSynchronizerEvent);
                continue;
            }

            eventConsumer.accept(timeSynchronizerEvent.getEvent());
            receivedEvents.remove(timeSynchronizerEvent);
            lastUpdateTimestamp = timeSynchronizerEvent.getTimestamp();
        }
    }
}
