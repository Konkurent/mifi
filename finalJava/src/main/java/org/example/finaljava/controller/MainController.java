package org.example.finaljava.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.finaljava.services.AuthService;
import org.example.finaljava.services.LinkService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainController {

    private final AuthService authService;
    private final LinkService linkService;

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws URISyntaxException, IOException {
        try {
            while (true) {
                String command = parseString(START);
                switch (command) {
                    case "/createUser" -> authService.createUser(parseString(INSERT_LOGIN), parseString(INSERT_PASS));
                    case "/loginUser" -> authService.login(parseString(INSERT_LOGIN), parseString(INSERT_PASS));
                    case "/createLink" -> createLink();
                    case "/showLinks" -> linkService.showAllLinks(parseString(INSERT_UUID));
                    case "/deleteLink" -> linkService.deleteLink(parseString(INSERT_UUID), parseString(INSERT_SHORT_LINK));
                    case "/updateLink" -> updateLink();
                    case "/jump" -> linkService.jump(parseString(INSERT_UUID), parseString(INSERT_SHORT_LINK));
                    default -> System.out.println("Команда не найдена");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            start();
        }
    }

    private void updateLink() {
        System.out.println(UPDATE_LINK);
        updateLinkByMode();
    }

    private void createLink() {
        System.out.println(CREATE_LINK);
        String uuid = parseString(INSERT_UUID);
        String fullLink = parseString(INSERT_FULL_LINK);
        ChronoUnit chronoUnit = parseChronoUnit();
        Long duration = parseLong(INSERT_DURATION + " " + SKIP);
        Long limit = parseLong(INSERT_LIMIT + " " + SKIP);
        SCANNER.reset();
        linkService.createLink(uuid, fullLink, chronoUnit, duration, limit);
    }


    private String updateLinkByMode() {
        while (true) {
            System.out.println(INSERT_UPDATE_MODE);
            SCANNER.reset();
            String mode = SCANNER.next();
            switch (mode) {
                case "/duration" -> linkService.updateTimeOut(parseString(INSERT_UUID), parseString(INSERT_SHORT_LINK), parseLong(INSERT_DURATION), parseChronoUnit());
                case "/limit" -> linkService.updateLimit(parseString(INSERT_UUID), parseString(INSERT_SHORT_LINK), parseLong(INSERT_LIMIT));
            }
        }
    }

    private static final String INSERT_UPDATE_MODE = """
            Что хотите обновить?"
            /duration - длительность
            /limit - лимит
            """;

    private static final String START = """
            Здравствуйте!
            Добро пожаловать в сервис коротких ссылок.
            Чтобы воспользоваться функционалом в полной мере ознакомьтесь с комадами.
            Команды:
            /createUser - создать пользователя
            /loginUser - войти в систему
            /createLink - создать ссылку
            /showLinks - показать все ссылки
            /deleteLink - удалить ссылку
            /updateLink - обновить ссылку
            /jump - перейти по ссылке
            """;

    private static final String CREATE_LINK = "Создание ссылки.";
    private static final String UPDATE_LINK = "Обновление ссылки.";

    // Команды
    private static final String INSERT_LOGIN = "Введите логин";
    private static final String INSERT_PASS = "Введите пароль";
    private static final String INSERT_UUID = "Введите UUID пользователя";
    private static final String INSERT_FULL_LINK = "Введите полную ссылку";
    private static final String INSERT_SHORT_LINK = "Введите коротку ссылку";
    private static final String INSERT_LIMIT = "Введите лимит переходов";
    private static final String INSERT_DURATION = "Введите продожительность действия ссылки. Наприме: 1 => 1 ChronoUnit";
    private static final String SKIP = "(/skip - пропуск)";
    private static final String INSERT_DURATION_TYPE = """
            Выбирете тип вводимого времени:
            1. NANOS - наносекунды
            2. MICROS - микросекунды
            3. MILLIS - миллисекунды
            4. SECONDS - секунды
            5. MINUTES - минуты
            6. HOURS - часы
            7. HALF_DAYS - полдня
            8. DAYS - дни
            9. WEEKS - недели
            10. MONTHS - месяцы
            11. YEARS - годы
            12. DECADES - десятилетия
            13. CENTURIES - столетия
            14. MILLENNIA - тысячелетия
            15. ERAS - эры
            16. FOREVER - вечность
            
            /skip - пропуск
            """;

    private ChronoUnit parseChronoUnit() {
        while (true) {
            System.out.print(INSERT_DURATION_TYPE + System.lineSeparator());
            try {
                SCANNER.reset();
                String val = SCANNER.nextLine().trim().toUpperCase();
                if (val.equals("/SKIP")) {
                    return null;
                }
                return ChronoUnit.valueOf(val);
            } catch (IllegalArgumentException e) {
                System.err.println("Некорректный тип времени, попробуйте еще раз.");
            }
        }
    }

    private static final Scanner SCANNER = new Scanner(System.in);

    private static Long parseLong(String title) {

        while (true) {
            System.out.print(title + System.lineSeparator());
            try {
                SCANNER.reset();
                String val = SCANNER.next();
                if (val.equals("/skip")) {
                    return null;
                }
                else {
                    return Long.parseLong(val);
                }
            } catch (NumberFormatException e) {
                System.err.println("Это не корректное число, попробуйте еще раз.");
            }
        }
    }

    private static String parseString(String title) {
        while (true) {
            SCANNER.reset();
            System.out.println(title);
            String val = SCANNER.nextLine();
            if (val.equals("")) {
                parseString("");
            }
            return val;
        }
    }
}
