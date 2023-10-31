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

    private static final int NUM_OF_INPUTS = 8;
    private static final int NUM_OF_OUTPUTS = Action.values().length;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;
    private final static double THRESHOLD = 50.0;

    private int[] NUM_OF_NEURONS_PER_LAYER = new int[]{NUM_OF_INPUTS, 2*NUM_OF_INPUTS, 4*NUM_OF_INPUTS, 8*NUM_OF_INPUTS, 16*NUM_OF_INPUTS, 32*NUM_OF_INPUTS, 64*NUM_OF_INPUTS, 128*NUM_OF_INPUTS, 256*NUM_OF_INPUTS, NUM_OF_OUTPUTS};

    private double enemyBearing;
    private double enemyDistance;
    private double moveDirection = 1;

    private MultiLayerPerceptron network = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new SigmoidalTransfer());
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
                setAhead(100*moveDirection); // Move forward by 100 pixels
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

        for(;;) {
            if(!this.trainingSet.containsKey(stringifyField(currentState.toArray()))) {
		    	this.trainingSet.put(stringifyField(currentState.toArray()), new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
            out.println("STATE OF THE Q TABLE:");
            //printMap(this.trainingSet);
        
            out.println("CURRENT STATE: "+stringifyField(currentState.toArray()));
            
            currentQValues = this.trainingSet.get(stringifyField(currentState.toArray()));
            out.println(currentQValues==null);
            
            out.println("CURRENT Q VALUES: "+stringifyField(currentQValues));
            
            // If exploring, we take a random action.
            if (Math.random() < EPSILON) { 
                action = rand.nextInt(Action.values().length);
            }
            else {
                action = chooseAction(currentQValues);
            }

            out.println("CHOSEN ACTION: "+action);
            executeAction(action);

            State nextState = getCurrentState();
            if(!this.trainingSet.containsKey(stringifyField(nextState.toArray()))) {
		    	this.trainingSet.put(stringifyField(nextState.toArray()), new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
            out.println("NEXT STATE: "+stringifyField(nextState.toArray()));

            out.println("RECEIVED REWARD: "+reward);
            double maxQ = getMaxQValue(this.trainingSet.get(stringifyField(nextState.toArray())));

            currentQValues[action] = currentQValues[action] + ALPHA * (reward + GAMMA * maxQ - currentQValues[action]);

            for(int i = 0; i < currentQValues.length; i++){
                currentQValues[i] = MultiLayerPerceptron.softmax(currentQValues[i], currentQValues);
            }
            out.println("UPDATED Q VALUES: "+stringifyField(currentQValues));
            this.trainingSet.put(stringifyField(currentState.toArray()), currentQValues);
            //out.println("STATE OF THE Q TABLE:");
            //printMap(this.trainingSet);
            
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
        double gunHeat = getGunHeat();
    
        // Get the enemy's bearing and distance from our robot
        double enemyBearing = this.enemyBearing;
        double enemyDistance = this.enemyDistance;
    
        // Return a new State object with these values
        return new State(ourX, ourY, ourHeading, ourVelocity, ourEnergy, enemyBearing, enemyDistance, gunHeat);
    }
    
    public void onHitWall(HitWallEvent e) {
    	reward += -50.0;
        moveDirection = -moveDirection;
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
    	reward += 80;
    }

    public void onRobotDeathEvent(RobotDeathEvent e){
        reward += 80;
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

        if ((this.getX() > WIDTH - THRESHOLD) || (this.getX() < THRESHOLD) || (this.getY() > HEIGHT - THRESHOLD) || (this.getY() < THRESHOLD)) {
            out.println("We have reached the threshold");
            reward -= 5;

            if (this.getDistanceRemaining() < THRESHOLD) {
                
                out.println("We are moving towards the wall.");
                reward -= 15;
            }
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
    private double gunHeat; //heat of gun

    public State(double x, double y, double heading, double velocity, double energy, double enemyBearing, double enemyDistance, double gunHeat) {
        this.x = Math.rint(x);
        this.y = Math.rint(y);
        this.heading = Math.rint(heading);
        this.velocity = Math.rint(velocity);
        this.energy = Math.rint(energy);
        this.enemyBearing = Math.rint(enemyBearing);
        this.enemyDistance = Math.rint(enemyDistance);
        this.gunHeat = Math.rint(gunHeat);
    }

    // Getters and setters for each field go here

    public double[] toArray() {
        return new double[]{x, y, heading, velocity, energy, enemyBearing, enemyDistance, gunHeat};
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}
