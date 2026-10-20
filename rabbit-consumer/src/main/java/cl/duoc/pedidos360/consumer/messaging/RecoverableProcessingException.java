package cl.duoc.pedidos360.consumer.messaging;

public class RecoverableProcessingException extends RuntimeException {

    public RecoverableProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}