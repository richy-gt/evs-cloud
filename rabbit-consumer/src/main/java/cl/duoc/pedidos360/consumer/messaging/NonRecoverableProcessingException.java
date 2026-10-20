package cl.duoc.pedidos360.consumer.messaging;

public class NonRecoverableProcessingException extends RuntimeException {

    public NonRecoverableProcessingException(String message) {
        super(message);
    }
}