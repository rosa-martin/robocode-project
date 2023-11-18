package sample;

public class EnemyInfo {
    double enemyBearing;
    double enemyDistance;
    double enemyHeading;
    double enemyVelocity;
    double enemyEnergy;

    public double getEnemyBearing() {
        return enemyBearing;
    }

    public void setEnemyBearing(double enemyBearing) {
        this.enemyBearing = enemyBearing;
    }

    public double getEnemyDistance() {
        return enemyDistance;
    }

    public void setEnemyDistance(double enemyDistance) {
        this.enemyDistance = enemyDistance;
    }

    public double getEnemyHeading() {
        return enemyHeading;
    }

    public void setEnemyHeading(double enemyHeading) {
        this.enemyHeading = enemyHeading;
    }

    public double getEnemyVelocity() {
        return enemyVelocity;
    }

    public void setEnemyVelocity(double enemyVelocity) {
        this.enemyVelocity = enemyVelocity;
    }

    public double getEnemyEnergy() {
        return enemyEnergy;
    }

    public void setEnemyEnergy(double enemyEnergy) {
        this.enemyEnergy = enemyEnergy;
    }

    public EnemyInfo(double enemyBearing, double enemyDistance, double enemyHeading, double enemyVelocity, double enemyEnergy) {
        this.enemyBearing = Math.rint(enemyBearing);
        this.enemyDistance = Math.rint(enemyDistance);
        this.enemyHeading = Math.rint(enemyHeading);
        this.enemyVelocity = Math.rint(enemyVelocity);
        this.enemyEnergy = Math.rint(enemyEnergy);
    }

    public double[] toArray() {
        return new double[]{enemyBearing, enemyDistance, enemyHeading, enemyVelocity, enemyEnergy};
    }

    @Override
    public String toString() {
        return QLearningRobot.stringifyField(this.toArray());
    }
}
