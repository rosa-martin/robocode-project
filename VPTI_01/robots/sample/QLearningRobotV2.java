package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

import java.awt.Color;
import tanks.RobocodeRunner;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;

public class QLearningRobotV2 extends AdvancedRobot {

    private static final int MAX_EPISODES = RobocodeRunner.NUM_OF_ROUNDS; // Number of rounds
    private static final double GAMMA = 0.7; // How important is the next estimated reward?
    private static final double ALPHA = RobocodeRunner.ALPHA; // How fast shall we converge? -- The learning rate
    private static double EPS_START = 1.0; // Maximal (Starting) Exploration rate
    private static double EPS_END = 0.05; // Minimal (Ending) Exploration rate
    private static int EPS_DECAY = 10000; // The exploration decay rate => We are focusing on exploitation more
                                          // than on exploration.

    private static final int NUM_OF_INPUTS = RobocodeRunner.NUM_OF_INPUTS;
    private static final int NUM_OF_OUTPUTS = Action.values().length;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;
    private final static double THRESHOLD = 50.0;
    private final static int TARGET_UPDATE_FREQ = 150;
    private final static int BATCH_SIZE = RobocodeRunner.BATCH_SIZE;
    private final static int MEMORY_SIZE = 2500;

    private double[] lastQValues = new double[NUM_OF_OUTPUTS];
    private double[] currentQValues = new double[NUM_OF_OUTPUTS];
    private int currentAction;
    private int lastAction;

    SigmoidalTransfer sigmoid = new SigmoidalTransfer();

    private int[] NUM_OF_NEURONS_PER_LAYER = new int[] { NUM_OF_INPUTS, 64, 128, 256, 512, NUM_OF_OUTPUTS };

    private double bulletBearing;
    private double bulletHeading;
    private double bulletVelocity;
    private double bulletPower;

    private double speedIndex = 1;
    private int max_enemies = 3;
    private int current_enemies = max_enemies;
    private HashMap<String, ScannedRobotEvent> enemies = new HashMap<String, ScannedRobotEvent>();
    
    private double total_round_loss = 0.0;
    private int cnt = 0;

    private static double hitWallPen = 0.0;
    private static double hitByBullet = 0.0;
    private static double hitEnemyPen = 0.0;
    private static double bulletMissedPen = 0.0;
    private static double bulletHitBulletPen = 0.0;
    private static double scannedRobotPen = 0.0;
    private static double bulletHitPen = 0.0;
    private static double robotDeathPen = 0.0;

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
        SPIN_RADAR; // Do a radar spin
        /*
         * DO_NOTHING, // self-explanatory
         * LOCK_RADAR, //lock radar on certain position
         * LOCK_GUN; // Do nothing
         * */
         //FIRE; // Fire

        public int getIndex() {
            return this.ordinal();
        }

        public static Action fromIndex(int index) {
            return Action.values()[index];
        }
    }

    private int chooseAction() {
        double eps_threshold = EPS_END + (EPS_START - EPS_END) * Math.exp(-1. * RobocodeRunner.STEPS_DONE / EPS_DECAY);
        RobocodeRunner.STEPS_DONE++;

        if (Math.random() < eps_threshold) {
            out.println("TAKING RANDOM ACTION");
            currentAction = rand.nextInt(NUM_OF_OUTPUTS);
        } else {
            // if(!zerosCheck(qValues)){
            // currentAction = rand.nextInt(NUM_OF_OUTPUTS);
            // } else {
            out.println("TAKING ACTION FROM THE NN");
            currentAction = getActionWithMaxQValue(lastQValues);
            // }
        }
        return currentAction;
    }

    private int getActionWithMaxQValue(double[] qValues) {
        int maxIndex = 0;
        double maxVal = 0;
        for (int i = 0; i < qValues.length; i++) {
            if (qValues[i] > maxVal) {
                maxVal = qValues[i];
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

    private void executeAction() {
        Action action = Action.fromIndex(chooseAction());
        switch (action) {
            case MOVE_FORWARD:
                out.println("MOVE_FORWARD");
                currentAction = Action.MOVE_FORWARD.getIndex();
                setAhead(45); // Move forward by 45 pixels
                break;
            case MOVE_BACKWARD:
                out.println("MOVE_BACKWARD");
                currentAction = Action.MOVE_BACKWARD.getIndex();
                setBack(45); // Move backward by 45 pixels
                break;
            case TURN_LEFT:
                out.println("TURN_LEFT");
                currentAction = Action.TURN_LEFT.getIndex();
                setTurnLeft(15); // Turn left by 45 degrees
                break;
            case TURN_RIGHT:
                out.println("TURN_RIGHT");
                currentAction = Action.TURN_RIGHT.getIndex();
                setTurnRight(15); // Turn right by 45 degrees
                break;
            /*
            case TURN_RADAR_LEFT:
            out.println("TURN_RADAR_LEFT");
            setTurnRadarLeft(45); // Turn radar left by 45 degrees
            break;
            case TURN_RADAR_RIGHT:
            out.println("TURN_RADAR_RIGHT");
            setTurnRadarRight(45); // Turn radar right by 45 degrees
            break;
            case TURN_GUN_LEFT:
            out.println("TURN_GUN_LEFT");
            setTurnGunLeft(45); // Turn gun left by 45 degrees
            break;
            case TURN_GUN_RIGHT:
            out.println("TURN_GUN_RIGHT");
            setTurnGunRight(45); // Turn gun right by 45 degrees
            break;
            */
            case SLOW_DOWN:
                out.println("SLOW_DOWN");
                currentAction = Action.SLOW_DOWN.getIndex();
                /*
                setMaxVelocity(Rules.MAX_VELOCITY / (2 * speedIndex)); // Slow down
                speedIndex++;
                if (speedIndex == 4) {
                    speedIndex = 1;
                }
                */
                setMaxVelocity(this.getVelocity() - 1.0);
                break;
            case FASTER:
                out.println("FASTER");
                currentAction = Action.FASTER.getIndex();
                /*
                if (getVelocity() < Rules.MAX_VELOCITY / 2) {
                    setMaxVelocity(Rules.MAX_VELOCITY / 2); // Increase the velocity
                } else {
                    setMaxVelocity(Rules.MAX_VELOCITY); // Increase the velocity
                }
                */
                setMaxVelocity(this.getVelocity() + 1.0);
                break;
            case SPIN_RADAR:
                out.println("SPIN_RADAR");
                currentAction = Action.SPIN_RADAR.getIndex();
                if (rand.nextBoolean()) {
                    setTurnRadarRightRadians(Double.POSITIVE_INFINITY); // Do a radar spin
                } else {
                    setTurnRadarLeftRadians(Double.POSITIVE_INFINITY); // Do a radar spin
                }
                break;
            /*
             * case DO_NOTHING:
             * out.println("DO_NOTHING");
             * currentAction = Action.DO_NOTHING.getIndex();
             * doNothing(); // Do nothing
             * break;
            case FIRE:
            out.println("FIRE");
            currentAction = Action.FIRE.getIndex();
            if (getEnergy() > 50)
            {
            setFire(Rules.MAX_BULLET_POWER); // Fire a huge bullet
            }
            else
            {
            setFire(Rules.MAX_BULLET_POWER/2);
            }
            break;
            /*
            case LOCK_RADAR:
            out.println("LOCK_RADAR");
            currentAction = Action.LOCK_RADAR.getIndex();
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
            break;
            case LOCK_GUN:
            out.println("LOCK_GUN");
            currentAction = Action.LOCK_GUN.getIndex();
            setTurnGunLeftRadians(getGunTurnRemainingRadians());
            break;
            */
        }
        execute(); // Executes all pending commands
    }

    public static String stringifyField(double[] field) {
        String out = "[";

        for (int i = 0; i < field.length; i++) {
            out += String.valueOf(field[i]) + ", ";
        }

        out += "]";

        return out;
    }

    public void printMap(HashMap<String, double[]> map) {
        for (String key : map.keySet()) {
            out.println(key + ": " + stringifyField(map.get(key)));
        }
    }

    public ArrayList<Sample> getSamples(int num) {
        Random rand = new Random();
        ArrayList<Sample> samples = new ArrayList<Sample>();

        if (num <= RobocodeRunner.memory.size()) {
            for (int i = 0; i < num; i++) {
                int index = rand.nextInt(RobocodeRunner.memory.size());
                samples.add(RobocodeRunner.memory.get(index));
            }
        }

        return samples;
    }

    public void run() {
        setAdjustRadarForRobotTurn(true);// keep the radar still while we turn
        setBodyColor(Color.black);
        setGunColor(Color.black);
        setRadarColor(Color.black);
        setScanColor(Color.black);
        setBulletColor(Color.black);
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn

        while (current_enemies != 0) {
            executeAction();
            out.println("Round loss: "+total_round_loss+" Counter: "+cnt+" Average: "+total_round_loss/cnt);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (enemies.containsKey(e.getName())) {
            scannedRobotPen = 5;
        } else {
            scannedRobotPen = 10;
        }

        enemies.put(e.getName(), e);

        Set<Entry<String, ScannedRobotEvent>> setOfEntries = enemies.entrySet();
        // get the iterator from entry set
        Iterator<Entry<String, ScannedRobotEvent>> iterator = setOfEntries.iterator();

        // iterate over map
        while (iterator.hasNext()) {
            Entry<String, ScannedRobotEvent> entry = iterator.next();
            ScannedRobotEvent sre = entry.getValue();

            if (sre.getTime() < (getTime() - 10)) {
                iterator.remove();
            }
        }

        double radarTurn =
                // Absolute bearing to target
                getHeadingRadians() + enemies.get(e.getName()).getBearingRadians()
                // Subtract current radar heading to get turn required
                        - getRadarHeadingRadians();

        setTurnRadarRightRadians(2.0 * Utils.normalRelativeAngle(radarTurn));

        // ************************************************************
        // *******Source: http://robowiki.net/wiki/Linear_Targeting
        // ... Radar code ..
        double FIREPOWER;

        if (enemies.get(e.getName()).getDistance() > 350.0) {
            FIREPOWER = 1.75;
        } else if (enemies.get(e.getName()).getDistance() > 200.0) {
            FIREPOWER = 2.25;
        } else {
            FIREPOWER = 3.0;
        }

        final double ROBOT_WIDTH = 16, ROBOT_HEIGHT = 16;
        // Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to
        // robot
        final double eAbsBearing = getHeadingRadians() + enemies.get(e.getName()).getBearingRadians();
        final double rX = getX(), rY = getY(),
                bV = Rules.getBulletSpeed(FIREPOWER);
        final double eX = rX + enemies.get(e.getName()).getDistance() * Math.sin(eAbsBearing),
                eY = rY + enemies.get(e.getName()).getDistance() * Math.cos(eAbsBearing),
                eV = enemies.get(e.getName()).getVelocity(),
                eHd = enemies.get(e.getName()).getHeadingRadians();
        // These constants make calculating the quadratic coefficients below easier
        final double A = (eX - rX) / bV;
        final double B = eV / bV * Math.sin(eHd);
        final double C = (eY - rY) / bV;
        final double D = eV / bV * Math.cos(eHd);
        // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
        final double a = A * A + C * C;
        final double b = 2 * (A * B + C * D);
        final double c = (B * B + D * D - 1);
        final double discrim = b * b - 4 * a * c;
        if (discrim >= 0) {
            // Reciprocal of quadratic formula
            final double t1 = 2 * a / (-b - Math.sqrt(discrim));
            final double t2 = 2 * a / (-b + Math.sqrt(discrim));
            final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
            // Assume enemy stops at walls
            final double endX = limit(
                    eX + eV * t * Math.sin(eHd),
                    ROBOT_WIDTH / 2, getBattleFieldWidth() - ROBOT_WIDTH / 2);
            final double endY = limit(
                    eY + eV * t * Math.cos(eHd),
                    ROBOT_HEIGHT / 2, getBattleFieldHeight() - ROBOT_HEIGHT / 2);
            setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
                    Math.atan2(endX - rX, endY - rY)
                            - getGunHeadingRadians()));
            setFire(FIREPOWER);
        }
        out.println("FIRING BULLET WITH POWER: " + FIREPOWER);
    }

    private double limit(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    private State getCurrentState() {
        // Get our robot's position and heading
        // Get our robot's position and heading
        double ourX = getX();
        double ourY = getY();
        double ourHeading = getHeading();
        double ourGunHeading = getGunHeading();
        double ourRadarHeading = getRadarHeading();
        double distRemaining = getDistanceRemaining();
        double ourVelocity = getVelocity();
        double ourEnergy = getEnergy();
        double enemyCount = getOthers();

        ArrayList<EnemyInfo> enemyList = new ArrayList<EnemyInfo>();
        // Get the enemy's bearing and distance from our robot
        for (ScannedRobotEvent enemyInfo : enemies.values()) {
            enemyList.add(new EnemyInfo(enemyInfo.getBearing(), enemyInfo.getDistance(), enemyInfo.getHeading(),
                    enemyInfo.getVelocity(), enemyInfo.getEnergy()));
        }
        while (enemyList.size() < max_enemies) {
            enemyList.add(new EnemyInfo(0.0, 0.0, 0.0, 0.0, 0.0));
        }
        double bulletHeading = this.bulletHeading;
        double bulletBearing = this.bulletBearing;
        double bulletPower = this.bulletPower;
        double bulletVelocity = this.bulletVelocity;

        // Return a new State object with these values
        return new State(enemyList, ourX, ourY, ourHeading, ourGunHeading, ourRadarHeading, distRemaining, ourVelocity,
                ourEnergy, enemyCount, bulletHeading, bulletBearing, bulletPower, bulletVelocity);
    }

    public void onHitWall(HitWallEvent e) {
        hitWallPen = -50;
        // moveDirection = -moveDirection;
    }

    public void onHitRobot(HitRobotEvent e) {
        if (getEnergy() > 50.0) {
            if (e.getEnergy() == 0) {
                hitEnemyPen = 40;
            } else if (e.getEnergy() < 15) {
                hitEnemyPen = 15;
            } else if (e.getEnergy() < 50) {
                hitEnemyPen = -10;
            } else {
                hitEnemyPen = -25;
            }
        } else {
            if (e.getEnergy() == 0 && getEnergy() > 25.0) {
                hitEnemyPen = 20;
            } else {
                hitEnemyPen = -35;
            }
        }
    }

    public void onRoundEnded(RoundEndedEvent e) {
        // RobocodeRunner.mainNetwork.saveWeights("weights", out);
        // out.println("Weights saved");

        current_enemies = max_enemies;
    }

    public void onBattleEnded(BattleEndedEvent e) {
        out.println(e.getResults().getScore());
    }

    public void onHitByBullet(HitByBulletEvent e) {
        this.bulletBearing = e.getBearing();
        this.bulletHeading = e.getHeading();
        this.bulletVelocity = e.getVelocity();
        this.bulletPower = e.getPower();

        if (this.bulletPower > 2) {
            hitByBullet = -50;
        } else {
            hitByBullet = -25;
        }
    }

    public void onBulletMissed(BulletMissedEvent e) {
        bulletMissedPen = -5;
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        bulletHitBulletPen = 15;
    }

    public void onBulletHit(BulletHitEvent e) {
        if (e.getBullet().getPower() >= 2) {
            bulletHitPen = 40;
        } else {
            bulletHitPen = 20;
        }
        if (e.getEnergy() <= 0) {
            bulletHitPen = 70;
        }
    }

    public void onRobotDeathEvent(RobotDeathEvent e) {
        robotDeathPen = 5;
        enemies.remove(e.getName());
        current_enemies--;
    }

    public void onStatus(StatusEvent e) {
        if (RobocodeRunner.CURRENT_EPISODE % TARGET_UPDATE_FREQ == 0) {
            out.println("#############################################");
            out.println("#### COPYING WEIGHTS TO TARGET NETWORK ####");
            out.println("#############################################");
            RobocodeRunner.mainNetwork.copyWeights(RobocodeRunner.targetNetwork);
        }

        lastState = getCurrentState();

        double energy = e.getStatus().getEnergy();
        int enemy_count = e.getStatus().getOthers();
        int enemies_dead = max_enemies - enemy_count;

        if (lastEnergy > energy) {
            currentReward += -15;
        } else if (lastEnergy < energy) {
            currentReward -= 15;
        } else {
            currentReward += 5;
        }

        if (hitWallPen != 0.0) {
            currentReward += hitWallPen;
            hitWallPen = 0.0;
        }
        if (hitEnemyPen != 0.0) {
            currentReward += hitEnemyPen;
            hitEnemyPen = 0.0;
        }
        if (bulletHitBulletPen != 0.0) {
            currentReward += bulletHitBulletPen;
            bulletHitBulletPen = 0.0;
        }
        if (hitByBullet != 0.0) {
            currentReward += hitByBullet;
            hitByBullet = 0.0;
        }
        if (bulletMissedPen != 0.0) {
            currentReward += bulletMissedPen;
            bulletMissedPen = 0.0;
        }
        if (scannedRobotPen != 0.0) {
            currentReward += scannedRobotPen;
            scannedRobotPen = 0.0;
        }
        if (bulletHitPen != 0.0) {
            currentReward += bulletHitPen;
            bulletHitPen = 0.0;
        }
        if (robotDeathPen != 0.0) {
            currentReward += robotDeathPen;
            robotDeathPen = 0.0;
        }

        if (energy > 0 && enemies_dead > 0) {
            currentReward += 15;
        } else if (energy > 0 && enemies_dead > 1) {
            currentReward += 30;
        }

        if ((this.getX() > WIDTH - THRESHOLD) || (this.getX() < THRESHOLD) || (this.getY() > HEIGHT - THRESHOLD)
                || (this.getY() < THRESHOLD)) {
            // out.println("We have reached the threshold");
            currentReward += -5;
            if (this.getDistanceRemaining() < THRESHOLD) {
                // out.println("We are moving towards the wall.");
                currentReward += -10;
            } else {
                currentReward += 5;
            }
        }

        out.println("LAST STATE: " + stringifyField(lastState.toArray()));
        lastQValues = RobocodeRunner.mainNetwork.execute(lastState.toArray());
        out.println("LAST Q VALUES: " + stringifyField(lastQValues));

        //executeAction();

        out.println("ACTION: " + lastAction);
        out.println("REWARD: " + lastReward);
        // out.println("NUMBER OF WEIGHTS: " +
        // RobocodeRunner.mainNetwork.getNumOfWeights());

        currentState = getCurrentState();
        out.println("CURRENT STATE: " + stringifyField(currentState.toArray()));
        currentQValues = RobocodeRunner.targetNetwork.execute(currentState.toArray());
        out.println("TARGET Q VALUES: " + stringifyField(currentQValues));
        double error = 0.0;
        out.println("SIZE OF MEMORY: " + RobocodeRunner.memory.size());
        if (RobocodeRunner.memory.size() < BATCH_SIZE) {
            // out.println("USING SINGLE INPUT");
            double maxQ = getMaxQValue(currentQValues);
            lastQValues[lastAction] = lastQValues[lastAction]
                    + 0.1 * (lastReward / 10 + GAMMA * maxQ - lastQValues[lastAction]);
            error = RobocodeRunner.mainNetwork.backPropagate(lastState.toArray(), lastQValues);
            out.println("UPDATED Q VALUES: " + stringifyField(lastQValues));

        }

        else {
            out.println("USING BATCH INPUT");
            ArrayList<Sample> trainingSet = getSamples(BATCH_SIZE);
            double[][] lastStates = new double[trainingSet.size()][NUM_OF_INPUTS];
            double[][] currentStates = new double[trainingSet.size()][NUM_OF_INPUTS];
            int[] actions = new int[trainingSet.size()];
            double[] rewards = new double[trainingSet.size()];
            //
            double[][] lastQs = new double[trainingSet.size()][NUM_OF_OUTPUTS];
            double[][] currentQs = new double[trainingSet.size()][NUM_OF_OUTPUTS];
            double[] maxQs = new double[trainingSet.size()];
            //
            for (int i = 0; i < trainingSet.size(); i++) {
                lastStates[i] = trainingSet.get(i).getLastState().toArray();
                currentStates[i] = trainingSet.get(i).getCurrentState().toArray();
                actions[i] = trainingSet.get(i).getAction();
                rewards[i] = trainingSet.get(i).getReward();
            }

            lastQs = RobocodeRunner.mainNetwork.executeBatch(lastStates);
            currentQs = RobocodeRunner.targetNetwork.executeBatch(currentStates);
            for (int i = 0; i < trainingSet.size(); i++) {
                maxQs[i] = getMaxQValue(currentQs[i]);
                //
                lastQs[i][actions[i]] = Math
                        .tanh(lastQs[i][actions[i]] + 0.1 * (rewards[i] + GAMMA * maxQs[i] - lastQs[i][actions[i]]));
                out.println("UPDATED Q VALUES: " + stringifyField(lastQs[i]));
            }
            error = RobocodeRunner.mainNetwork.batchBackPropagate(lastStates, lastQs);
        }

        out.println("HUBER LOSS: " + error);
        
        total_round_loss += error;
        cnt += 1;
        
        if (RobocodeRunner.memory.size() >= MEMORY_SIZE) {
            RobocodeRunner.memory.remove(0);
        }
        RobocodeRunner.memory.add(new Sample(lastState, lastAction, lastReward / 10, currentState));

        RobocodeRunner.CURRENT_EPISODE++;
        lastEnergy = energy;
        lastState = currentState;
        lastAction = currentAction;
        lastReward = currentReward;
        currentReward = 0.0;
    }

    /**
     * onWin: Do a victory dance
     */
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}