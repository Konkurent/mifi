package demo.wallet.wallet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.wallet.wallet.constants.OperationType;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@ExtendWith(SpringExtension.class)
class WalletControllerTest {

    @Autowired
    private WalletController walletController;

    @Autowired
    private AuthController authController;

    String session;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        session = authController.signUp(new AuthController.CreateUserPayload("admin", "admin"));
    }

    @Test
    void createLimit() {
        walletController.createLimit(new WalletController.CreateLimitPayload(session, "x1", 100D));
        walletController.createLimit(new WalletController.CreateLimitPayload(session, "x2", 200D));
    }
    
    @Test
    void createOperation() {
        walletController.createOperation(new WalletController.CreateOperationPayload(session, OperationType.DEBITING, "x1", 100D));
        walletController.createOperation(new WalletController.CreateOperationPayload(session, OperationType.CREDITING, "x1", 50D));
        val result = walletController.getOperations(session);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void getGeneralStatistics() throws JsonProcessingException {
         System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletController.getGeneralStatistics(session)));
    }

    @Test
    void getGatherCategoryStats() throws JsonProcessingException {
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletController.getGatherCategoryStats(session, "x1")));
    }
}