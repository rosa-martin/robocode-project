package sample;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.transfer.RectifiedLinear;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.core.transfer.Linear;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.comp.neuron.BiasNeuron;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import robocode.*;
import robocode.util.Utils;

public class TeamRobot5 extends AdvancedRobot{
	private static boolean exploring = false; //if True then the action is chosen randomly
	private static double currentReward = 0;
	private static double lastReward = 0;

	private static double epsilon = 1;
	private static double decayRate = 0.001; //0.00005 for 20000 games = 1
	private static double minEpsilon = 0.05;
	private static double maxEpsilon = 1;

	private static int battles = 1;
	private static double alpha = 0.1; // Learning rate
    private static double gamma = 0.5; // Eagerness - 0 looks in the near future, 1 looks in the distant future

	private static HashMap<double[], ArrayList<Double>> q_map = new HashMap<double[], ArrayList<Double>>();
	private static double[] lastState;		// field with 16 states
	private static double[] currentState;
	private static double[] currentAction;
	private static double[] lastAction;
	private static double lastEnergy;
	private static boolean afterFirstTurn = false;
	private static boolean afterSecondTurn = false;
	private static int dist; //(1000/10)
	private static int angle;  //(360/10)

	// PENALIZATION
	private static double hitWallPen = 0.0;
	private static double hitByBullet = 0.0;
	private static double hitEnemyPen = 0.0;
	private static double bulletHitBulletPen = 0.0;
	private static double bulletMissedPen = 0.0;
	private static double bulletHitPen = 0.0;
	private static double enemyScannedPen = 0.0;

	Network network;
	File networkFile;
	private static String dataFile = "NN_weights";
	
	// INPUT PARAMETERS
	private static double wasHitByEnemy = 0.0;
	private static double wasHitByWall = 0.0;
	private static double wasHitByBullet = 0.0;
	private static double spotEnemy = 0.0;
	private static double hitEnemy = 0.0;
	private static double missEnemy = 0.0;

	private static double eSpottedX = 0.0;
	private static double eSpottedY = 0.0;
	private static double eHitX = 0.0;
	private static double eHitY = 0.0;
	private static double eDistance = 0.0;
	private static double eBearing = 0.0;
	private static double eHeading = 0.0;
	private static double eVelocity = 0.0;
	private static double eAbsBearing = 0.0;
	private static double eEnergy = 0.0;
	private static double bulletHitPower = 0.0;
	private static double bulletHitVelocity = 0.0;
	private static double bulletHitHeading = 0.0;
	private static double eBulletBearing = 0.0;
	private static double eBulletHeading = 0.0;
	private static double eBulletVelocity = 0.0;
	private static double eBulletPower = 0.0;

	private static boolean doFire = false;
	private static int numFire = 0;
	
//	***********************************************
//	***********************************************
	private static boolean doSave = true;
	private static boolean doLoad = false;
//	***********************************************
//	***********************************************
	
	public void run() {
		this.network = new Network();
		this.networkFile = this.getDataFile(dataFile+"_"+battles+".hdf5");
		this.network.downloadNetwork(this.networkFile);

		for(;;) {
			if (this.getTime() % 10 == 0 && this.getEnergy() > 0) {
				
				double absoluteBearing = this.getHeadingRadians() + this.getGunHeadingRadians();
				
				// Predict next move
				double[] inputs = {wasHitByBullet, wasHitByEnemy, wasHitByWall, spotEnemy, hitEnemy, missEnemy, this.getEnergy(), this.getX(), this.getY(),
					this.getHeading(), absoluteBearing, this.getGunHeading(), this.getGunHeat(), this.getRadarHeading(), this.getVelocity(), eSpottedX, eSpottedY, eBearing,
					eHeading, eVelocity, eAbsBearing, eDistance, eBulletBearing, eBulletHeading, eBulletPower, eBulletVelocity, eHitX, eHitY, eEnergy, bulletHitPower, 
					bulletHitVelocity, bulletHitHeading, (double) this.getOthers(), this.getDistanceRemaining()};	// remove reward -- it is included in the loss
				double[] outputs = this.network.evaluate(inputs);
				// Perform the predicted action
				this.doAction(outputs);
				
				lastState = inputs;
				lastAction = outputs;

				// Compare Q
				compareQ(lastState, currentState);
				
				// Reset
				wasHitByBullet = 0.0;
				wasHitByEnemy = 0.0;
				wasHitByWall = 0.0;
				spotEnemy = 0.0;
				hitEnemy = 0.0;
				missEnemy = 0.0;
			}
		}
	}

	public ArrayList<Double> calcQ(double[] lastState, double[] currentState) {
		if (q_map.containsKey(lastState)) {
			ArrayList<Double> qs = q_map.get(lastState);
			ArrayList<Double> maxQs = q_map.get(currentState);
			ArrayList<Double> newQs = new ArrayList<Double>();

			for (int i = 0; i < qs.size(); i++) {
				newQs.add((1 - alpha) * qs.get(i) + alpha * (lastReward + gamma * maxQs.get(i)));
			}

			out.println("State: " + lastState);
			out.println("Reward: " + lastReward);
			out.println("Q-value: " + qs);
			out.println("New Q-value: " + newQs);

			return newQs;
		} else {
			return new ArrayList<Double>(Collections.nCopies(9, 0.0)); // Return zeros if the state is not present
		}
	}

	public ArrayList<Double> getQ(double[] lastState) {
		if (q_map.containsKey(lastState)) {
			return q_map.get(lastState);
		} else {
			return new ArrayList<Double>(Collections.nCopies(9, 0.0)); // Return zeros if the state is not present
		}
	}
	
	public double compareQ(double[] lastState, double[] currentState) {
		ArrayList<Double> currentQs = getQ(lastState);
		ArrayList<Double> predQs = calcQ(lastState, currentState);

		double sum_current = 0.0;
		double sum_pred = 0.0;

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

	public void doAction(double[] values) {

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
		if (enemyScannedPen != 0.0) {
			currentReward += enemyScannedPen;
			enemyScannedPen = 0.0;
		}
		if (bulletMissedPen != 0.0) {
			currentReward += bulletMissedPen;
			bulletMissedPen = 0.0;
		}
		if (bulletHitPen != 0.0) {
			currentReward += bulletHitPen;
			bulletHitPen = 0.0;
		}
		if (hitByBullet != 0.0) {
			currentReward += hitByBullet;
			hitByBullet = 0.0;
		}

		lastEnergy = energy;
		lastState = currentState;
		lastAction = currentAction;
		lastReward = currentReward;
		currentReward = 0.0;
	}

    public void onHitWall(HitWallEvent e) {
		wasHitByWall = 1.0;
    	double vel =  this.getVelocity();
		
		double punishment = 0.0;

		if (this.getEnergy() < lastEnergy)				// If received damage
		{
			punishment += -15.0;
		}
    	if (this.getDistanceRemaining()>0 && vel>0)		// If heading towards the wall and is not standing still
    	{
    		punishment += -50.0;
    	}

		hitWallPen = punishment;
    }
    
    public void onHitRobot(HitRobotEvent e) {
		wasHitByEnemy = 1.0;
    	hitEnemyPen = -25.0;
    }
    
    public void onBulletHitBullet(BulletHitBulletEvent e) {
    	bulletHitBulletPen = 15;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	hitByBullet = -70.0;
		eBulletBearing = e.getBearing();
		eBulletHeading = e.getHeading();
		eBulletPower = e.getPower();
		eBulletVelocity = e.getVelocity();
		wasHitByBullet = 1.0;
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
    	bulletMissedPen = -15.0;
		missEnemy = 1.0;
    }
    
    public void onBulletHit(BulletHitEvent e) {
		bulletHitHeading = e.getBullet().getHeading();
		bulletHitPower = e.getBullet().getPower();
		eEnergy = e.getEnergy();
		bulletHitVelocity = e.getBullet().getVelocity();
		eHitX = e.getBullet().getX();
		eHitY = e.getBullet().getY();
    	bulletHitPen = 50.0;
		hitEnemy = 1.0;
    }
    
	public void onScannedRobot(ScannedRobotEvent e) {	
		angle = (int) Math.round((e.getBearing()+180)/10);
		dist = (int) Math.round(e.getDistance()/10);
		double power = 3.0;
		spotEnemy = 1.0;
		enemyScannedPen = 15.0;
		
		if(getOthers()==1) {
			power = 2;
			if(dist<30&&dist>=15) {power = 2.5;};
			if(dist<15) {power = 3;};
		}

		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		eAbsBearing = absoluteBearing;
		eBearing = e.getBearing();
		eDistance = e.getDistance();
		double enemyX = myX + eDistance * Math.sin(absoluteBearing);
		eSpottedX = enemyX;
		double enemyY = myY + eDistance * Math.cos(absoluteBearing);
		eSpottedY = enemyY;
		double enemyHeading = e.getHeadingRadians();
		eHeading = e.getHeading();
		double enemyVelocity = e.getVelocity();
		eVelocity = enemyVelocity;
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		battles++;
	} 
}
