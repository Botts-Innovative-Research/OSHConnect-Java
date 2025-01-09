package org.sensorhub.oshconnect.net;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.impl.service.consys.system.SystemBindingGeoJson;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public class ConSysApiClientExtras {
    static final String SYSTEMS_COLLECTION = "systems";

    HttpClient http;
    URI endpoint;

    protected ConSysApiClientExtras() {
    }

    public static ConSysApiClientExtras.ConSysApiClientExtrasBuilder newBuilder(String endpoint) {
        Asserts.checkNotNull(endpoint, "endpoint");
        return new ConSysApiClientExtras.ConSysApiClientExtrasBuilder(endpoint);
    }

    public CompletableFuture<List<ISystemWithDesc>> getSystems(ResourceFormat format) {
        return sendGetRequest(endpoint.resolve(SYSTEMS_COLLECTION), format, body -> {
            try {
                var ctx = new RequestContext(body);

                JsonObject bodyJson = JsonParser.parseReader(new InputStreamReader(ctx.getInputStream())).getAsJsonObject();
                JsonArray features = bodyJson.getAsJsonArray("items");

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
