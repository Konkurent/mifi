package kechin.dmitrii.yaTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class YaWeatherServiceImpl implements YaWeatherService {

    private final HttpClient httpClient;

    private final static String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";
    private final static String TOKEN = "15644090-47ec-4abb-bbf3-42680865f9be";

    public YaWeatherServiceImpl() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String send(YaWeatherRequest request) throws IOException, InterruptedException {
        return httpClient.send(createGetRequest(request), HttpResponse.BodyHandlers.ofString()).body();
    }

    private HttpRequest createGetRequest(YaWeatherRequest request) {
        YaWeatherRequestQuery query = new YaWeatherRequestQuery(request);
        return HttpRequest.newBuilder().GET()
                .uri(URI.create(BASE_URL + query))
                .header("X-Yandex-Weather-Key", TOKEN)
                .build();
    }
}
