package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import java.awt.Color;
import tanks.RobocodeRunner;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;

public class QLearningRobotV2 extends AdvancedRobot {

    private static final int MAX_EPISODES = RobocodeRunner.NUM_OF_ROUNDS;       // Number of rounds
    private static final double GAMMA = 0.9;            // How important is the next estimated reward?
    private static final double ALPHA = 0.1;            // How fast shall we converge? -- The learning rate
    private static double EPS_START = 1.0;              // Maximal (Starting) Exploration rate
    private static double EPS_END = 0.05;               // Minimal (Ending) Exploration rate
    private static int EPS_DECAY = 1000;                // The exploration decay rate => We are focusing on exploitation more that exploration.

    private static final int NUM_OF_INPUTS = 19;
    private static final int NUM_OF_OUTPUTS = Action.values().length;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;
    private final static double THRESHOLD = 50.0;
    private final static int TARGET_UPDATE_FREQ = 100;
    private final static int BATCH_SIZE = 30;
    private final static int MEMORY_SIZE = 7500;

    private double[] lastQValues = new double[NUM_OF_OUTPUTS];
    private double[] currentQValues = new double[NUM_OF_OUTPUTS];
    private int action;

    private int[] NUM_OF_NEURONS_PER_LAYER = new int[]{NUM_OF_INPUTS, 64, 128, 256, 512, NUM_OF_OUTPUTS};

    private double enemyBearing;
    private double enemyHeading;
    private double enemyDistance;
    private double enemyX;
    private double enemyY;
    private double enemyVelocity;
    private double enemyEnergy;
    private double bulletBearing; 
    private double bulletHeading;
    private double bulletVelocity;
    private double bulletPower; 
    private double scannedRobots;

    private static double hitWallPen = 0.0;
	private static double hitByBullet = 0.0;
	private static double hitEnemyPen = 0.0;
    private static double bulletMissedPen = 0.0;
    private static double scannedRobotPen = 0.0;
    private static double bulletHitPen = 0.0;
    private static double robotDeathPen = 0.0;

    private MultiLayerPerceptron mainNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new ReLU());
    private MultiLayerPerceptron targetNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, GAMMA, new ReLU());
    
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
        /*
        TURN_RADAR_LEFT, // Turn radar left
        TURN_RADAR_RIGHT, // Turn radar right
        TURN_GUN_LEFT, // Turn gun left
        TURN_GUN_RIGHT, // Turn gun right
        */
        SLOW_DOWN, // Slow down
        FASTER, // Increase the velocity
        SPIN_RADAR, // Do a radar spin
        DO_NOTHING; // Do nothing
        /*
        BATCH_FIRE, // Fire a batch of bullets
        FIRE; // Fire
        */
        public int getIndex() {
            return this.ordinal();
        }
    
        public static Action fromIndex(int index) {
            return Action.values()[index];
        }
    }

    private int chooseAction(double[] qValues) {
        double eps_threshold = EPS_END + (EPS_START - EPS_END) * Math.exp(-1. * RobocodeRunner.STEPS_DONE / EPS_DECAY);
        RobocodeRunner.STEPS_DONE ++;

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
            /*
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
            */
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
            /*
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
            */
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

    public ArrayList<Sample> getSamples(int num) {
        Random rand = new Random();
        ArrayList<Sample> samples = new ArrayList<Sample>();
        
        if (num <= RobocodeRunner.memory.size())
        {
            for (int i = 0; i < num; i++) {
                int index = rand.nextInt(RobocodeRunner.memory.size());
                samples.add(RobocodeRunner.memory.get(index));
            }
        }

        return samples;
    }

public void run() {
    setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
    setBodyColor(Color.black);
    setGunColor(Color.black);
    setRadarColor(Color.black);
    setScanColor(Color.black);
    setBulletColor(Color.black);
    setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
    
    for(;;) {
        executeAction(action);
    }
}

    public void onScannedRobot(ScannedRobotEvent e) {
        // Calculate the bearing to the scanned robot
        this.enemyBearing = e.getBearing();
        this.enemyEnergy = e.getEnergy();
        // Get the distance to the scanned robot
        this.enemyDistance = e.getDistance();
        this.enemyHeading = e.getHeading();
        scannedRobotPen = 1;
	
//		************************************************************
//		*******Source: http://robowiki.net/wiki/Linear_Targeting
        // ... Radar code ..
        final double FIREPOWER = 2;
        final double ROBOT_WIDTH = 16,ROBOT_HEIGHT = 16;
        // Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to robot
        final double eAbsBearing = getHeadingRadians() + e.getBearingRadians();
        final double rX = getX(), rY = getY(),
            bV = Rules.getBulletSpeed(FIREPOWER);
        final double eX = rX + e.getDistance()*Math.sin(eAbsBearing),
            eY = rY + e.getDistance()*Math.cos(eAbsBearing),
            eV = e.getVelocity(),
            eHd = e.getHeadingRadians();
        // These constants make calculating the quadratic coefficients below easier
        final double A = (eX - rX)/bV;
        final double B = eV/bV*Math.sin(eHd);
        final double C = (eY - rY)/bV;
        final double D = eV/bV*Math.cos(eHd);
        // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
        final double a = A*A + C*C;
        final double b = 2*(A*B + C*D);
        final double c = (B*B + D*D - 1);
        final double discrim = b*b - 4*a*c;
        if (discrim >= 0) {
            // Reciprocal of quadratic formula
            final double t1 = 2*a/(-b - Math.sqrt(discrim));
            final double t2 = 2*a/(-b + Math.sqrt(discrim));
            final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
            // Assume enemy stops at walls
            final double endX = limit(
                eX + eV*t*Math.sin(eHd),
                ROBOT_WIDTH/2, getBattleFieldWidth() - ROBOT_WIDTH/2);
            final double endY = limit(
                eY + eV*t*Math.cos(eHd),
                ROBOT_HEIGHT/2, getBattleFieldHeight() - ROBOT_HEIGHT/2);
            setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
                Math.atan2(endX - rX, endY - rY)
                - getGunHeadingRadians()));
            setFire(FIREPOWER);
        }
    }

    private double limit(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
    

    private State getCurrentState() {
        // Get our robot's position and heading
        double ourX = getX();
        double ourY = getY();
        double ourHeading = getHeading();
        double ourVelocity = getVelocity();
        double ourEnergy = getEnergy();
        //double ourGunHeat = getGunHeat();
        //double ourGunHeading = getGunHeading();
        double ourRadarHeading = getRadarHeading();
        double enemyCount = getOthers();
        double scannedRobots = (double) getScannedRobotEvents().size();
    
        // Get the enemy's bearing and distance from our robot
        double enemyBearing = this.enemyBearing;
        double enemyDistance = this.enemyDistance;
        double enemyX = this.enemyX;
        double enemyY = this.enemyY;
        double enemyHeading = this.enemyHeading;
        double enemyVelocity = this.enemyVelocity;
        double enemyEnergy = this.enemyEnergy;
        double bulletHeading = this.bulletHeading;
        double bulletBearing = this.bulletBearing;
        double bulletPower = this.bulletPower;
        double bulletVelocity = this.bulletVelocity;
    
        // Return a new State object with these values
        return new State(ourX, ourY, ourHeading, ourVelocity, ourEnergy, ourRadarHeading, enemyCount, scannedRobots, enemyBearing,
        enemyDistance, enemyX, enemyY, enemyHeading, enemyVelocity, enemyEnergy, bulletHeading, bulletBearing, bulletPower, bulletVelocity);
    }
    
    public void onHitWall(HitWallEvent e) {
    	hitWallPen = -5.0;
        //moveDirection = -moveDirection;
    }
    
    public void onHitRobot(HitRobotEvent e) {
        if (e.getEnergy() < 10){
            hitEnemyPen = 1.5;
        }
        else {
            hitEnemyPen = -1.5;
        }
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        this.bulletBearing = e.getBearing();
        this.bulletHeading = e.getHeading();
        this.bulletVelocity = e.getVelocity();
        this.bulletPower = e.getPower();
    	hitByBullet = -3.0;
    }

    public void onBulletMissed(BulletMissedEvent e) {
    	bulletMissedPen = -0.5;
    }

    public void onBulletHit(BulletHitEvent e) {
        if (e.getBullet().getPower()>=2){
            bulletHitPen = 5;
        }
    	else {
            bulletHitPen = 2;
        }
        if(e.getEnergy() <= 0){
            bulletHitPen = 10;
        }
    }

    public void onRobotDeathEvent(RobotDeathEvent e){
        robotDeathPen = 2.5;
    }

    public void onStatus(StatusEvent e) {
        if (lastState == null) {
            out.println("INITIAL STATE");
            lastState = getCurrentState();
        }

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
        
		if (energy > 0 && enemies_dead > 0)
		{
			currentReward += 1.5;
		}
		else if (energy > 0 && enemies_dead > 1)
		{
			currentReward += 3;
		}
        
        if ((this.getX() > WIDTH - THRESHOLD) || (this.getX() < THRESHOLD) || (this.getY() > HEIGHT - THRESHOLD) || (this.getY() < THRESHOLD)) {
            //out.println("We have reached the threshold");
            //reward -= 5;
            if (this.getDistanceRemaining() < THRESHOLD) {
                //out.println("We are moving towards the wall.");
                currentReward += -1;
            }
        }
        
        //out.println("LAST STATE: "+stringifyField(lastState.toArray()));
        lastQValues = mainNetwork.execute(lastState.toArray());
        //out.println("LAST Q VALUES: "+stringifyField(lastQValues));
        
        action = chooseAction(lastQValues);

        out.println("ACTION: "+action); 
        out.println("REWARD: "+lastReward);

        currentState = getCurrentState();
        //out.println("CURRENT STATE: "+stringifyField(currentState.toArray()));
        currentQValues = targetNetwork.execute(currentState.toArray());
        //out.println("CURRENT Q VALUES: "+stringifyField(currentQValues));
        double error = 0.0;
        out.println("SIZE OF MEMORY: "+RobocodeRunner.memory.size());
        if(RobocodeRunner.memory.size() < BATCH_SIZE) {
            out.println("USING SINGLE INPUT");
            double maxQ = getMaxQValue(currentQValues);
            lastQValues[action] = lastQValues[action] + ALPHA * (lastReward + GAMMA * maxQ - lastQValues[action]);
            error = mainNetwork.backPropagate(lastState.toArray(), lastQValues);
            //out.println("UPDATED Q VALUES: "+stringifyField(lastQValues));
        }
        else{
            out.println("USING BATCH INPUT");
            ArrayList<Sample> trainingSet = getSamples(BATCH_SIZE);
            double[][] lastStates = new double[trainingSet.size()][NUM_OF_INPUTS];
            double[][] currentStates = new double[trainingSet.size()][NUM_OF_INPUTS];
            int[] actions = new int[trainingSet.size()];
            double[] rewards = new double[trainingSet.size()];

            double[][] lastQs = new double[trainingSet.size()][NUM_OF_OUTPUTS];
            double[][] currentQs = new double[trainingSet.size()][NUM_OF_OUTPUTS];
            double[] maxQs = new double[trainingSet.size()];

            for (int i = 0; i < trainingSet.size(); i++) {
                lastStates[i] = trainingSet.get(i).getLastState().toArray();
                currentStates[i] = trainingSet.get(i).getCurrentState().toArray();
                actions[i] = trainingSet.get(i).getAction();
                rewards[i] = trainingSet.get(i).getReward();
            }
            
            lastQs = mainNetwork.executeBatch(lastStates);
            currentQs = targetNetwork.executeBatch(currentStates);
            for (int i = 0; i < trainingSet.size(); i++) {
                maxQs[i] = getMaxQValue(currentQs[i]);

                lastQs[i][actions[i]] = lastQs[i][actions[i]] + ALPHA * (rewards[i] + GAMMA * maxQs[i] - lastQs[i][actions[i]]);
                //out.println("UPDATED Q VALUES: "+stringifyField(lastQs[i]));
            }
            error = mainNetwork.batchBackPropagate(lastStates, lastQs);
        }
        
        out.println("HUBER LOSS: "+error);
        if (RobocodeRunner.memory.size() >= MEMORY_SIZE) {
            RobocodeRunner.memory.remove(0);
        }
        RobocodeRunner.memory.add(new Sample(lastState, action, lastReward, currentState));

        if (RobocodeRunner.CURRENT_EPISODE % TARGET_UPDATE_FREQ == 0) {
            out.println("COPYING WEIGHTS TO TARGET NETWORK");
            mainNetwork.copyWeights(targetNetwork);
        }
        
        RobocodeRunner.CURRENT_EPISODE ++;
        lastEnergy = energy;
		lastState = currentState;
		lastReward = currentReward;
		currentReward = 0.0;
	}
}