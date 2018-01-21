package au.org.rma.sandbox.service;

import au.org.rma.sandbox.async.AsyncRestTemplateFactory;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.Single;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TextService {

    @Autowired
    private AsyncRestTemplateFactory templateFactory;

    @HystrixCommand(groupKey="TextService")
    public Single<String> reverseText(String text) {
        return Observable.timer(500, TimeUnit.MILLISECONDS)
                .map(value -> reverseTextImpl(text)).toSingle();
    }

    private String reverseTextImpl(String text) {
        if (text.equals("boom")) {
            throw new IllegalArgumentException("boom doesn't work");
        }
        log.info("Reversed {}", text);
        return StringUtils.reverse(text);
    }

    @HystrixCommand(groupKey="TextService")
    public Single<String> chuckNorrisJoke() {
        return templateFactory.builder("https://api.chucknorris.io/jokes/random")
                .addHeader("User-Agent", "node")
                .exchange(String.class);
    }
}
