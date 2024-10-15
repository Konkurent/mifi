package kechin.dmitrii.yaTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YaWeatherRequestQuery {

    private final YaWeatherRequest request;

    YaWeatherRequestQuery(YaWeatherRequest request) {
        this.request = request;
    }

    @Override
    public String toString() {
        List<String> params = new ArrayList<>();
        params.add("lon=" + request.lon());
        params.add("lat=" + request.lat());
        Optional.ofNullable(request.limit()).map(limit -> "limit=" + limit).ifPresent(params::add);
        return "?" + String.join("&", params);
    }
}
