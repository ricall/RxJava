package au.org.rma.sandbox.controller;

import au.org.rma.sandbox.service.TextService;
import au.org.rma.sandbox.state.State;
import au.org.rma.sandbox.state.StateHolder;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rx.Single;

@Slf4j
@RestController
public class TextController {

    @Autowired
    private TextService textService;

    @HystrixCommand(groupKey = "Controller", fallbackMethod = "reverseError")
    @RequestMapping("/reverse/{text}")
    public Single<ResponseEntity<String>> reverse(@PathVariable("text") String text) {
        return textService.reverseText(text)
                .map(reversed -> ResponseEntity.ok(reversed))
                .onErrorReturn(e -> ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service Unavailable: " + e.getMessage()));
    }

    public Single<ResponseEntity<String>> reverseError(String args, Throwable e) {
        log.error("Handling error: {}", args, e);
        return Single.just(ResponseEntity.ok("Service Unavailable"));
    }

    @HystrixCommand(groupKey = "Controller")
    @RequestMapping("/chuck")
    public Single<ResponseEntity<String>> chuck() {
        StateHolder.setState(new State(Long.toString(System.currentTimeMillis())));

        log.info("Controller: {}", StateHolder.getState());
        return textService.chuckNorrisJoke()
                .map(text -> ResponseEntity.ok(text))
                .onErrorReturn(e -> ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Service Unavailable: " + e.getClass() + ":" + e.getMessage()));
    }

}
