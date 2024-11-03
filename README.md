# OSHConnect-Java

# OSH Connect Tutorial

OSH Connect for Python is a straightforward library for interacting with OpenSensorHub using OGC API
Connected Systems. This tutorial will help guide you through a few simple examples to get you
started with OSH Connect.

## Creating an instance of OSHConnect

The intended method of interacting with OpenSensorHub is through the OSHConnect class. To this you
must first create an instance of OSHConnect:

```Java
OSHConnect oshConnect = new OSHConnect("OSH Connect Example");
```

The name parameter is optional, but can be useful for debugging purposes.

## Adding a Node to an OSHConnect instance

```Java
OSHNode node = oshConnect.createNode("localhost:8181/sensorhub", true, "admin", "admin");
```

The `createNode` method takes the following parameters:

- `sensorHubRoot`: The URL of the OpenSensorHub instance
- `isSecure`: Whether the connection is secure (HTTPS)
- `username`: The username to authenticate with
- `password`: The password to authenticate with

## System Discovery

```Java
oshConnect.discoverSystems();
```

This method will discover all systems available on every node in the OSHConnect instance.
It is also possible to discover systems on a specific node:

```Java
node.discoverSystems();
```

## Datastream Discovery

```Java
oshConnect.discoverDatastreams();
```

This method will discover all datastreams available on every system for every node in the OSHConnect
instance.
It is also possible to discover datastreams on a specific system.

## Subscribing to Datastreams

Once you have discovered the datastreams you are interested in, you can retrieve observations from
them:

```Java
List<OSHDatastream> datastreams = oshConnect.discoverDatastreams();

// Create a new DatastreamHandler to manage connections to the datastreams.
DatastreamHandler handler = oshConnect.getDatastreamManager().createDatastreamHandler(this::onStreamUpdate);

// Add all the discovered datastreams to the handler.
datastreams.forEach(handler::addDatastream);

// Connect, listen for updates.
handler.connect();

// Start listening for historical data instead of live data.
Instant oneMinuteAgo = Instant.now().minusSeconds(60);
handler.setTimeExtent(TimeExtent.startingAt(oneMinuteAgo));
handler.setReplaySpeed(0.25);

// This method will be called whenever a new observation is available.
private void onStreamUpdate(DatastreamEventArgs args) {
    var datastreamId = args.getDatastream().getDatastreamResource().getId();
    var timestamp = args.getTimestamp();

    String message = String.format("onStreamUpdate: timestamp=%s datastreamId=%s", timestamp, datastreamId);
    System.out.println(message);
}
```
