package kechin.dmitrii.yaTask;


public record YaWeatherRequest(
        Float lat,
        Float lon,
        Integer limit
) {

    public YaWeatherRequest(Float lat, Float lon) {
        this(lat, lon, null);
    }

    public YaWeatherRequest {
        ObjectUtil.assertNotNull(lat, "Широта не может быть равна null");
        ObjectUtil.assertNotNull(lon, "Долгота не может быть равна null");
    }
}