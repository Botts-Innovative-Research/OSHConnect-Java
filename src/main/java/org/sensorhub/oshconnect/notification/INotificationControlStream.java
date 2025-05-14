package org.sensorhub.oshconnect.notification;

import org.sensorhub.oshconnect.OSHControlStream;

public interface INotificationControlStream extends INotificationItem<OSHControlStream> {
    @Override
    void onItemAdded(OSHControlStream item);

    @Override
    void onItemRemoved(OSHControlStream item);
}
