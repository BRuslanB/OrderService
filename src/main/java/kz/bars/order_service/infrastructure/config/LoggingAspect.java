package kz.bars.order_service.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Логирование перед выполнением методов контроллеров.
     */
    @Before("execution(* com.example.project.presentation.controllers.*.*(..))")
    public void logBeforeControllerMethods(JoinPoint joinPoint) {
        log.info("Перед выполнением метода: {} с аргументами: {}",
                joinPoint.getSignature().toShortString(),
                joinPoint.getArgs());
    }

    /**
     * Логирование после выполнения методов сервисного слоя.
     */
    @After("execution(* com.example.project.application.services.*.*(..))")
    public void logAfterServiceMethods(JoinPoint joinPoint) {
        log.info("После выполнения метода: {}", joinPoint.getSignature().toShortString());
    }

    /**
     * Логирование результата методов репозиториев.
     */
    @AfterReturning(value = "execution(* com.example.project.domain.repositories.*.*(..))", returning = "result")
    public void logAfterReturningRepositoryMethods(JoinPoint joinPoint, Object result) {
        log.info("Метод: {} выполнен. Возвращаемое значение: {}",
                joinPoint.getSignature().toShortString(),
                result != null ? result : "null");
    }

    /**
     * Логирование исключений, возникающих в любом слое приложения.
     */
    @AfterThrowing(value = "execution(* com.example.project..*.*(..))", throwing = "exception")
    public void logExceptions(JoinPoint joinPoint, Throwable exception) {
        if (exception != null) {
            log.error("Исключение в методе: {}. Ошибка: {}",
                    joinPoint.getSignature().toShortString(),
                    exception.getMessage(),
                    exception);
        }
    }

    /**
     * Логирование выполнения времени методов сервисного слоя.
     */
    @Around("execution(* com.example.project.application.services.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed(); // Выполняем метод
            long endTime = System.currentTimeMillis();
            log.info("Метод: {} выполнен за {} мс",
                    joinPoint.getSignature().toShortString(),
                    (endTime - startTime));
            return result;
        } catch (Throwable throwable) {
            log.error("Ошибка в методе {}: {}",
                    joinPoint.getSignature().toShortString(),
                    throwable.getMessage(),
                    throwable);
            throw throwable;
        }
    }
}
