package au.org.rma.sandbox.state;

import org.springframework.core.task.TaskDecorator;

public class StateHolderTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        State state = StateHolder.getState();
        return () -> {
            StateHolder.setState(state);
            runnable.run();
            StateHolder.clear();
        };
    }
}
