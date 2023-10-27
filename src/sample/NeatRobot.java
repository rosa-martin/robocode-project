package sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.evo.NEAT.Environment;
import com.evo.NEAT.Genome;
import com.evo.NEAT.com.evo.NEAT.config.NEAT_Config;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class NeatRobot extends AdvancedRobot implements Environment {

    // Parameters
    private int wasHitByEnemy = 0;
	private int hitWall = 0;
	private int wasHitByBullet = 0;
	private int spotEnemy = 0;
	private int hitEnemy = 0;
	private int missEnemy = 0;
    
    // Inputs
    private static float eSpottedX = 0.0f;
	private static float eSpottedY = 0.0f;
	private static float eHitX = 0.0f;
	private static float eHitY = 0.0f;
	private static float eDistance = 0.0f;
	private static float eBearing = 0.0f;
	private static float eHeading = 0.0f;
	private static float eVelocity = 0.0f;
	private static float eAbsBearing = 0.0f;
	private static float eEnergy = 0.0f;
	private static float bulletHitPower = 0.0f;
	private static float bulletHitVelocity = 0.0f;
	private static float bulletHitHeading = 0.0f;
	private static float eBulletBearing = 0.0f;
	private static float eBulletHeading = 0.0f;
	private static float eBulletVelocity = 0.0f;
	private static float eBulletPower = 0.0f;

    private static double currentReward = 0;
	private static double lastReward = 0;

	private static int battles = 1;
	private static double alpha = 0.1; // Learning rate
    private static double gamma = 0.5; // Eagerness - 0 looks in the near future, 1 looks in the distant future

	private static HashMap<float[], ArrayList<Float>> q_map = new HashMap<float[], ArrayList<Float>>();
	private static float[] lastState;		// field with 16 states
	private static float[] currentState;
	private static float[] currentAction;
	private static float[] lastAction;
	private static double lastEnergy;

	// PENALIZATION
	private static int hitWallPen = 0;
	private static int hitByBullet = 0;
	private static int hitEnemyPen = 0;
	private static int bulletHitBulletPen = 0;
	private static int bulletMissedPen = 0;
	private static int bulletHitPen = 0;
	private static int enemyScannedPen = 0;

    private void setGlobalsToZero(){
        this.wasHitByEnemy = 0;
        this.wasHitByBullet = 0;
        this.hitWall = 0;
        this.spotEnemy = 0;
        this.hitEnemy = 0;
        this.missEnemy = 0;
    }

    @Override
    public void evaluateFitness(ArrayList<Genome> population) {
        
        for(Genome gene : population){
            float fitness = (float) 0.0;
            gene.setFitness(fitness);
            for(int i = 0; i < NEAT_Config.INPUTS; i++){
                // Predict next move
                double absoluteBearing = this.getHeadingRadians() + this.getGunHeadingRadians();

				float inputs[] = {wasHitByBullet, wasHitByEnemy, hitWall, spotEnemy, hitEnemy, missEnemy, (float) this.getEnergy(), (float) this.getX(), (float) this.getY(),
					(float) this.getHeading(), (float) absoluteBearing, (float) this.getGunHeading(), (float) this.getGunHeat(), (float) this.getRadarHeading(), (float) this.getVelocity(), eSpottedX, eSpottedY, eBearing,
					eHeading, eVelocity, eAbsBearing, eDistance, eBulletBearing, eBulletHeading, eBulletPower, eBulletVelocity, eHitX, eHitY, eEnergy, bulletHitPower, 
					bulletHitVelocity, bulletHitHeading, (float) this.getOthers(), (float) this.getDistanceRemaining()};
                    
                    float outputs[] = gene.evaluateNetwork(inputs);
                    lastState = inputs;

                    this.doAction(outputs);
				    lastAction = outputs;

                    fitness = compareQ(lastState, currentState);
                    this.setGlobalsToZero();
            }
            gene.setFitness(fitness);
        } 
    }

    public ArrayList<Float> calcQ(float[] lastState, float[] currentState) {
		if (q_map.containsKey(lastState)) {
			ArrayList<Float> qs = q_map.get(lastState);
			ArrayList<Float> maxQs = q_map.get(currentState);
			ArrayList<Float> newQs = new ArrayList<Float>();

			for (int i = 0; i < qs.size(); i++) {
				newQs.add((float) ((1 - alpha) * qs.get(i) + alpha * (lastReward + gamma * maxQs.get(i))));
			}

			out.println("State: " + lastState);
			out.println("Reward: " + lastReward);
			out.println("Q-value: " + qs);
			out.println("New Q-value: " + newQs);

			return newQs;
		} else {
			return new ArrayList<Float>(Collections.nCopies(9, 0.0f)); // Return zeros if the state is not present
		}
	}

	public ArrayList<Float> getQ(float[] lastState) {
		if (q_map.containsKey(lastState)) {
			return q_map.get(lastState);
		} else {
			return new ArrayList<Float>(Collections.nCopies(9, 0.0f)); // Return zeros if the state is not present
		}
	}
	
	public float compareQ(float[] lastState, float[] currentState) {
		ArrayList<Float> currentQs = getQ(lastState);
		ArrayList<Float> predQs = calcQ(lastState, currentState);

		float sum_current = 0.0f;
		float sum_pred = 0.0f;

		// Are the new generated actions better in terms of the q values
		for (int i=0; i<currentQs.size(); i++) {
			sum_current += currentQs.get(i);
			sum_pred += predQs.get(i);
		}

		if (sum_current < sum_pred) {
			q_map.put(lastState, predQs);
			return sum_pred;
		}
		else{
			return sum_current;
		}
	}

	public void doAction(float[] values) {

		setAhead(values[0]);
		out.println("Go ahead by " + values[0] + " points.");

		setTurnRight(values[1]);
		out.println("Turn right by " + values[1] + " °.");

		setTurnLeft(values[2]);
		out.println("Turn left by " + values[2] + " °.");

		setBack(values[3]);
		out.println("Go back by " + values[3] + " points.");

		setTurnGunRight(values[4]);
		out.println("Turn gun right by " + values[4] + "°.");

		setTurnGunLeft(values[5]);
		out.println("Turn gun left by " + values[5] + "°.");

		setTurnRadarRight(values[6]);
		out.println("Turn radar right by " + values[6] + "°.");

		setTurnRadarLeft(values[7]);
		out.println("Turn radar left by " + values[7] + "°.");

		setFire(values[8]);
		out.println("Fire with power " + values[8] + ".");

		execute();
	}

	public void onStatus(StatusEvent e) {

		double energy = e.getStatus().getEnergy();
		int enemy_count = e.getStatus().getOthers();
		int max_enemies = 6;
		int enemies_dead = max_enemies - enemy_count;

		if (energy > 0 && enemies_dead > 0)
		{
			currentReward += 15;
		}
		else if (energy > 0 && enemies_dead > 1)
		{
			currentReward += 30;
		}
		else if (energy > 0 && enemies_dead > 2)
		{
			currentReward += 60;
		}
		else if (energy > 0 && enemies_dead > 3)
		{
			currentReward += 120;
		}
		else if (energy > 0 && enemies_dead > 4)
		{
			currentReward += 240;
		}
		else if (energy > 0 && enemies_dead > 5)
		{
			currentReward += 480;
		}

		if(lastEnergy > this.getEnergy())
		{
			currentReward += - 20;
		}

		if (hitWallPen != 0) {
			currentReward += hitWallPen;
			hitWallPen = 0;
		}
		if (hitEnemyPen != 0) {
			currentReward += hitEnemyPen;
			hitEnemyPen = 0;
		}
		if (bulletHitBulletPen != 0) {
			currentReward += bulletHitBulletPen;
			bulletHitBulletPen = 0;
		}
		if (enemyScannedPen != 0) {
			currentReward += enemyScannedPen;
			enemyScannedPen = 0;
		}
		if (bulletMissedPen != 0) {
			currentReward += bulletMissedPen;
			bulletMissedPen = 0;
		}
		if (bulletHitPen != 0) {
			currentReward += bulletHitPen;
			bulletHitPen = 0;
		}
		if (hitByBullet != 0) {
			currentReward += hitByBullet;
			hitByBullet = 0;
		}

		lastEnergy = energy;
		lastState = currentState;
		lastAction = currentAction;
		lastReward = currentReward;
		currentReward = 0.0;
	}

    public void onHitWall(HitWallEvent e) {
		hitWall = 1;
    	double vel =  this.getVelocity();
		
		int punishment = 0;

		if (this.getEnergy() < lastEnergy)				// If received damage
		{
			punishment += -15;
		}
    	if (this.getDistanceRemaining()>0 && vel>0)		// If heading towards the wall and is not standing still
    	{
    		punishment += -50;
    	}

		hitWallPen = punishment;
    }
    
    public void onHitRobot(HitRobotEvent e) {
		wasHitByEnemy = 1;
    	hitEnemyPen = -25;
    }
    
    public void onBulletHitBullet(BulletHitBulletEvent e) {
    	bulletHitBulletPen = 15;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	hitByBullet = -70;
		eBulletBearing = (float) e.getBearing();
		eBulletHeading = (float) e.getHeading();
		eBulletPower = (float) e.getPower();
		eBulletVelocity = (float) e.getVelocity();
		wasHitByBullet = 1;
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
    	bulletMissedPen = -15;
		missEnemy = 1;
    }
    
    public void onBulletHit(BulletHitEvent e) {
		bulletHitHeading = (float) e.getBullet().getHeading();
		bulletHitPower = (float) e.getBullet().getPower();
		eEnergy = (float) e.getEnergy();
		bulletHitVelocity = (float) e.getBullet().getVelocity();
		eHitX = (float) e.getBullet().getX();
		eHitY = (float) e.getBullet().getY();
    	bulletHitPen = 50;
		hitEnemy = 1;
    }
    
	public void onScannedRobot(ScannedRobotEvent e) {	
		angle = (int) Math.round((e.getBearing()+180)/10);
		dist = (int) Math.round(e.getDistance()/10);

		spotEnemy = 1;
		enemyScannedPen = 15;

		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		eAbsBearing = (float) absoluteBearing;
		eBearing = (float) e.getBearing();
		eDistance = (float) e.getDistance();
		double enemyX = myX + eDistance * Math.sin(absoluteBearing);
		eSpottedX = (float) enemyX;
		double enemyY = myY + eDistance * Math.cos(absoluteBearing);
		eSpottedY = (float) enemyY;
		eHeading = (float) e.getHeading();
		double enemyVelocity = e.getVelocity();
		eVelocity = (float) enemyVelocity;
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		battles++;
	}
    
}
