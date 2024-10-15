package kechin.dmitrii.yaTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final static Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {
        YaWeatherService yaWeatherService = new YaWeatherServiceImpl();
        // получение погоды за сегодня по коорднатам
        YaWeatherRequest request = new YaWeatherRequest(inputLat(), inputLan());
        String result = yaWeatherService.send(request);
        Integer todayTemp = MAPPER.readTree(result).get("fact").get("temp").asInt();
        System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readValue(result, Serializable.class)));
        System.out.println("Температура: " + todayTemp);
        // получение погоды по коорднатам за период + ср температура
        request = new YaWeatherRequest(inputLat(), inputLan(), inputLimit());
        result = yaWeatherService.send(request);
        Serializable period = MAPPER.readValue(yaWeatherService.send(request), Serializable.class);
        List<Double> dayTempAvg = new ArrayList<>();
        MAPPER.readTree(result).get("forecasts").forEach(it -> {
            JsonNode node = it.get("parts");
            dayTempAvg.add(node.get("day_short").get("temp").asDouble() + node.get("night_short").get("temp").asDouble() / 2);
        });
        System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readValue(result, Serializable.class)));
        System.out.println("Средняя температура: " + dayTempAvg.stream().mapToDouble(it -> it).sum() / dayTempAvg.size());


    }

    private static Float inputFloat() {
        try {
            return new Scanner(System.in).reset().nextFloat();
        } catch (Exception e) {
            System.err.println("Введите число в формате ##,##");
            return inputFloat();
        }
    }

    private static Integer inputInteger() {
        try {
            return SCANNER.reset().nextInt();
        } catch (Exception e) {
            System.err.println("Введите число в формате ##");
            return inputInteger();
        }
    }

    private static Float inputLat() {
        System.out.print("Введите широту: ");
        return inputFloat();
    }

    private static Float inputLan() {
        System.out.print("Введите долготу: ");
        return inputFloat();
    }

    private static Integer inputLimit() {
        System.out.print("Введите кол-во дней, включая сегодня, за которое вывести погоду: ");
        return inputInteger();
    }
}