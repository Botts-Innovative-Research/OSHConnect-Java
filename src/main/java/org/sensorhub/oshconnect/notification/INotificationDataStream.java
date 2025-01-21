package org.sensorhub.oshconnect.notification;

import org.sensorhub.oshconnect.OSHDataStream;

public interface INotificationDataStream extends INotificationItem<OSHDataStream> {
    @Override
    void onItemAdded(OSHDataStream item);

    @Override
    void onItemRemoved(OSHDataStream item);
}
