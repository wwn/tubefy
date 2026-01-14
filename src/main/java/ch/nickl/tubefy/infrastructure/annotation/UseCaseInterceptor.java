package ch.nickl.tubefy.infrastructure.annotation;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
@UseCase
@Interceptor
public class UseCaseInterceptor {

    @AroundInvoke
    public Object logUseCaseExecution(InvocationContext context) throws Exception {
        String className = context.getTarget().getClass().getSimpleName();
        String methodName = context.getMethod().getName();

        MDC.put("useCase", className);
        MDC.put("method", methodName);
        MDC.put("correlationId", UUID.randomUUID().toString());

        Object[] parameters = context.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] != null) {
                    MDC.put("param" + i, parameters[i].toString());
                }
            }
        }

        log.info("UseCase started: {}.{}", className, methodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = context.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("UseCase finished: {}.{} ({}ms)", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("UseCase failed: {}.{} ({}ms): {}", className, methodName, duration, e.getMessage());
            throw e;
        } finally {
            MDC.remove("useCase");
            MDC.remove("method");
            MDC.remove("correlationId");
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    MDC.remove("param" + i);
                }
            }
        }
    }
}
