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
        String rawClassName = context.getTarget().getClass().getSimpleName();
        String className = rawClassName.replaceAll("\\$.*", "").replace("_Subclass", "");
        String methodName = context.getMethod().getName();

        if (MDC.get("correlationId") == null) {
            MDC.put("correlationId", UUID.randomUUID().toString());
        }
        MDC.put("useCase", className);
        MDC.put("method", methodName);

        Object[] parameters = context.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] != null) {
                    MDC.put("param" + i, parameters[i].toString());
                }
            }
        }

        long startTime = System.currentTimeMillis();
        log.info("UseCase started: {}", className);
        try {
            Object result = context.proceed();

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("status", "SUCCESS");

            log.info("UseCase finished: {}", className);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("status", "FAILURE");
            MDC.put("errorReason", e.getMessage());

            log.error("UseCase failed: {} - {}", className, e.getMessage());
            throw e;
        } finally {
            MDC.remove("useCase");
            MDC.remove("method");
            MDC.remove("durationMs");
            MDC.remove("status");
            MDC.remove("errorReason");

            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    MDC.remove("param" + i);
                }
            }
        }
    }
}