package skillfactory.specialinstruments.exception;

public class AccountExistException extends RuntimeException {

    public AccountExistException() {
        super("Error! Account already exist");
    }

}
