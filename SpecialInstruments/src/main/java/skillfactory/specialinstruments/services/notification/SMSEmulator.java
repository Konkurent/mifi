package skillfactory.specialinstruments.services.notification;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.smpp.Connection;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.SubmitSM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.dao.entity.AccountEntity;
import skillfactory.specialinstruments.dao.entity.OTPEntity;

import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
public class SMSEmulator implements OTPNotificator {

    @Value("${smpp.host}")
    private String host;
    @Value("${smpp.port}")
    private int port;
    @Value("${smpp.systemId}")
    private String systemId;
    @Value("${smpp.password}")
    private String password;

    @SneakyThrows
    public void sendCode(String destination, String code) {
        Connection connection;
        Session session = null;

        try {
            // 1. Установка соединения
            connection = new TCPIPConnection(host, port);
            session = new Session(connection);
            // 2. Подготовка Bind Request
            BindTransmitter bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType("OTP");
            bindRequest.setInterfaceVersion((byte) 0x34); // SMPP v3.4
            bindRequest.setAddressRange("OTPService");
            // 3. Выполнение привязки
            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new Exception("Bind failed: " + bindResponse.getCommandStatus());
            }
            // 4. Отправка сообщения
            SubmitSM submitSM = new SubmitSM();
            submitSM.setSourceAddr("OTPService");
            submitSM.setDestAddr(destination);
            submitSM.setShortMessage("Your code: " + code);

            session.submit(submitSM);
            log.info("[SMS_EMULATOR] To: {}; Message: {}", destination, code);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        finally {
            if (session != null) {
                session.close();
            }
        }
    }


    @Override
    public void send(OTPEntity otp) {
        Optional.ofNullable(otp.getAccount()).map(AccountEntity::getPhone).filter(Predicate.not(String::isBlank)).ifPresent(phone -> {
            sendCode(phone, otp.getCode().toString());
        });
    }

}
