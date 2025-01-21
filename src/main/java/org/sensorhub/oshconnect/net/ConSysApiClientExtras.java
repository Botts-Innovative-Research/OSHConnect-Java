package org.sensorhub.oshconnect.net;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.obs.DataStreamBindingJson;
import org.sensorhub.impl.service.consys.obs.ObsHandler;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.system.SystemBindingGeoJson;
import org.sensorhub.oshconnect.datamodels.ObservationBindingOmJson;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public class ConSysApiClientExtras {
    static final String JSON_ARRAY_ITEMS = "items";
    static final String SYSTEMS_COLLECTION = "systems";
    static final String DATASTREAMS_COLLECTION = "datastreams";
    static final String OBSERVATIONS_COLLECTION = "observations";

    protected HttpClient http;
    protected URI endpoint;

    protected ConSysApiClientExtras() {
    }

    public static ConSysApiClientExtras.ConSysApiClientExtrasBuilder newBuilder(String endpoint) {
        Asserts.checkNotNull(endpoint, "endpoint");
        return new ConSysApiClientExtras.ConSysApiClientExtrasBuilder(endpoint);
    }

    /**
     * Get the systems of the OpenSensorHub node.
     *
     * @return A list of systems.
     */
    public CompletableFuture<List<ISystemWithDesc>> getSystems() {
        return sendGetRequest(endpoint.resolve(SYSTEMS_COLLECTION), ResourceFormat.JSON, body -> {
            try {
                var ctx = new RequestContext(body);

                JsonObject bodyJson = JsonParser.parseReader(new InputStreamReader(ctx.getInputStream())).getAsJsonObject();
                JsonArray features = bodyJson.getAsJsonArray(JSON_ARRAY_ITEMS);

                List<ISystemWithDesc> systems = new ArrayList<>();
                for (var feature : features) {
                    var ctx2 = new RequestContext(new ByteArrayInputStream(feature.toString().getBytes()));
                    var binding = new SystemBindingGeoJson(ctx2, null, null, true);
                    systems.add(binding.deserialize());
                }

                return systems;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Get the datastream IDs for a system.
     *
     * @param systemID The ID of the system.
     * @return A list of datastream IDs.
     */
    public CompletableFuture<List<String>> getDatastreamIds(String systemID) {
        return sendGetRequest(endpoint.resolve(SYSTEMS_COLLECTION + "/" + systemID + "/" + DATASTREAMS_COLLECTION), ResourceFormat.JSON, body -> {
            try {
                var ctx = new RequestContext(body);

                JsonObject bodyJson = JsonParser.parseReader(new InputStreamReader(ctx.getInputStream())).getAsJsonObject();
                JsonArray features = bodyJson.getAsJsonArray(JSON_ARRAY_ITEMS);

                List<String> datastreams = new ArrayList<>();
                for (var feature : features) {
                    // Get the ID
                    var id = feature.getAsJsonObject().entrySet().stream()
                            .filter(e -> e.getKey().equals("id"))
                            .findFirst()
                            .orElseThrow(() -> new IOException("No id found in feature"));
                    datastreams.add(id.getValue().getAsString());
                }

                return datastreams;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Integer> deleteDatastream(String dataStreamId) {
        return sendDeleteRequest(endpoint.resolve(DATASTREAMS_COLLECTION + "/" + dataStreamId));
    }

    /**
     * Push an observation to a datastream.
     *
     * @param dataStreamId The ID of the datastream.
     * @param dataStream   The datastream object.
     * @param obs          The observation to push.
     * @return The ID of the observation if the operation was successful, otherwise null.
     */
    public CompletableFuture<String> pushObservation(String dataStreamId, IDataStreamInfo dataStream, ObservationData obs) {
        try {
            ObsHandler.ObsHandlerContextData contextData = new ObsHandler.ObsHandlerContextData();
            contextData.dsInfo = dataStream;

            var buffer = new ByteArrayOutputStream();
            var ctx = new RequestContext(buffer);
            ctx.setData(contextData);

            ctx.setFormat(ResourceFormat.OM_JSON);
            var binding = new ObservationBindingOmJson(ctx, null, false);
            binding.serialize(null, obs, false);

            return sendPostRequest(
                    endpoint.resolve(DATASTREAMS_COLLECTION + "/" + dataStreamId + "/" + OBSERVATIONS_COLLECTION),
                    ctx.getFormat(),
                    buffer.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Error initializing binding", e);
        }
    }

    /**
     * Get an observation by ID.
     *
     * @param observationId The ID of the observation.
     * @param dataStream    The datastream object.
     * @return The observation.
     */
    public CompletableFuture<ObservationData> getObservation(String observationId, IDataStreamInfo dataStream) {
        return sendGetRequest(endpoint.resolve(OBSERVATIONS_COLLECTION + "/" + observationId), ResourceFormat.OM_JSON, body -> {
            try {
                ObsHandler.ObsHandlerContextData contextData = new ObsHandler.ObsHandlerContextData();
                contextData.dsInfo = dataStream;

                var ctx = new RequestContext(body);
                ctx.setData(contextData);
                ctx.setFormat(ResourceFormat.OM_JSON);

                var binding = new ObservationBindingOmJson(ctx, null, true);
                return binding.deserialize();

            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Get the latest set of observations for a datastream.
     *
     * @param dataStreamId The ID of the datastream.
     * @param dataStream   The datastream object.
     * @return A list of observations.
     */
    public CompletableFuture<List<ObservationData>> getObservations(String dataStreamId, IDataStreamInfo dataStream) {
        return getObservations(dataStreamId, dataStream, "");
    }

    /**
     * Get a set of observations for a datastream with a query string.
     *
     * @param dataStreamId The ID of the datastream.
     * @param dataStream   The datastream object.
     * @param queryString  The query string to include in the request.
     *                     Valid parameters are:
     *                     <ul>
     *                     <li>id:
     *                     List of resource local IDs or unique IDs (URI).
     *                     Only resources that have one of the provided identifiers are selected.</li>
     *                     <li>phenomenonTime:
     *                     Only resources with a phenomenonTime property
     *                     that intersects the value of the phenomenonTime parameter are selected.</li>
     *                     <li>resultTime:
     *                     Only resources with a phenomenonTime property
     *                     that intersects the value of the phenomenonTime parameter are selected.</li>
     *                     <li>foi:
     *                     List of feature local IDs or unique IDs (URI).
     *                     Only resources that are associated with a feature of interest
     *                     that has one of the provided identifiers are selected.</li>
     *                     <li>observedProperty:
     *                     List of property local IDs or unique IDs (URI).
     *                     Only resources that are associated with an observable property
     *                     that has one of the provided identifiers are selected.</li>
     *                     <li>limit:
     *                     Limits the number of items that are presented in the response.</li>
     *                     </ul>
     * @return A list of observations.
     */
    public CompletableFuture<List<ObservationData>> getObservations(String dataStreamId, IDataStreamInfo dataStream, String queryString) {
        if (queryString == null)
            queryString = "";
        if (!queryString.isEmpty() && !queryString.startsWith("?"))
            queryString = "?" + queryString;

        String url = DATASTREAMS_COLLECTION + "/" + dataStreamId + "/" + OBSERVATIONS_COLLECTION + queryString;
        System.out.println("URL: " + url);

        return sendGetRequest(endpoint.resolve(url), ResourceFormat.OM_JSON, body -> {
            try {
                ObsHandler.ObsHandlerContextData contextData = new ObsHandler.ObsHandlerContextData();
                contextData.dsInfo = dataStream;

                var ctx = new RequestContext(body);

                JsonObject bodyJson = JsonParser.parseReader(new InputStreamReader(ctx.getInputStream())).getAsJsonObject();
                JsonArray features = bodyJson.getAsJsonArray(JSON_ARRAY_ITEMS);

                List<ObservationData> observations = new ArrayList<>();
                for (var feature : features) {
                    var ctx2 = new RequestContext(new ByteArrayInputStream(feature.toString().getBytes()));
                    ctx2.setData(contextData);
                    ctx2.setFormat(ResourceFormat.OM_JSON);

                    var binding = new ObservationBindingOmJson(ctx2, null, true);
                    observations.add(binding.deserialize());
                }

                return observations;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    protected <T> CompletableFuture<T> sendGetRequest(URI collectionUri, ResourceFormat format, Function<InputStream, T> bodyMapper) {
        var req = HttpRequest.newBuilder()
                .uri(collectionUri)
                .GET()
                .header(HttpHeaders.ACCEPT, format.getMimeType())
                .build();

        var bodyHandler = (HttpResponse.BodyHandler<T>) resp -> {
            var upstream = HttpResponse.BodySubscribers.ofByteArray();
            return HttpResponse.BodySubscribers.mapping(upstream, body -> {
                var is = new ByteArrayInputStream(body);
                return bodyMapper.apply(is);
            });
        };

        return http.sendAsync(req, bodyHandler)
                .thenApply(resp -> {
                    if (resp.statusCode() == 200)
                        return resp.body();
                    else
                        throw new CompletionException("HTTP error " + resp.statusCode(), null);
                });
    }

    public CompletableFuture<Integer> updateDataStream(String dataStreamId, IDataStreamInfo dataStream) {
        try {
            var buffer = new ByteArrayOutputStream();
            var ctx = new RequestContext(buffer);

            var binding = new DataStreamBindingJson(ctx, null, null, false, Collections.emptyMap());
            binding.serialize(null, dataStream, false);

            return sendPutRequest(
                    endpoint.resolve(DATASTREAMS_COLLECTION + "/" + dataStreamId),
                    ResourceFormat.JSON,
                    buffer.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Error initializing binding", e);
        }
    }

    protected CompletableFuture<String> sendPostRequest(URI collectionUri, ResourceFormat format, byte[] body) {
        var req = HttpRequest.newBuilder()
                .uri(collectionUri)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .header(HttpHeaders.ACCEPT, ResourceFormat.JSON.getMimeType())
                .header(HttpHeaders.CONTENT_TYPE, format.getMimeType())
                .build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() == 201 || resp.statusCode() == 303) {
                        var location = resp.headers()
                                .firstValue(HttpHeaders.LOCATION)
                                .orElseThrow(() -> new IllegalStateException("Missing Location header in response"));
                        return location.substring(location.lastIndexOf('/') + 1);
                    } else
                        throw new CompletionException(resp.body(), null);
                });
    }

    protected CompletableFuture<Integer> sendPutRequest(URI collectionUri, ResourceFormat format, byte[] body) {
        var req = HttpRequest.newBuilder()
                .uri(collectionUri)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(body))
                .header(HttpHeaders.ACCEPT, ResourceFormat.JSON.getMimeType())
                .header(HttpHeaders.CONTENT_TYPE, format.getMimeType())
                .build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode);
    }

    protected CompletableFuture<Integer> sendDeleteRequest(URI collectionUri) {
        var req = HttpRequest.newBuilder()
                .uri(collectionUri)
                .DELETE()
                .header(HttpHeaders.ACCEPT, ResourceFormat.JSON.getMimeType())
                .build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode);
    }

    public static class ConSysApiClientExtrasBuilder extends BaseBuilder<ConSysApiClientExtras> {
        HttpClient.Builder httpClientBuilder;


        ConSysApiClientExtrasBuilder(String endpoint) {
            this.instance = new ConSysApiClientExtras();
            this.httpClientBuilder = HttpClient.newBuilder();

            try {
                if (!endpoint.endsWith("/"))
                    endpoint += "/";
                instance.endpoint = new URI(endpoint);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI " + endpoint);
            }
        }

        public ConSysApiClientExtras.ConSysApiClientExtrasBuilder useHttpClient(HttpClient http) {
            instance.http = http;
            return this;
        }

        public ConSysApiClientExtras.ConSysApiClientExtrasBuilder simpleAuth(String user, char[] password) {
            if (!Strings.isNullOrEmpty(user)) {
                var finalPwd = password != null ? password : new char[0];
                httpClientBuilder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, finalPwd);
                    }
                });
            }

            return this;
        }

        @Override
        public ConSysApiClientExtras build() {
            if (instance.http == null)
                instance.http = httpClientBuilder.build();

            return instance;
        }
    }
}
