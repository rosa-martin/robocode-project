package sample;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.evo.NEAT.Environment;
import com.evo.NEAT.Genome;
import com.evo.NEAT.Pool;
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
import robocode.control.events.BattleStartedEvent;

public class NeatRobot extends AdvancedRobot {

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
	private static float robotHeading = 0.0f;
	private static float robotVelocity = 0.0f;
	private static float robotGunHeading = 0.0f;
	private static float robotGunHeat = 0.0f;
	private static float enemiesRemaining = 6.0f;
	private static float robotDistanceRemaining = 0.0f;
	private static float robotRadarHeading = 0.0f;
	private static float robotX = 0.0f;
	private static float robotY = 0.0f;
	private static float robotEnergy = 0.0f;

    private static int currentReward = 0;

	private static double lastEnergy;

	private static Pool pool;
	private static Genome topGenome;
	private NEAT neat;
	private static int generation = 0;

	private static float[] inputs;
	private static float[] outputs;

	public void run(){
		for (;;){
			pool.evaluateFitness(neat);
			topGenome = pool.getTopGenome();
			//float[] actions = topGenome.evaluateNetwork(getInputs());
			this.doAction(outputs);
		}
	}

	public static int getReward() {
		return currentReward;
	}

	public static float[] getInputs()
	{
		float absoluteBearing = robotHeading + robotGunHeading;
		return new float[] {(float) robotEnergy, (float) robotX, (float) robotY, (float) robotHeading, absoluteBearing, (float) robotGunHeading, 
		(float) robotGunHeat, (float) robotRadarHeading, (float) robotVelocity, eSpottedX, eSpottedY, eBearing, eHeading, eVelocity, eAbsBearing, 
		eDistance, eBulletBearing, eBulletHeading, eBulletPower, eBulletVelocity, eHitX, eHitY, eEnergy, bulletHitPower, bulletHitVelocity, 
		bulletHitHeading, (float) enemiesRemaining, (float) robotDistanceRemaining}; //28 inputs
	}

	public void onBattleStarted(BattleStartedEvent event){
		neat = new NEAT();
		pool = new Pool();
		pool.initializePool();
		topGenome = new Genome();
	}

	public static void setOutputs(float[] values)
	{
		outputs = values;
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

		out.println("Top fitness: " + topGenome.getPoints());
		out.println("Generation: " + generation);

		execute();
	}

	public void onStatus(StatusEvent e) {
		double energy = e.getStatus().getEnergy();
		int enemy_count = e.getStatus().getOthers();
		int max_enemies = 6;
		int enemies_dead = max_enemies - enemy_count;
		
		// init stuff
		robotHeading = (float) this.getHeading();
		robotVelocity = (float) this.getVelocity();
		robotGunHeading = (float) this.getGunHeading();
		robotGunHeat = (float) this.getGunHeat();
		enemiesRemaining = (float) this.getOthers();
		robotDistanceRemaining = (float) this.getDistanceRemaining();
		robotRadarHeading = (float) this.getRadarHeading();
		robotX = (float) this.getX();
		robotY = (float) this.getY();
		robotEnergy = (float) this.getEnergy();

		if (energy > 0 && enemies_dead > 0) {
			currentReward += 15;
		} else if (energy > 0 && enemies_dead > 1) {
			currentReward += 30;
		} else if (energy > 0 && enemies_dead > 2) {
			currentReward += 60;
		} else if (energy > 0 && enemies_dead > 3) {
			currentReward += 120;
		} else if (energy > 0 && enemies_dead > 4) {
			currentReward += 240;
		} else if (energy > 0 && enemies_dead > 5) {
			currentReward += 480;
		}
		if (lastEnergy > this.getEnergy()) {
			currentReward += -20;
		}
	}

    public void onHitWall(HitWallEvent e) {

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

		currentReward += punishment;
    }
    
    public void onHitRobot(HitRobotEvent e) {
    	currentReward += -25;
    }
    
    public void onBulletHitBullet(BulletHitBulletEvent e) {
    	currentReward += 15;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	currentReward += -70;
		eBulletBearing = (float) e.getBearing();
		eBulletHeading = (float) e.getHeading();
		eBulletPower = (float) e.getPower();
		eBulletVelocity = (float) e.getVelocity();
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
    	currentReward += -15;
    }
    
    public void onBulletHit(BulletHitEvent e) {
		bulletHitHeading = (float) e.getBullet().getHeading();
		bulletHitPower = (float) e.getBullet().getPower();
		eEnergy = (float) e.getEnergy();
		bulletHitVelocity = (float) e.getBullet().getVelocity();
		eHitX = (float) e.getBullet().getX();
		eHitY = (float) e.getBullet().getY();
    	currentReward += 50;
    }
    
	public void onScannedRobot(ScannedRobotEvent e) {	
		currentReward += 15;

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
		currentReward = 0;
		generation++;
		pool.breedNewGeneration();
		topGenome.writeTofile();
	}
    
}
