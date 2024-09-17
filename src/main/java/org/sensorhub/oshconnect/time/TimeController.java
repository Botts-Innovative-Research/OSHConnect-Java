package org.sensorhub.oshconnect.time;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class TimeController {
    @Getter
    private final PlaybackState playbackState = PlaybackState.STOPPED;
    @Getter
    @Setter
    private TemporalMode temporalMode;
    private int playbackSpeed = 1;
    @Getter
    @Setter
    private Instant startTime;
    @Getter
    @Setter
    private Instant endTime;
    @Getter
    private Instant currentTime;

    public void start() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void stop() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void fastForward(int speed) {
        playbackSpeed = speed;
    }

    public void rewind(int speed) {
        playbackSpeed = -speed;
    }

    public void skip(long milliseconds) {
        currentTime = currentTime.plusMillis(milliseconds);
    }

    public void skipTo(Instant time) {
        setCurrentTime(time);
    }
    
    public void reset() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void setCurrentTime(Instant time) {
        if (time.isBefore(startTime)) {
            currentTime = startTime;
        } else if (time.isAfter(endTime)) {
            currentTime = endTime;
        } else {
            currentTime = time;
        }
    }
}
