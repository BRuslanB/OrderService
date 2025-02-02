package kz.bars.order_service.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Кастомное исключение для API, используемое для обработки ошибок в сервисах и контроллерах.
 * Позволяет передавать HTTP-статус ошибки вместе с сообщением.
 */
@Getter
public class ApiException extends RuntimeException {

    /**
     * HTTP-статус ошибки.
     */
    private final HttpStatus status;

    /**
     * Конструктор исключения с сообщением и HTTP-статусом.
     *
     * @param message сообщение об ошибке (будет передано в суперкласс RuntimeException)
     * @param status  HTTP-статус, который должен быть возвращён клиенту
     */
    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

