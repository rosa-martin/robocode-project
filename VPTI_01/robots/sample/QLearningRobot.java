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

public class QLearningRobot extends AdvancedRobot {

    private static final int MAX_EPISODES = 1000;       // Number of rounds
    private static final double GAMMA = 0.75;           // How important is the next estimated reward?
    private static final double ALPHA = 0.1;            // How fast shall we converge? -- The learning rate
    private static double EPSILON = 1.0;                // Exploration rate
    private static double EPS_DECAY = 1.0 / MAX_EPISODES;         // The exploration decay rate => We are focusing on exploitation more that exploration.

    private static final int NUM_OF_INPUTS = 10;
    private static final int NUM_OF_OUTPUTS = Action.values().length;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;
    private final static double THRESHOLD = 50.0;

    private double[] currentQValues = new double[NUM_OF_OUTPUTS];
    private int action;

    private int[] NUM_OF_NEURONS_PER_LAYER = new int[]{NUM_OF_INPUTS, 256*NUM_OF_INPUTS, NUM_OF_OUTPUTS};

    private double enemyBearing;
    private double enemyDistance;
    private static int numFire = 0;
    private double lastEnergy = 100.0;

    private MultiLayerPerceptron network = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new HeavysideTransfer());
    private static HashMap<String, double[]> trainingSet = new HashMap<String, double[]>();
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
            if(!trainingSet.containsKey(stringifyField(currentState.toArray()))) {
		    	trainingSet.put(stringifyField(currentState.toArray()), new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
        
            out.println("CURRENT STATE: "+stringifyField(currentState.toArray()));
            
            currentQValues = trainingSet.get(stringifyField(currentState.toArray()));
            
            out.println("CURRENT Q VALUES: "+stringifyField(currentQValues));
            
            // If exploring, we take a random action.
            if (Math.random() < EPSILON) { 
                out.println("TAKING RANDOM ACTION");
                action = rand.nextInt(NUM_OF_OUTPUTS);
            }
            else {
                out.println("TAKING ACTION FROM THE NEURAL NETWORK");
                double[] pred_qs = predict(currentState);
                out.println("PREDICTED Q VALUES: "+stringifyField(pred_qs));
                action = chooseAction(pred_qs);
            }

            out.println("CHOSEN ACTION: "+action);
            executeAction(action);
            currentReward = reward;

            State nextState = getCurrentState();
            if(!trainingSet.containsKey(stringifyField(nextState.toArray()))) {
		    	trainingSet.put(stringifyField(nextState.toArray()), new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
            out.println("NEXT STATE: "+stringifyField(nextState.toArray()));

            out.println("RECEIVED REWARD: "+currentReward);
            double maxQ = getMaxQValue(trainingSet.get(stringifyField(nextState.toArray())));

            for (int i = 0; i<currentQValues.length; i++) {
                currentQValues[i] = currentQValues[i] + ALPHA * (currentReward + GAMMA * maxQ - currentQValues[i]);
            }
            for (int i = 0; i<currentQValues.length; i++) {
                currentQValues[i] = MultiLayerPerceptron.softmax(currentQValues[i], currentQValues);
            }
            
            out.println("UPDATED Q VALUES: "+stringifyField(currentQValues));
            trainingSet.put(stringifyField(currentState.toArray()), currentQValues);
            //out.println("STATE OF THE Q TABLE:");
            //printMap(this.trainingSet);
            
            double error = this.network.backPropagate(currentState.toArray(), trainingSet.get(stringifyField(currentState.toArray())));
            out.println("CURRENT ERROR: " + error);
            
            currentState = nextState;
            // Set the current reward to zero
            reward = 0;
            currentReward = 0;

            if (EPSILON>0.001){
                EPSILON -= EPS_DECAY;
            }     

            // Save everything, when we stop the program -- NOT WORKING YET
            //Signal.handle(new Signal("INT"), new SignalHandler () {
            //public void handle(Signal sig) {
            //    out.println("Received SIGINT signal. Terminating...");
            //    
            //    PrintStream w = null;
            //    try {
            //        w = new PrintStream(new RobocodeFileOutputStream(getDataFile("q_map.dat")));
            //        for(Map.Entry<String, double[]> entry: QLearningRobot.trainingSet.entrySet()) {
            //            w.println(entry.getKey()+":"+entry.getValue());
            //        }
            //        if (w.checkError()) {
            //            out.println("Error reading the training set");
            //        }
            //    } catch (IOException e) {
            //        out.println("IOException trying to write: ");
            //        e.printStackTrace(out);
            //    } finally {
            //        if (w != null) {
            //            w.close();
            //        }
            //    }
            //    // Force exit anyway
            //    System.exit(1);
            //}
          //});
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