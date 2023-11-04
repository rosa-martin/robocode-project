package sample;

public class State {
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
