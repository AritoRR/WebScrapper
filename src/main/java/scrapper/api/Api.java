package scrapper.api;

import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionException;

public interface Api {
    void handleApiResponse(String response);

    default void sendAsyncRequest(HttpRequest request) {
        try {
            getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync(HttpResponse::body)
                    .thenAcceptAsync(this::handleApiResponse);
        } catch (CompletionException |
                 NullPointerException |
                 SecurityException e){
            getLogger().error(e);
        }
    }

    default void fetchData(){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getSource()))
                    .GET()
                    .build();

            sendAsyncRequest(request);

        } catch (IllegalArgumentException e){
            getLogger().error(e);
        }
    }

    HttpClient getHttpClient();

    Logger getLogger();

    String getSource();

}
