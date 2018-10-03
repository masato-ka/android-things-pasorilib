package ka.masato.library.device.pasori.driver;

public class FailedRfCommunication extends RuntimeException {
    public FailedRfCommunication(String s) {
        super(s);
    }
}
