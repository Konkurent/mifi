package demo.wallet.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.wallet.wallet.controller.AuthController;
import demo.wallet.wallet.controller.AuthController.CreateUserPayload;
import demo.wallet.wallet.model.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Lazy
    private WalletService walletService;

    private Map<String, User> usersBySessions = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        var user = getUsers()
                .stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
        if(user == null){
            return null;
        }
        var session = java.util.UUID.randomUUID().toString();
        usersBySessions.put(session, user);
        return session;
    }

    public String createUser(CreateUserPayload payload) {
        var user = new User();
        user.setLogin(payload.login());
        user.setPassword(payload.pass());
        usersBySessions.put(UUID.randomUUID().toString(), user);
        return login(payload.login(), payload.pass());
    }

    @SneakyThrows
    private List<User> getUsers() {
        var om = new ObjectMapper();
        return om.readValue(new File("./users.json"), List.class);
    }

    public void logout(String sessionToken) {
        this.usersBySessions.remove(sessionToken);
    }

    public User getUserBySession(String sessionToken) {
        return this.usersBySessions.get(sessionToken);
    }

}
