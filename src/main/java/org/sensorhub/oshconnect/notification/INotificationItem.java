package org.sensorhub.oshconnect.notification;

public interface INotificationItem<T> {
    void onItemAdded(T item);

    void onItemRemoved(T item);
}
