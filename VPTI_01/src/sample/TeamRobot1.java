package sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.ScannedRobotEvent;

public class TeamRobot1 extends AdvancedRobot {
    private double reward = 0;
    private HashMap<String, Double> qTable = new HashMap<>();
    private String state = "start";
    private String action = "none";
    private double epsilon = 0.1;
    private String[] actions = {"move_forward", "move_backward", "turn_left", "turn_right", "turn_gun_right", "turn_gun_left", "turn_radar_right", "turn_radar_left", "fire", "barrage"};
    private String file_path = "D:\\Robocode\\VPTI_01\\VPTI_01\\qTable.ser";
    
    public void run() {
        while (true) {
            executeAction(action);
            double maxQ = maxQ(state);
            double q = qTable.getOrDefault(state + "_" + action, 0.0);
            double newQ = q + 0.1 * (reward + 0.9 * maxQ - q);
            qTable.put(state + "_" + action, newQ);
            action = selectAction(state);
            reward = 0;
        }
    }

    public String selectAction(String state) {
        if (Math.random() < epsilon) { // Exploration
            return actions[(int) (Math.random() * actions.length)];
        } else { // Exploitation
            return actions[argMaxQ(state)];
        }
    }
    
    public void executeAction(String action) {
        switch (action) {
            case "move_forward":
                setAhead(50);
                break;
            case "move_backward":
                setBack(25);
                break;
            case "turn_left":
                setTurnLeft(90);
                break;
            case "turn_right":
                setTurnRight(90);
                break;
            case "turn_gun_right":
                setTurnGunRight(90);
                break;
            case "turn_gun_left":
                setTurnGunLeft(90);
                break;
            case "turn_radar_right":
                setTurnRadarRight(90);
                break;
            case "turn_radar_left":
                setTurnRadarLeft(90);
                break;
            case "fire":
                fire(1);
                break;
            case "barrage":
                fire(5);
                break;
        }
        execute();
    }
    
    public double maxQ(String state) {
        double max = Double.NEGATIVE_INFINITY;
        for (String action : actions) {
            String key = state + "_" + action;
            if (qTable.containsKey(key)) {
                double value = qTable.get(key);
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    public int argMaxQ(String state) {
        int bestActionIndex = 0;
        double maxQ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < actions.length; i++) {
            String key = state + "_" + actions[i];
            if (qTable.containsKey(key)) {
                double value = qTable.get(key);
                if (value > maxQ) {
                    maxQ = value;
                    bestActionIndex = i;
                }
            }
        }
        return bestActionIndex;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        reward += 20;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        reward -= 20;
        turnLeft(90 - e.getBearing());
    }
    
    public void onHitWall(HitWallEvent e) {
        reward -= 50;
    }
    
    public void onHitRobot(HitRobotEvent e) {
        reward -= 10;
    }
    
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        reward += 5;
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
    	reward -= 15;
    }
    
    public void onBulletHit(BulletHitEvent e) {
    	reward += 35;
    }
    /*
    public void onRoundEnded(RoundEndedEvent e) {
    	System.out.println("A round has ended. Saving the Q-Table...");
    	saveQTable();
    }
    
    public void onRoundStarted(RoundEndedEvent e) {
    	System.out.println("A round has started. Loading the Q-Table...");
    	loadQTable();
    }
    */
    public void saveQTable() {
        try {
            RobocodeFileOutputStream fos = new RobocodeFileOutputStream(getDataFile(file_path));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(qTable);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
	public void loadQTable() {
        try {
            FileInputStream fis = new FileInputStream(getDataFile(file_path).getAbsolutePath());
            ObjectInputStream ois = new ObjectInputStream(fis);
            qTable = (HashMap<String, Double>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
    }
}