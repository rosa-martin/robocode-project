package sample;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import robocode.*;

public class QLearningRobot extends AdvancedRobot {

    private static final int MAX_EPISODES = 1000;       // Number of rounds
    private static final double GAMMA = 0.75;           // How important is the next estimated reward?
    private static final double ALPHA = 0.1;            // How fast shall we converge? -- The learning rate
    private static double EPSILON = 1.0;                // Exploration rate
    private static double EPS_DECAY = 1.0 / MAX_EPISODES;         // The exploration decay rate => We are focusing on exploitation more that exploration.

    private static final int NUM_OF_INPUTS = 7;

    private double enemyBearing;
    private double enemyDistance;

    private MultiLayerPerceptron network = new MultiLayerPerceptron(new int[]{NUM_OF_INPUTS}, GAMMA, new SigmoidalTransfer());
    HashMap<String, double[]> trainingSet = new HashMap<String, double[]>();
    Random rand = new Random();

    private int reward;

    public static enum Action {
        MOVE_FORWARD, // Move forward
        MOVE_BACKWARD, // Move backward
        TURN_LEFT, // Turn left
        TURN_RIGHT, // Turn right
        TURN_RADAR_LEFT, // Turn radar left
        TURN_RADAR_RIGHT, // Turn radar right
        TURN_GUN_LEFT, // Turn gun left
        TURN_GUN_RIGHT, // Turn gun right
        DO_NOTHING, // Do nothing
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
        double[] qValues = this.network.execute(state.toArray());
        
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
            case TURN_RADAR_LEFT:
                setTurnRadarLeft(45); // Turn left by 45 degrees
                break;
            case TURN_RADAR_RIGHT:
                setTurnRadarRight(45); // Turn right by 45 degrees
                break;
            case TURN_GUN_LEFT:
                setTurnGunLeft(45); // Turn left by 45 degrees
                break;
            case TURN_GUN_RIGHT:
                setTurnGunRight(45); // Turn right by 45 degrees
                break;
            case DO_NOTHING:
                doNothing(); // Do nothing
                break;
            case FIRE:
                fire(1); // Fire a bullet with power 1
                break;
        }
        execute(); // Executes all pending commands
    }    
    
    public static String stringifyField(double[] field){
        String out = "[";

        for (int i = 0; i < field.length; i ++){
            out += String.valueOf(field[i])+", ";
        }

        out += "]";

        return out;
    }

    public void printMap(HashMap<String, double[]> map){
        for (String key : map.keySet())
        {
            out.println(key + ": " + stringifyField(map.get(key)));
        }
    }

    public void run() {
        State currentState = getCurrentState();
        double[] currentQValues = new double[Action.values().length];
        int action = 0;

        while (true) {
            if(!this.trainingSet.containsKey(stringifyField(currentState.toArray()))) {
		    	this.trainingSet.put(stringifyField(currentState.toArray()), new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
            out.println("STATE OF THE Q TABLE:");
            printMap(this.trainingSet);
        
            out.println("CURRENT STATE: "+stringifyField(currentState.toArray()));
            for (String key : this.trainingSet.keySet()){
                out.println(key);
            }
            
            currentQValues = this.trainingSet.get(stringifyField(currentState.toArray()));
            out.println(currentQValues==null);
            
            out.println("CURRENT Q VALUES: "+stringifyField(currentQValues));
            
            // If exploring, we take a random action.
            if (Math.random() < EPSILON) { 
                action = rand.nextInt(Action.values().length) + 1;
            }
            else {
                action = chooseAction(currentQValues);
            }

            out.println("CHOSEN ACTION: "+action);
            executeAction(action);

            State nextState = getCurrentState();
            out.println("NEXT STATE: "+stringifyField(nextState.toArray()));

            out.println("RECEIVED REWARD: "+reward);
            double maxQ = getMaxQValue(this.trainingSet.get(stringifyField(nextState.toArray())));

            currentQValues[action] = currentQValues[action] + ALPHA * (reward + GAMMA * maxQ - currentQValues[action]);
            out.println("UPDATED Q VALUES: "+stringifyField(currentQValues));

            this.trainingSet.put(stringifyField(currentState.toArray()), currentQValues);
            out.println("STATE OF THE Q TABLE:");
            printMap(this.trainingSet);
            
            double error = this.network.backPropagate(currentState.toArray(), this.trainingSet.get(stringifyField(currentState.toArray())));
            out.println("CURRENT ERROR: " + error);
            
            currentState = nextState;
            // Set the current reward to zero
            reward = 0;

            if (EPSILON>0.001){
                EPSILON -= EPS_DECAY;
            }     
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
        double ourEnergy = getEnergy();
    
        // Get the enemy's bearing and distance from our robot
        double enemyBearing = this.enemyBearing;
        double enemyDistance = this.enemyDistance;
    
        // Return a new State object with these values
        return new State(ourX, ourY, ourHeading, ourVelocity, ourEnergy, enemyBearing, enemyDistance);
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
    private double energy; // The robot's energy
    private double enemyBearing; // The bearing to the enemy from the robot's heading
    private double enemyDistance; // The distance to the enemy

    public State(double x, double y, double heading, double velocity, double energy, double enemyBearing, double enemyDistance) {
        this.x = Math.rint(x);
        this.y = Math.rint(y);
        this.heading = Math.rint(heading);
        this.velocity = Math.rint(velocity);
        this.energy = Math.rint(energy);
        this.enemyBearing = Math.rint(enemyBearing);
        this.enemyDistance = Math.rint(enemyDistance);
    }

    // Getters and setters for each field go here

    public double[] toArray() {
        return new double[]{x, y, heading, velocity, energy, enemyBearing, enemyDistance};
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}
