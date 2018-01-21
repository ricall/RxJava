package au.org.rma.sandbox.async;

import au.org.rma.sandbox.state.StateHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRequestCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

import java.net.URI;

@Slf4j
public class OAuthAsyncRestTemplate extends AsyncRestTemplate {
    private static String CURRENCY_URL = "https://api.fixer.io/latest?symbols=USD,AUD";

    private AsyncRestTemplate restTemplate;

    public OAuthAsyncRestTemplate(AsyncListenableTaskExecutor taskExecutor, AsyncRestTemplate restTemplate) {
        super(taskExecutor);
        this.restTemplate = restTemplate;
    }

    @Override
    protected <T> ListenableFuture<T> doExecute(URI url, HttpMethod method, AsyncRequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(CURRENCY_URL, HttpMethod.GET, null, String.class);
        SettableListenableFuture<T> settable = new SettableListenableFuture<>();
        log.info("OAuth: {}", StateHolder.getState());

        future.addCallback(response -> {
            try {
                log.info("OAuth Callback: {}", StateHolder.getState());
                AsyncRequestCallback newRequestCallback = requestCallback;
                if (response.getStatusCode().is2xxSuccessful()) {
                    newRequestCallback = request -> {
                        request.getHeaders().set("Currency", response.getBody());
                        requestCallback.doWithRequest(request);
                        log.info("Headers: {}", request.getHeaders());
                    };
                }
                super.doExecute(url, method, newRequestCallback, responseExtractor).addCallback(response2 -> {
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
}
