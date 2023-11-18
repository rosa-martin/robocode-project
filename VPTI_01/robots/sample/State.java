package sample;

import java.util.ArrayList;
import java.util.Arrays;

public class State {
    private double x; // The robot's x position
    private double y; // The robot's y position
    private double heading; // The robot's heading in degrees
    private double gunHeading; // The heading of the gun in degrees
    private double radarHeading;
    private double distRemaining;
    private double velocity; // The robot's velocity
    private double energy; // The robot's energy
    private double enemyCount;
    private ArrayList<Double> enemyBearings = new ArrayList<Double>();
    private ArrayList<Double> enemyDistances = new ArrayList<Double>();
    private ArrayList<Double> enemyHeadings = new ArrayList<Double>();
    private ArrayList<Double> enemyVelocities = new ArrayList<Double>();
    private ArrayList<Double> enemyEnergies = new ArrayList<Double>();
    private double bulletHeading;
    private double bulletBearing;
    private double bulletPower;
    private double bulletVelocity;

    public State(ArrayList<EnemyInfo> enemyList, double x, double y, double heading, double gunHeading, double radarHeading, double distRemaining, 
    double velocity, double energy, double enemyCount, double bulletHeading, double bulletBearing, double bulletPower, double bulletVelocity) {
        this.x = Math.rint(x);
        this.y = Math.rint(y);
        this.heading = Math.rint(heading);
        this.gunHeading = Math.rint(gunHeading);
        this.radarHeading = Math.rint(radarHeading);
        this.distRemaining = Math.rint(distRemaining);
        this.velocity = Math.rint(velocity);
        this.energy = Math.rint(energy);
        this.enemyCount = Math.rint(enemyCount);
        for (EnemyInfo enemyInfo : enemyList){
            this.enemyBearings.add(enemyInfo.getEnemyBearing());
            this.enemyDistances.add(enemyInfo.getEnemyDistance());
            this.enemyHeadings.add(enemyInfo.getEnemyHeading());
            this.enemyVelocities.add(enemyInfo.getEnemyVelocity());
            this.enemyEnergies.add(enemyInfo.getEnemyEnergy());
        }
        this.bulletHeading = Math.rint(bulletHeading);
        this.bulletBearing = Math.rint(bulletBearing);
        this.bulletVelocity = Math.rint(bulletVelocity);
        this.bulletPower = Math.rint(bulletPower);
    }

    public double[] toArray() {
        ArrayList<Double> out = new ArrayList<Double>(Arrays.asList(new Double[]{x, y, heading, gunHeading, radarHeading, distRemaining, velocity, energy, enemyCount, bulletHeading, bulletBearing, bulletPower, bulletVelocity}));
        out.addAll(enemyBearings);
        out.addAll(enemyDistances);
        out.addAll(enemyHeadings);
        out.addAll(enemyVelocities);
        out.addAll(enemyEnergies);

        return out.stream().mapToDouble(d -> d).toArray();
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}
