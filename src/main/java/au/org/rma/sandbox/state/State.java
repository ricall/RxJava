package au.org.rma.sandbox.state;

public class State {

    private String state;

    public State(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return this.state;
    }
}
