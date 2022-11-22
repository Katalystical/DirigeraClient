package de.dvdgeisler.iot.dirigera.client.api.http;

import de.dvdgeisler.iot.dirigera.client.api.model.Home;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Component
public class ClientApi extends AbstractClientApi {
    private final static Logger log = LoggerFactory.getLogger(ClientApi.class);
    private final String hostname;
    private final short port;

    public final ClientDeviceApi device;
    public final ClientDeviceSetApi deviceSet;
    public final ClientGatewayApi gateway;
    public final ClientMusicApi music;
    public final ClientOAuthApi oauth;
    public final ClientRemoteLinkApi remoteLink;
    public final ClientRoomApi room;
    public final ClientSceneApi scene;
    public final ClientStepApi step;
    public final ClientUserApi user;

    public ClientApi(
            @Value("${dirigera.hostname}") final String hostname,
            @Value("${dirigera.port:8443}") final short port,
            final TokenStore tokenStore,
            final ClientDeviceApi device,
            final ClientDeviceSetApi deviceSet,
            final ClientGatewayApi gateway,
            final ClientMusicApi music,
            final ClientOAuthApi oauth,
            final ClientRemoteLinkApi remoteLink,
            final ClientRoomApi room,
            final ClientSceneApi scene,
            final ClientStepApi step,
            final ClientUserApi user
    ) throws SSLException {
        super(String.format("https://%s:%d/v1/", hostname, port), tokenStore);
        this.hostname = hostname;
        this.port = port;
        this.device = device;
        this.deviceSet = deviceSet;
        this.gateway = gateway;
        this.music = music;
        this.oauth = oauth;
        this.remoteLink = remoteLink;
        this.room = room;
        this.scene = scene;
        this.step = step;
        this.user = user;
    }

    public Mono<Home> home() {
        return this.webClient
                .get()
                .uri(uri -> uri.path("home").build())
                .headers(this.tokenStore::setBearerAuth)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, this::onError)
                .bodyToMono(Home.class);
    }

    public Mono<Map> dump() {
        return this.webClient
                .get()
                .uri(uri -> uri.path("home").build())
                .headers(this.tokenStore::setBearerAuth)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::isError, this::onError)
                .bodyToMono(Map.class);
    }

    public void websocket() {
        final String token;
        final String authorizationHeader;
        final HttpClient httpClient;
        final WebSocketClient client;



        try {
            token = this.tokenStore.getAccessToken();
            authorizationHeader = String.format("Bearer %s", token);
            httpClient = this.httpClient
                    .headers(headers -> headers.add(HttpHeaders.AUTHORIZATION, authorizationHeader))
                    .keepAlive(true);
            client = new ReactorNettyWebSocketClient(httpClient);
            client.execute(URI.create(String.format("https://%s:%d/v1/", hostname, port)), session ->
                    session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(message -> log.debug("Received websocket message: {}", message))
                            .repeat()
                            .then()
            ).block();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


