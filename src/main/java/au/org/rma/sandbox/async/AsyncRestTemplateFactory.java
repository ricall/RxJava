package au.org.rma.sandbox.async;

import au.org.rma.sandbox.service.InvalidResponseException;
import au.org.rma.sandbox.state.StateHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Single;

import static java.util.Collections.singletonList;

@Slf4j
public class AsyncRestTemplateFactory {

    private AsyncRestTemplate template;

    @Autowired
    public AsyncRestTemplateFactory(AsyncRestTemplate asyncRestTemplate) {
        this.template = asyncRestTemplate;
    }

    public Builder builder(String url) {
        return new Builder(url);
    }

    public class Builder {
        private Builder(String url) {
            this.url = url;
            this.headers = new HttpHeaders();
            headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        }

        private String url;
        private HttpHeaders headers;
        private HttpMethod method = HttpMethod.GET;

        public Builder addHeader(String headerName, String headerValue) {
            headers.set(headerName, headerValue);
            return this;
        }

        public Builder withMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public <T> Single<T> exchange(Class<T> responseType) {
            return exchange(null, responseType);
        }

        public <T> Single<T> exchange(Object body, Class<T> responseType) {
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ListenableFuture<ResponseEntity<T>> future = template.exchange(url, method, entity, responseType);

            return Single.create(subscriber -> {
                log.info("Service: {}", StateHolder.getState());

                future.addCallback(response -> {
                    log.info("Service State: {}", StateHolder.getState());
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Received 2xx response: {}", response.getBody());
                        subscriber.onSuccess(response.getBody());
                    } else {
                        log.error("Received non 2xx response: {}", response.getBody());
                        subscriber.onError(new InvalidResponseException("Unexpected Response"));
                    }
                }, error -> {
                    log.error("Error calling service", error);
                    subscriber.onError(error);
                });
            });
        }


    }
}
