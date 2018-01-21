package au.org.rma.sandbox.state;

import lombok.extern.slf4j.Slf4j;
import rx.functions.Action0;
import rx.functions.Func1;

@Slf4j
public class StateHolderOnScheduleActionHook implements Func1<Action0, Action0> {

    @Override
    public Action0 call(Action0 action0) {
        State state = StateHolder.getState();
        return () -> {
            StateHolder.setState(state);
            action0.call();
            StateHolder.clear();
        };
    }
}
