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
    private double scannedRobots;
    private double enemyCount;
    private double enemyX;
    private double enemyY;
    private double enemyHeading;
    private double enemyVelocity;
    private double enemyEnergy;
    private double bulletHeading;
    private double bulletBearing;
    private double bulletPower;
    private double bulletVelocity;

    public State(double x, double y, double heading, double velocity, double energy, double radarHeading, double enemyCount, double scannedRobots,
     double enemyBearing, double enemyDistance, double enemyX, double enemyY, double enemyHeading, double enemyVelocity, double enemyEnergy, 
     double bulletHeading, double bulletBearing, double bulletPower, double bulletVelocity) {
        this.x = Math.rint(x);
        this.y = Math.rint(y);
        this.heading = Math.rint(heading);
        this.velocity = Math.rint(velocity);
        this.energy = Math.rint(energy);
        this.enemyBearing = Math.rint(enemyBearing);
        this.enemyDistance = Math.rint(enemyDistance);
        //this.gunHeat = Math.rint(gunHeat);
        //this.gunHeading = Math.rint(gunHeading);
        this.radarHeading = Math.rint(radarHeading);
        this.scannedRobots = scannedRobots;
        this.enemyCount = Math.rint(enemyCount);
        this.enemyX = Math.rint(enemyX);
        this.enemyY = Math.rint(enemyY);
        this.enemyHeading = Math.rint(enemyHeading);
        this.enemyVelocity = Math.rint(enemyVelocity);
        this.enemyEnergy = Math.rint(enemyEnergy);
        this.bulletHeading = Math.rint(bulletHeading);
        this.bulletBearing = Math.rint(bulletBearing);
        this.bulletVelocity = Math.rint(bulletVelocity);
        this.bulletPower = Math.rint(bulletPower);
    }

    public double[] toArray() {
        return new double[]{x, y, heading, velocity, energy, radarHeading, enemyCount, scannedRobots, enemyBearing, enemyDistance, enemyX, enemyY,
            enemyHeading, enemyVelocity, enemyEnergy, bulletHeading, bulletBearing, bulletPower, bulletVelocity};
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}
