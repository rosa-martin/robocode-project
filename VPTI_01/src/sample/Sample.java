package sample;

public class Sample {
    private State lastState;
    private int action;
    private double reward;
    private State currentState;
    
    public State getLastState() {
        return lastState;
    }

    public void setLastState(State currentState) {
        this.lastState = currentState;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State nexState) {
        this.currentState = nexState;
    }

    public Sample(State currentState, int action, double currentReward, State nexState) {
        this.lastState = currentState;
        this.action = action;
        this.reward = currentReward;
        this.currentState = nexState;
    }
}
