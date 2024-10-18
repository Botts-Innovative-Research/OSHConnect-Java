package org.sensorhub.oshconnect.notification;

import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

public interface INotificationSystem extends INotificationItem<OSHSystem> {
    @Override
    void onItemAdded(OSHSystem item);

    @Override
    void onItemRemoved(OSHSystem item);
}
