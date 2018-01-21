package au.org.rma.sandbox;

import au.org.rma.sandbox.async.AsyncRestTemplateFactory;
import au.org.rma.sandbox.async.AsyncRestTemplateCurrencyRateInterceptor;
import au.org.rma.sandbox.state.StateHolderOnScheduleActionHook;
import au.org.rma.sandbox.state.StateHolderTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;
import rx.plugins.RxJavaHooks;

import java.util.Collections;

@Configuration
public class SandboxConfiguration {

    @Bean
    AsyncRestTemplateFactory asyncRestTemplateFactory() {
        return new AsyncRestTemplateFactory(asyncRestTemplate());
    }

    @Bean
    @Primary
    AsyncRestTemplate asyncRestTemplate() {
        AsyncRestTemplate template = new AsyncRestTemplate(threadPoolTaskExecutor());
        template.setInterceptors(Collections.singletonList(interceptor()));
        return template;
    }

    @Bean
    AsyncRestTemplate interceptorRestTemplate() {
        return new AsyncRestTemplate(threadPoolTaskExecutor());
    }


    @Bean
    AsyncRestTemplateCurrencyRateInterceptor interceptor() {
        return new AsyncRestTemplateCurrencyRateInterceptor(interceptorRestTemplate());
    }

    @Bean
    ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new StateHolderTaskDecorator());
        executor.setMaxPoolSize(20);
        executor.initialize();

        return executor;
    }

    {
        RxJavaHooks.setOnScheduleAction(new StateHolderOnScheduleActionHook());
    }
}
