package org.sensorhub.oshconnect.notification;

import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;

public interface INotificationDatastream extends INotificationItem<OSHDatastream> {
    @Override
    void onItemAdded(OSHDatastream item);

    @Override
    void onItemRemoved(OSHDatastream item);
}
