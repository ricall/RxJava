package au.org.rma.sandbox.state;

public class StateHolder {
    private static ThreadLocal<State> LOCAL_STATE = new ThreadLocal<>();

    public static void setState(State state) {
        LOCAL_STATE.set(state);
    }

    public static State getState() {
        return LOCAL_STATE.get();
    }

    public static void clear() {
        LOCAL_STATE.remove();
    }
}
