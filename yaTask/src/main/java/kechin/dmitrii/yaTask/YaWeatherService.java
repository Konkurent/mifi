package kechin.dmitrii.yaTask;

import java.io.IOException;

public sealed interface YaWeatherService permits YaWeatherServiceImpl {
    String send(YaWeatherRequest request) throws IOException, InterruptedException;
}
