package skillfactory.specialinstruments.services.notification;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dao.entity.OTPEntity;
import skillfactory.specialinstruments.dao.repositories.account.AccountRepository;
import skillfactory.specialinstruments.dto.telegram.ChatUpdateSummary;
import skillfactory.specialinstruments.dto.telegram.Updates;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot implements OTPNotificator {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final AccountRepository accountRepository;

    @Value("${telegram.bot.token}")
    private String apiCode;



    private <T> ResponseEntity<T> sendRequest(String apiUrl, HttpEntity<?> requestEntity, HttpMethod method, Class<T> clazz) {
        try {
            return restTemplate.exchange(apiUrl, method, requestEntity, clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Updates getUpdates() {
        String apiUrl = "https://api.telegram.org/bot%s/getUpdates".formatted(apiCode);
        return sendRequest(apiUrl, HttpEntity.EMPTY, HttpMethod.GET, Updates.class).getBody();
    }

    @PostConstruct
    public void updates() {
        Updates updates = getUpdates();
        Map<String, Long> pairs =  updates.result().stream().map(ChatUpdateSummary::message)
                .filter(it -> it.text() != null && !it.text().isBlank())
                .filter(it -> it.text().contains("login:"))
                .map(it -> Map.entry(it.text().replaceAll("login:", "").replaceAll("\\s+", ""), it.chat().id()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Set<AccountEntity> accountEntities = pairs.entrySet().stream().map(entry -> {
            Optional<AccountEntity> account = accountRepository.findByLoginIgnoreCase(entry.getKey());
            account.ifPresent(it -> it.setChatId(String.valueOf(entry.getValue())));
            return account.orElse(null);
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        accountRepository.saveAll(accountEntities);
        this.scheduledExecutorService.schedule(this::updates, 10, TimeUnit.SECONDS);
    }

    public void sendMessage(String chatId, String message) {
        String apiUrl = buildQuery(
                "https://api.telegram.org/bot%s/sendMessage".formatted(apiCode),
                Map.entry("chat_id", chatId),
                Map.entry("text", message)
        );
        sendRequest(apiUrl, HttpEntity.EMPTY, HttpMethod.GET, Void.class);
    }

    private String buildQuery(String apiUrl, Map.Entry<String, String>... props) {
        return apiUrl + "?" + Arrays.stream(props).sequential()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }


    @Override
    public void send(OTPEntity otp) {
        Optional.ofNullable(otp.getAccount()).map(AccountEntity::getChatId).filter(Predicate.not(String::isBlank)).ifPresent(chatId -> {
            sendMessage(chatId, "Your otp code is " + otp.getCode().toString());
        });
    }
}
