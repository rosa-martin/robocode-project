package sample;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;
import robocode.*;

public class QLearningRobot extends AdvancedRobot {

    private static final double GAMMA = 0.9;
    private static final double ALPHA = 0.1;

    private static final int NUM_OF_INPUTS = 6;

    private double enemyBearing;
    private double enemyDistance;

    private NeuralNetwork<?> network = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, NUM_OF_INPUTS, 10, Action.values().length);
    DataSet trainingSet = new DataSet(NUM_OF_INPUTS, Action.values().length);

    private State currentState;
    private int reward;

    public enum Action {
        MOVE_FORWARD, // Move forward
        MOVE_BACKWARD, // Move backward
        TURN_LEFT, // Turn left
        TURN_RIGHT, // Turn right
        FIRE; // Fire
    
        public int getIndex() {
            return this.ordinal();
        }
    
        public static Action fromIndex(int index) {
            return Action.values()[index];
        }
    }

    private int chooseAction(double[] qValues) {
        return getActionWithMaxQValue(qValues);
    }

    private double[] predict(State state) {
        network.setInput(state.toArray());
        double[] qValues = network.getOutput();
        return qValues;
    }

    private int getActionWithMaxQValue(double[] qValues) {
        int maxIndex = 0;
        for (int i = 0; i < qValues.length; i++) {
            if (qValues[i] > qValues[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private double getMaxQValue(double[] qValues) {
        double maxVal = 0.0;
        for (int i = 0; i < qValues.length; i++) {
            if (qValues[i] > maxVal) {
                maxVal = qValues[i];
            }
        }
        return maxVal;
    }
    
    private void executeAction(int actionIndex) {
        Action action = Action.fromIndex(actionIndex);
        switch (action) {
            case MOVE_FORWARD:
                setAhead(100); // Move forward by 100 pixels
                break;
            case MOVE_BACKWARD:
                setBack(100); // Move backward by 100 pixels
                break;
            case TURN_LEFT:
                setTurnLeft(45); // Turn left by 45 degrees
                break;
            case TURN_RIGHT:
                setTurnRight(45); // Turn right by 45 degrees
                break;
            case FIRE:
                fire(1); // Fire a bullet with power 1
                break;
        }
        execute(); // Executes all pending commands
    }    
    

    public void run() {
        
        while (true) {
            if (currentState == null) {
                currentState = getCurrentState();
            }
            
            double[] currentQValues = predict(currentState);
            int action = chooseAction(currentQValues);
            executeAction(action);

            State nextState = getCurrentState();
            double[] nextQValues = predict(nextState);

            double maxNextQValue = getMaxQValue(nextQValues);
            currentQValues[action] = currentQValues[action] + ALPHA * (reward + GAMMA * maxNextQValue - currentQValues[action]);

            this.trainingSet.add(new DataSetRow(currentState.toArray(), currentQValues));
            this.network.learn(trainingSet);

            currentState = nextState;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Calculate the bearing to the scanned robot
        this.enemyBearing = e.getBearing();
    
        // Get the distance to the scanned robot
        this.enemyDistance = e.getDistance();
    }
    

    private State getCurrentState() {
        // Get our robot's position and heading
        double ourX = getX();
        double ourY = getY();
        double ourHeading = getHeading();
        double ourVelocity = getVelocity();
    
        // Get the enemy's bearing and distance from our robot
        double enemyBearing = this.enemyBearing;
        double enemyDistance = this.enemyDistance;
    
        // Return a new State object with these values
        return new State(ourX, ourY, ourHeading, ourVelocity, enemyBearing, enemyDistance);
    }
    
    public void onHitWall(HitWallEvent e) {
    	reward += -50.0;
    }
    
    public void onHitRobot(HitRobotEvent e) {
    	reward += -30.0;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	reward += -15.0;
    }

    public void onBulletMissed(BulletMissedEvent e) {
    	reward += -15;
    }

    public void onBulletHit(BulletHitEvent e) {
    	reward += 50;
    }
    
    public void onRoundEnded(RoundEndedEvent e) {
		reward = 0;
	}   

    public void onStatus(StatusEvent e) {

		double energy = e.getStatus().getEnergy();
		int enemy_count = e.getStatus().getOthers();
		int max_enemies = 5;
		int enemies_dead = max_enemies - enemy_count;

		if (energy > 0 && enemies_dead > 0)
		{
			reward += 15;
		}
		else if (energy > 0 && enemies_dead > 1)
		{
			reward += 30;
		}
		else if (energy > 0 && enemies_dead > 2)
		{
			reward += 60;
		}
		else if (energy > 0 && enemies_dead > 3)
		{
			reward += 120;
		}
	}
}

class State {
    private double x; // The robot's x position
    private double y; // The robot's y position
    private double heading; // The robot's heading in degrees
    private double velocity; // The robot's velocity
    private double enemyBearing; // The bearing to the enemy from the robot's heading
    private double enemyDistance; // The distance to the enemy

    public State(double x, double y, double heading, double velocity, double enemyBearing, double enemyDistance) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.velocity = velocity;
        this.enemyBearing = enemyBearing;
        this.enemyDistance = enemyDistance;
    }

    // Getters and setters for each field go here

    public double[] toArray() {
        return new double[]{x, y, heading, velocity, enemyBearing, enemyDistance};
    }
}
