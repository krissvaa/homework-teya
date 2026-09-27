package lv.kriss.demo.teya.exception;

public class DuplicateBalanceException extends RuntimeException {

    public DuplicateBalanceException(String message) {
        super(message);
    }
}
