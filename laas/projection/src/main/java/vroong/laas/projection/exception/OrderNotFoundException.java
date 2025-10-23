package vroong.laas.projection.exception;

/**
 * Order를 찾을 수 없을 때 발생하는 예외
 */
public class OrderNotFoundException extends RuntimeException {
    
    private final Long orderId;
    
    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
}

