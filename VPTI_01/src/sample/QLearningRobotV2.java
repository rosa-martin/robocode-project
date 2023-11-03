package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import tanks.RobocodeRunner;
//import javafx.geometry.Point2D;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintStream;

public class QLearningRobotV2 extends AdvancedRobot {

    private static final int MAX_EPISODES = RobocodeRunner.NUM_OF_ROUNDS;       // Number of rounds
    private static int episode = 0;                     // Number of the current episode
    private static final double GAMMA = 0.9;            // How important is the next estimated reward?
    private static final double ALPHA = 0.1;            // How fast shall we converge? -- The learning rate
    private static double EPS_START = 1.0;              // Maximal (Starting) Exploration rate
    private static double EPS_END = 0.05;               // Minimal (Ending) Exploration rate
    private static int EPS_DECAY = 1000;                // The exploration decay rate => We are focusing on exploitation more that exploration.
    private static int STEPS_DONE = 0;                  // How many times we have made a decision

    private static final int NUM_OF_INPUTS = 15;
    private static final int NUM_OF_OUTPUTS = Action.values().length;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;
    private final static double THRESHOLD = 50.0;
    private final static int TARGET_UPDATE_FREQ = 200;

    private double[] lastQValues = new double[NUM_OF_OUTPUTS];
    private double[] currentQValues = new double[NUM_OF_OUTPUTS];
    private int action;

    private int[] NUM_OF_NEURONS_PER_LAYER = new int[]{NUM_OF_INPUTS, 64, 128, 256, 512, 1024, NUM_OF_OUTPUTS};

    private double enemyBearing;
    private double enemyHeading;
    private double enemyDistance;
    private double enemyX;
    private double enemyY;
    private double enemyVelocity;
    private static int numFire = 0;

    private static double hitWallPen = 0.0;
	private static double hitByBullet = 0.0;
	private static double hitEnemyPen = 0.0;
    private static double bulletMissedPen = 0.0;
    private static double scannedRobotPen = 0.0;
    private static double bulletHitPen = 0.0;
    private static double robotDeathPen = 0.0;

    private MultiLayerPerceptron mainNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new ReLU());
    private MultiLayerPerceptron targetNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new ReLU());
    private ArrayList<Sample> samples = new ArrayList<Sample>();

    //private static HashMap<String, double[]> trainingSet = new HashMap<String, double[]>();
    Random rand = new Random();

    State currentState;
    State lastState;
    private double currentReward;
    private double lastReward;
    private double lastEnergy = 100.0;


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
        SPIN_RADAR, // Do a radar spin
        DO_NOTHING, // Do nothing
        BATCH_FIRE, // Fire a batch of bullets
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
            case SPIN_RADAR:
                setTurnRadarRight(360); // Do a radar spin
                break;
            case DO_NOTHING:
                doNothing(); // Do nothing
                break;
            case FIRE:
                if (enemyDistance <= 80)
                {
                    fire(Rules.MAX_BULLET_POWER); // Fire a huge bullet
                }
                else
                {
                    fire(Rules.MAX_BULLET_POWER/2);
                }
                break;
            case BATCH_FIRE:
                if (enemyDistance <= 80)
                {
                    for (int i = 0; i < 3; i++){
                        fire(Rules.MAX_BULLET_POWER);   // Fire a batch of heavy bullets
                    }
                }
                else
                {
                    for (int i = 0; i < 3; i++){
                        fire(Rules.MAX_BULLET_POWER/2); // Fire a batch of bullets
                    }
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
    if (lastState == null) {
            out.println("INITIAL STATE");
            lastState = getCurrentState();
        }
    for(;;) {
        executeAction(action);
    }
}

    public void onScannedRobot(ScannedRobotEvent e) {
        // Calculate the bearing to the scanned robot
        this.enemyBearing = e.getBearing();
    
        // Get the distance to the scanned robot
        this.enemyDistance = e.getDistance();
        this.enemyHeading = e.getHeading();
        scannedRobotPen = 1;
	
//		************************************************************
//		*******Source: http://robowiki.net/wiki/Linear_Targeting
        double bulletPower = Math.min(3.0,getEnergy());
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
        while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
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
        //fire(bulletPower);
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
        double enemyCount = getOthers();
    
        // Get the enemy's bearing and distance from our robot
        double enemyBearing = this.enemyBearing;
        double enemyDistance = this.enemyDistance;
    
        // Return a new State object with these values
        return new State(ourX, ourY, ourHeading, ourVelocity, ourEnergy, enemyBearing, enemyDistance, ourGunHeat, ourGunHeading, ourRadarHeading, enemyCount, enemyX, enemyY, enemyHeading, enemyVelocity);
    }
    
    public void onHitWall(HitWallEvent e) {
    	hitWallPen = -5.0;
        //moveDirection = -moveDirection;
    }
    
    public void onHitRobot(HitRobotEvent e) {
    	hitEnemyPen = -1.5;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	hitByBullet = -3.0;
    }

    public void onBulletMissed(BulletMissedEvent e) {
    	bulletMissedPen = -0.5;
    }

    public void onBulletHit(BulletHitEvent e) {
    	bulletHitPen = 3;
        if(e.getEnergy() <= 0){
            bulletHitPen = 8;
        }
    }

    public void onRobotDeathEvent(RobotDeathEvent e){
        robotDeathPen = 2.5;
    }

    public void onStatus(StatusEvent e) {
		double energy = e.getStatus().getEnergy();
		int enemy_count = e.getStatus().getOthers();
		int max_enemies = 3;
		int enemies_dead = max_enemies - enemy_count;

        if (lastEnergy > energy)
        {
            currentReward += -1.5;
        }
		if(hitWallPen!=0.0) {
			currentReward += hitWallPen;
			hitWallPen = 0.0;
		}
		if(hitEnemyPen!=0.0) {
			currentReward += hitEnemyPen;
			hitEnemyPen = 0.0;
		}
		if(hitByBullet!=0.0) {
			currentReward += hitByBullet;
			hitByBullet = 0.0;
		}
        if(bulletMissedPen!=0.0) {
			currentReward += bulletMissedPen;
			bulletMissedPen = 0.0;
		}
        if(scannedRobotPen!=0.0) {
			currentReward += scannedRobotPen;
			scannedRobotPen = 0.0;
		}
        if(bulletHitPen!=0.0) {
			currentReward += bulletHitPen;
			bulletHitPen = 0.0;
		}
        if(robotDeathPen!=0.0) {
			currentReward += robotDeathPen;
			robotDeathPen = 0.0;
		}
        /* 
		if (energy > 0 && enemies_dead > 0)
		{
			reward += 30;
		}
		else if (energy > 0 && enemies_dead > 1)
		{
			reward += 60;
		}
        */
        if ((this.getX() > WIDTH - THRESHOLD) || (this.getX() < THRESHOLD) || (this.getY() > HEIGHT - THRESHOLD) || (this.getY() < THRESHOLD)) {
            //out.println("We have reached the threshold");
            //reward -= 5;
            if (this.getDistanceRemaining() < THRESHOLD) {
                //out.println("We are moving towards the wall.");
                currentReward += -0.5;
            }
        }
        
        out.println("CURRENT STATE: "+stringifyField(lastState.toArray()));
        lastQValues = mainNetwork.execute(lastState.toArray());
        out.println("CURRENT Q VALUES: "+stringifyField(lastQValues));
        
        action = chooseAction(lastQValues);

        out.println("ACTION: "+action); 
        out.println("REWARD: "+lastReward);

        currentState = getCurrentState();
        out.println("NEXT STATE: "+stringifyField(currentState.toArray()));
        currentQValues = targetNetwork.execute(currentState.toArray());
        out.println("TARGET Q VALUES: "+stringifyField(currentQValues));

        double maxQ = getMaxQValue(currentQValues);
        
        lastQValues[action] = lastQValues[action] + ALPHA * (lastReward + GAMMA * maxQ - lastQValues[action]);

        //currentQValues = MultiLayerPerceptron.softmax(currentQValues);
        out.println("UPDATED Q VALUES: "+stringifyField(lastQValues));
        
        double error = mainNetwork.backPropagate(lastState.toArray(), lastQValues);
        out.println("HUBER LOSS: "+error);
        //samples.add(new Sample(currentState, action, currentReward, nextState));

        if (episode % TARGET_UPDATE_FREQ == 0) {
            out.println("COPYING WEIGHTS TO TARGET NETWORK");
            mainNetwork.copyWeights(targetNetwork);
        }
        episode ++;
        
        lastEnergy = energy;
		lastState = currentState;
		lastReward = currentReward;
		currentReward = 0.0;
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
    private double enemyCount;
    private double enemyX;
    private double enemyY;
    private double enemyHeading;
    private double enemyVelocity;

    public State(double x, double y, double heading, double velocity, double energy, double enemyBearing, double enemyDistance, double gunHeat, double gunHeading, double radarHeading, double enemyCount, double enemyX, double enemyY, double enemyHeading, double enemyVelocity) {
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
        this.enemyCount = Math.rint(enemyCount);
        this.enemyX = Math.rint(enemyX);
        this.enemyY = Math.rint(enemyY);
        this.enemyHeading = Math.rint(enemyHeading);
        this.enemyVelocity = Math.rint(enemyVelocity);
    }

    // Getters and setters for each field go here

    public double[] toArray() {
        return new double[]{x, y, heading, velocity, energy, enemyBearing, enemyDistance, gunHeat, gunHeading, radarHeading, enemyCount, enemyX, enemyY, enemyHeading, enemyVelocity};
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}

class Sample {
    private State currentState;
    private int action;
    private double reward;
    private State nexState;
    
    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
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

    public State getNexState() {
        return nexState;
    }

    public void setNexState(State nexState) {
        this.nexState = nexState;
    }

    public Sample(State currentState, int action, double currentReward, State nexState) {
        this.currentState = currentState;
        this.action = action;
        this.reward = currentReward;
        this.nexState = nexState;
    }
}
