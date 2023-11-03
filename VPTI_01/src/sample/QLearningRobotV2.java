package sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sun.misc.Signal;
import sun.misc.SignalHandler;

//import javafx.geometry.Point2D;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintStream;

public class QLearningRobotV2 extends AdvancedRobot {

    private static final int MAX_EPISODES = 1000;       // Number of rounds
    private static final double GAMMA = 0.75;           // How important is the next estimated reward?
    private static final double ALPHA = 0.1;            // How fast shall we converge? -- The learning rate
    private static double EPS_START = 0.9;              // Maximal (Starting) Exploration rate
    private static double EPS_END = 0.05;               // Minimal (Ending) Exploration rate
    private static int EPS_DECAY = 1000;                // The exploration decay rate => We are focusing on exploitation more that exploration.
    private static int STEPS_DONE = 0;                  // How many times we have made a decision

    private static final int NUM_OF_INPUTS = 10;
    private static final int NUM_OF_OUTPUTS = Action.values().length;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;
    private final static double THRESHOLD = 50.0;
    private final static int TARGET_UPDATE_FREQ = 50;

    private double[] currentQValues = new double[NUM_OF_OUTPUTS];
    private int action;

    private int[] NUM_OF_NEURONS_PER_LAYER = new int[]{NUM_OF_INPUTS, 256*NUM_OF_INPUTS, NUM_OF_OUTPUTS};

    private double enemyBearing;
    private double enemyDistance;
    private static int numFire = 0;
    private double lastEnergy = 100.0;

    private MultiLayerPerceptron mainNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new HeavysideTransfer());
    private MultiLayerPerceptron targetNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new HeavysideTransfer());

    //private static HashMap<String, double[]> trainingSet = new HashMap<String, double[]>();
    Random rand = new Random();

    private int reward;
    private double currentReward;


    public static enum Action {
        MOVE_FORWARD, // Move forward
        MOVE_BACKWARD, // Move backward
        TURN_LEFT, // Turn left
        TURN_RIGHT, // Turn right
        TURN_RADAR_LEFT, // Turn radar left
        TURN_RADAR_RIGHT, // Turn radar right
        TURN_GUN_LEFT, // Turn gun left
        TURN_GUN_RIGHT, // Turn gun right
        SLOW_DOWN, // Slow down
        FASTER, // Increase the velocity
        LOCK_THE_RADAR, // Lock the radar
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
        double eps_threshold = EPS_END + (EPS_START - EPS_END) * Math.exp(-1. * STEPS_DONE / EPS_DECAY);
        STEPS_DONE ++;

        if (Math.random() < eps_threshold) { 
            out.println("TAKING RANDOM ACTION");
            action = rand.nextInt(NUM_OF_OUTPUTS);
        }
        else {
            out.println("TAKING ACTION FROM THE NN");
            action = getActionWithMaxQValue(qValues);
        }
        return action;
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
                if(getScannedRobotEvents().size()==0) {
				    setTurnRadarRight(360);
			    }
                setAhead(40); // Move forward by 40 pixels
                break;
            case MOVE_BACKWARD:
                setBack(40); // Move backward by 40 pixels
                break;
            case TURN_LEFT:
                setTurnLeft(30); // Turn left by 30 degrees
                break;
            case TURN_RIGHT:
                setTurnRight(30); // Turn right by 30 degrees
                break;
            case TURN_RADAR_LEFT:
                setTurnRadarLeft(90); // Turn left by 90 degrees
                break;
            case TURN_RADAR_RIGHT:
                setTurnRadarRight(90); // Turn right by 90 degrees
                break;
            case TURN_GUN_LEFT:
                setTurnGunLeft(30); // Turn left by 30 degrees
                break;
            case TURN_GUN_RIGHT:
                setTurnGunRight(30); // Turn right by 30 degrees
                break;
            case SLOW_DOWN:
                setMaxVelocity(this.getVelocity()/2); // Slow down
                break;
            case FASTER:
                setMaxVelocity(this.getVelocity()*2); // Increase the velocity
                break;
            case LOCK_THE_RADAR:
                setTurnRadarLeftRadians(getRadarTurnRemainingRadians());    // Lock the radar
                break;
            case DO_NOTHING:
                doNothing(); // Do nothing
                break;
            case FIRE:
                if (enemyDistance <= 30)
                {
                    fire(Rules.MAX_BULLET_POWER); // Fire a bullet with maximal power because the enemy is nearby
                }
                else if (enemyDistance < 30 && enemyDistance < 80)
                {
                    fire(Rules.MAX_BULLET_POWER/2);
                }
                else{
                    fire(Rules.MIN_BULLET_POWER); // Fire a bullet with minimal power because the enemy is far away
                }
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
    for(;;) {
        int iteration = 1;

        currentQValues = mainNetwork.execute(currentState.toArray());
        out.println("CURRENT Q VALUES: "+stringifyField(currentQValues));
        
        chooseAction(currentQValues);
        
        executeAction(action);
        out.println("ACTION: "+action);
        currentReward = reward;
        out.println("REWARD: "+currentReward);

        State nextState = getCurrentState();
        double[] target = targetNetwork.execute(nextState.toArray());
        out.println("TARGET Q VALUES: "+stringifyField(target));

        double maxQ = getMaxQValue(target);
        
        for (int i = 0; i < NUM_OF_OUTPUTS; i++) {
            currentQValues[i] = currentQValues[i] + ALPHA * (currentReward + GAMMA * maxQ - currentQValues[i]);
        }
        out.println("UPDATED Q VALUES: "+stringifyField(currentQValues));
        for (int i = 0; i < NUM_OF_OUTPUTS; i++) {
            currentQValues[i] = MultiLayerPerceptron.softmax(currentQValues[i], currentQValues);
        }
        out.println("AFTER SOFTMAX: "+stringifyField(currentQValues));
        
        double error = mainNetwork.backPropagate(currentState.toArray(), currentQValues);
        out.println("HUBER LOSS: "+error);
        
        currentState = nextState;
        reward = 0;
        currentReward = 0;
        
        // Update the target network weights less frequently
        // TODO:
        /*
        if (iteration % TARGET_UPDATE_FREQ == 0) {
            targetNetwork.setWeights(mainNetwork.getWeights());
        }
        */
        iteration ++;
    }
}

    public void onScannedRobot(ScannedRobotEvent e) {
        // Calculate the bearing to the scanned robot
        this.enemyBearing = e.getBearing();
    
        // Get the distance to the scanned robot
        this.enemyDistance = e.getDistance();
        
        reward += 20;

		int dist = (int) Math.round(e.getDistance()/10);
		double power = 3.0;

		if(getOthers()==1) {
			power = 3;
			if(dist<30&&dist>=15) {power = 2.5;};
			if(dist<15) {power = 3;};
		}

		
//		************************************************************
//		*******Source: http://robowiki.net/wiki/Linear_Targeting ***
		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = e.getHeadingRadians();
		double enemyVelocity = e.getVelocity();
		 
		 
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight(), 
		       battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while((++deltaTime) * (20.0 - 3.0 * power) < 
		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
			predictedX += Math.sin(enemyHeading) * enemyVelocity;	
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			if(	predictedX < 18.0 
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0){
				predictedX = Math.min(Math.max(18.0, predictedX), 
		                    battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), 
		                    battleFieldHeight - 18.0);
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(
		    predictedX - getX(), predictedY - getY()));
		 
		setTurnRadarRightRadians(
		    Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
//		***********************************************************
//		***********************************************************
		
		fire(power);
		numFire++;
		if(numFire!=2) {
			scan();
		}
		numFire = 0;
		setTurnRadarRight(360);
    }
    

    private State getCurrentState() {
        // Get our robot's position and heading
        double ourX = getX();
        double ourY = getY();
        double ourHeading = getHeading();
        double ourVelocity = getVelocity();
        double ourEnergy = getEnergy();
        double ourGunHeat = getGunHeat();
        double ourGunHeading = getGunHeading();
        double ourRadarHeading = getRadarHeading();
        
    
        // Get the enemy's bearing and distance from our robot
        double enemyBearing = this.enemyBearing;
        double enemyDistance = this.enemyDistance;
    
        // Return a new State object with these values
        return new State(ourX, ourY, ourHeading, ourVelocity, ourEnergy, enemyBearing, enemyDistance, ourGunHeat, ourGunHeading, ourRadarHeading);
    }
    
    public void onHitWall(HitWallEvent e) {
    	reward += -75.0;
        //moveDirection = -moveDirection;
    }
    
    public void onHitRobot(HitRobotEvent e) {
    	reward += -10.0;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	reward += -50.0;
    }

    public void onBulletMissed(BulletMissedEvent e) {
    	reward += -10;
    }

    public void onBulletHit(BulletHitEvent e) {
    	reward += 25;
        if(e.getEnergy() <= 0){
            reward += 150;
        }
    }

    public void onRobotDeathEvent(RobotDeathEvent e){
        reward += 100;
    }

    public void onStatus(StatusEvent e) {
		double energy = e.getStatus().getEnergy();
		int enemy_count = e.getStatus().getOthers();
		int max_enemies = 4;
		int enemies_dead = max_enemies - enemy_count;

        if (lastEnergy > energy)
        {
            reward += -15;
        }

		//if (energy > 0 && enemies_dead > 0)
		//{
		//	reward += 15;
		//}
		//else if (energy > 0 && enemies_dead > 1)
		//{
		//	reward += 30;
		//}
		//else if (energy > 0 && enemies_dead > 2)
		//{
		//	reward += 60;
		//}
		//else if (energy > 0 && enemies_dead > 3)
		//{
		//	reward += 120;
		//}

        if ((this.getX() > WIDTH - THRESHOLD) || (this.getX() < THRESHOLD) || (this.getY() > HEIGHT - THRESHOLD) || (this.getY() < THRESHOLD)) {
            out.println("We have reached the threshold");
            reward -= 5;

            if (this.getDistanceRemaining() < THRESHOLD) {
                
                out.println("We are moving towards the wall.");
                reward -= 30;
            }
        } 
        lastEnergy = energy;
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
    private double gunHeading; // The heading of the gun in degrees
    private double radarHeading; // The heading of the radar in degrees

    public State(double x, double y, double heading, double velocity, double energy, double enemyBearing, double enemyDistance, double gunHeat, double gunHeading, double radarHeading) {
        this.x = Math.rint(x);
        this.y = Math.rint(y);
        this.heading = Math.rint(heading);
        this.velocity = Math.rint(velocity);
        this.energy = Math.rint(energy);
        this.enemyBearing = Math.rint(enemyBearing);
        this.enemyDistance = Math.rint(enemyDistance);
        this.gunHeat = Math.rint(gunHeat);
        this.gunHeading = Math.rint(gunHeading);
        this.radarHeading = Math.rint(radarHeading);
    }

    // Getters and setters for each field go here

    public double[] toArray() {
        return new double[]{x, y, heading, velocity, energy, enemyBearing, enemyDistance, gunHeat, gunHeading, radarHeading};
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}
