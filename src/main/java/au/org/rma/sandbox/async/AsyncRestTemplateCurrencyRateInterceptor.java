package au.org.rma.sandbox.async;

import au.org.rma.sandbox.state.StateHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;

@Slf4j
public class AsyncRestTemplateCurrencyRateInterceptor implements AsyncClientHttpRequestInterceptor {
    private static String CURRENCY_URL = "https://api.fixer.io/latest?symbols=USD,AUD";

    private AsyncRestTemplate restTemplate;

    @Autowired
    public AsyncRestTemplateCurrencyRateInterceptor(@Qualifier("interceptorRestTemplate") AsyncRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request, byte[] body, AsyncClientHttpRequestExecution execution) throws IOException {
        if (shouldIntercept(request.getURI())) {
            return enrichRequest(request, body, execution);
        }
        return execution.executeAsync(request, body);
    }

    private boolean shouldIntercept(URI uri) {
        if (uri.getHost().equals("api.fixer.io")) {
            return false;
        }
        return true;
    }

    private ListenableFuture<ClientHttpResponse> enrichRequest(HttpRequest request, byte[] body, AsyncClientHttpRequestExecution execution) {
        ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(CURRENCY_URL, HttpMethod.GET, null, String.class);
        SettableListenableFuture<ClientHttpResponse> settable = new SettableListenableFuture<>();

        future.addCallback(response -> {
            try {
                if (response.getStatusCode().is2xxSuccessful()) {
                    getHeaders(request).add("currency", response.getBody());
                }
                execution.executeAsync(request, body).addCallback(response2 -> {
                    settable.set(response2);
                }, error -> {
                    log.error("Got error on original request");
                    settable.setException(error);
                });
            } catch (Throwable t) {
                log.error("Got error chaining requests", t);
                settable.setException(t);
            }
        }, error -> {
            log.error("Got error on currency request");
            settable.setException(error);
        });
        return settable;
    }

    private HttpHeaders getHeaders(HttpRequest request) {
        try {
            Class clazz = ClassUtils.forName(
                    "org.springframework.http.client.AbstractAsyncClientHttpRequest",
                    Thread.currentThread().getContextClassLoader());
            Field field = ReflectionUtils.findField(clazz, "headers");
            field.setAccessible(true);
            return (HttpHeaders)field.get(request);
        } catch (Throwable t) {
            log.error("Failed to get headers");
            return null;
        }
    }
}
