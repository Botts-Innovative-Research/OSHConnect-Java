package org.sensorhub.oshconnect.time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * This class can be used in scenarios where events may be received out of order,
 * for example, due to network latency or other delays.
 * Events will be held in a buffer for a specified time period,
 * then served both in order and with respect to their timestamps.
 * <p>
 * The time synchronization can be enabled or disabled at any time.
 * When disabled, events will be triggered immediately instead of being buffered,
 * making it possible to switch between real-time and synchronized processing without separate code paths.
 * Add an event using the {@link #addEvent(long, Object)} method, and it will be either buffered and processed later
 * or fired immediately depending on whether time synchronization is enabled.
 *
 * @param <T> The type of the event.
 */
public class TimeSynchronizer<T> {
    private static final int TIME_BETWEEN_UPDATES_MS = 10;
    private final List<TimeSynchronizerEvent<T>> receivedEvents = new ArrayList<>();
    private final Consumer<T> eventConsumer;
    private ScheduledExecutorService executorService;
    private boolean timeSynchronizationEnabled;
    private long lastUpdateTimestamp;
    /**
     * The time in milliseconds to buffer events for time synchronization.
     */
    private int bufferTimeMS = 1000;
    /**
     * When time synchronization is enabled, events with a timestamp outside the buffer time will be discarded.
     * True by default.
     */
    private boolean discardOutdatedEvents = true;
    /**
     * Whether to discard any buffered events, when time synchronization is turned off or when resetting.
     */
    private boolean discardBuffer = true;

    /**
     * @param eventConsumer A Consumer that processes events.
     *                      This is called when an event is ready to be processed,
     *                      either immediately if time synchronization is disabled or after time synchronization.
     */
    public TimeSynchronizer(Consumer<T> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    /**
     * Enables time synchronization.
     * Events will be buffered for the specified buffer time.
     */
    public void enableTimeSynchronization() {
        this.timeSynchronizationEnabled = true;

        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
        executorService.scheduleWithFixedDelay(this::processBufferedEvents, 0, TIME_BETWEEN_UPDATES_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Disables time synchronization.
     * Further events will be processed immediately.
     */
    public void disableTimeSynchronization() {
        this.timeSynchronizationEnabled = false;

        if (executorService != null) {
            executorService.shutdown();
        }

        clearBuffer();
    }

    /**
     * Adds an event to the buffer.
     * If time synchronization is disabled, the event is processed immediately.
     *
     * @param timestamp The timestamp of the event.
     * @param args      The event arguments to pass to the consumer.
     */
    public void addEvent(long timestamp, T args) {
        if (timeSynchronizationEnabled) {
            TimeSynchronizerEvent<T> timeSynchronizerEvent = new TimeSynchronizerEvent<>(timestamp, args);
            receivedEvents.add(timeSynchronizerEvent);
        } else {
            eventConsumer.accept(args);
        }
    }

    /**
     * Processes buffered events based on their timestamps.
     * Events that are outside the buffer time will be discarded if discardOutdatedEvents is true.
     */
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
                receivedEvents.remove(timeSynchronizerEvent);
                continue;
            }

            eventConsumer.accept(timeSynchronizerEvent.getEvent());
            receivedEvents.remove(timeSynchronizerEvent);
            lastUpdateTimestamp = timeSynchronizerEvent.getTimestamp();
        }
    }

    /**
     * Clears the buffer and processes any remaining events.
     */
    private void clearBuffer() {
        if (!discardBuffer) {
            receivedEvents.sort(Comparator.comparingLong(TimeSynchronizerEvent::getTimestamp));
            for (TimeSynchronizerEvent<T> event : receivedEvents) {
                eventConsumer.accept(event.getEvent());
            }
        }
        receivedEvents.clear();
    }

    /**
     * The time in milliseconds to buffer events for time synchronization.
     */
    public int getBufferTimeMS() {
        return bufferTimeMS;
    }

    /**
     * Sets the buffer time in milliseconds.
     *
     * @param bufferTimeMS The buffer time in milliseconds.
     *                     For best results, this should be greater than the expected network latency.
     * @throws IllegalArgumentException if bufferTimeMS is less than or equal to 0.
     */
    private void setBufferTimeMS(int bufferTimeMS) {
        if (bufferTimeMS <= 0) {
            throw new IllegalArgumentException("Buffer time must be greater than 0");
        }
        this.bufferTimeMS = bufferTimeMS;
    }

    /**
     * When time synchronization is enabled, events with a timestamp outside the buffer time will be discarded.
     * True by default.
     */
    public boolean getDiscardOutdatedEvents() {
        return discardOutdatedEvents;
    }

    /**
     * When time synchronization is enabled, events with a timestamp outside the buffer time will be discarded.
     * True by default.
     */
    public void setDiscardOutdatedEvents(boolean discardOutdatedEvents) {
        this.discardOutdatedEvents = discardOutdatedEvents;
    }

    /**
     * Whether to discard any buffered events, when time synchronization is turned off or when resetting.
     */
    public boolean getDiscardBuffer() {
        return discardBuffer;
    }

    /**
     * Whether to discard any buffered events, when time synchronization is turned off or when resetting.
     */
    public void setDiscardBuffer(boolean discardBuffer) {
        this.discardBuffer = discardBuffer;
    }
}
