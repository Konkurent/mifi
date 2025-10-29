package mifi.client1.controller;

import lombok.RequiredArgsConstructor;
import mifi.client1.dto.ConfigDTO;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigDTO configDTO;

    @GetMapping("/{field}")
    public Object getConfig(@PathVariable String field) {
        return ReflectionUtils.getField(
                Objects.requireNonNull(ReflectionUtils.findField(ConfigDTO.class, field)),
                configDTO
        );
    }

}
