package sample;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


import robocode.*;
import robocode.util.Utils;

public class TeamRobot4 extends AdvancedRobot{
	private static boolean exploring = false; //if True then the action is choosen randomly
	private static double currentReward = 0;
	private static double lastReward = 0;
	private static double epsilon = 1;
	private static double decayRate = 0.001; //0.00005 for 20000 games = 1
	private static double minEpsilon = 0.05;
	private static double maxEpsilon = 1;
	private static double battles = 1;
	private static double alpha = 0.1; // Learning rate
    private static double gamma = 0.5; // Eagerness - 0 looks in the near future, 1 looks in the distant future
	private static HashMap<String, ArrayList<Double>> q_map = new HashMap<String, ArrayList<Double>>();
	private static String lastState = null;
	private static String currentState = null;
	private static int currentAction = 0;
	private static int lastAction = 0;
	private static int lastEnergy;
	private static boolean afterFirstTurn = false;
	private static boolean afterSecondTurn = false;
	private static int dist; //(1000/10)
	private static int angle;  //(360/10)
	private static double hitWallPen = 0.0;
	private static double hitByBullet = 0.0;
	private static double hitEnemyPen = 0.0;
	private static double bulletHitBulletPen = 0.0;
	private static double bulletMissedPen = 0.0;
	private static double bulletHitPen = 0.0;
	private static double enemyScannedPen = 0.0;
	private static boolean doFire = false;
	private static int numFire = 0;
	
//	***********************************************
//	***********************************************
	private static boolean doSave = true;
	private static boolean doLoad = false;
//	***********************************************
//	***********************************************
	
	public void run() {
		if(doLoad) {
			loadQMap();
			epsilon = 0.1;
			
		};
		setTurnRadarRight(360);
		for(;;) {
			if (ThreadLocalRandom.current().nextDouble(0,1)<epsilon) {
				exploring = true;
			}else {
				exploring = false;
			};
			if(afterFirstTurn) {
				setAdjustGunForRobotTurn(true);
				doFire = true;
				doAction();
			}
		}
	}


	public void doAction() {
		switch (chooseAction()) {
		case 0:
			setAhead(50);
			currentAction = 0;
			out.println("Ahead.");
			break;
		case 1:
			setTurnRight(30);
			currentAction = 1;
			out.println("Turn right.");
			break;
		case 2:
			setTurnLeft(30);
			currentAction = 2;
			out.println("Turn left.");
			break;
		case 3:
			setBack(50);
			currentAction = 3;
			out.println("Go back.");
			break;
		case 4:
			setTurnGunRight(45);
			currentAction = 4;
			out.println("Turn Gun Right By 45°.");
			break;
		case 5:
			setTurnGunLeft(45);
			currentAction = 5;
			out.println("Turn Gun Left By 45°.");
			break;
		case 6:
			setTurnRadarRight(45);
			currentAction = 6;
			out.println("Turn Radar Right By 45°.");
			break;
		case 7:
			setTurnRadarLeft(45);
			currentAction = 7;
			out.println("Turn Radar Left By 45°.");
			break;
		case 8:
			/*
			if(getScannedRobotEvents().size()==0) {
				setTurnRadarRight(360);
			}
			*/
			currentAction = 8;
			out.println("Do nothing.");
			break;
		default:
			break;
		};
		execute();
	}
	
	public int chooseAction() {
		boolean isRandom;
		int resRand = new Random().nextInt(9); // num of avalible actions
		ArrayList<Double> q_values = new ArrayList<Double>();
		
		if(exploring) {isRandom=true;} else {isRandom=false;};	
		if(isRandom) {
			return resRand;
		}
		
		if(q_map.containsKey(currentState)) {
			q_values = q_map.get(currentState);
			if(zerosCheck(q_values)) {
				return resRand;
			};
			Double forIndex = Collections.max(q_values);
			return q_map.get(currentState).indexOf(forIndex);
		}
		else {
			return resRand;
		}
	}
	
	public static boolean zerosCheck(ArrayList<Double> arr) {
		for(Double d: arr){
			if(!d.equals(0.0))
				return false;
		}
		return true;
	}
	
	public void calcQ() {
		if(q_map.containsKey(lastState)) {	        
				double q = q_map.get(lastState).get(lastAction);
		        double maxQ = Collections.max(q_map.get(currentState));
		        double newQ = (1-alpha) *q + alpha * (lastReward + gamma * maxQ);
	            ArrayList<Double> last_q_values = q_map.get(lastState);
	            last_q_values.set(lastAction, newQ);
	            q_map.put(lastState, last_q_values);
	            
	            out.println("State: "+lastState);
	            out.println("Reward: "+lastReward);
	            out.println("Q-value: "+q);
	            out.println("New Q-value: "+newQ);

		}
	}
	
	public int getSector(double x, double y) { //return number of sector where tank is located --  size of sector is 50*40
		int cX = 1;
		int cY = 1;
		while(x>50) {
			x = x-50;
			cX++;
		};
		while(y>40) {
			y = y-40;
			cY++;
		}
		return cX*cY;
	}

    
	public void onStatus(StatusEvent e) {
		if(afterFirstTurn) {
			double X = e.getStatus().getX();
			double Y = e.getStatus().getY();
			int energy = (int) Math.round(e.getStatus().getEnergy());
			int heat = (int) Math.round(e.getStatus().getGunHeat());
			int sector = getSector(X,Y);
			
			currentState = sector+"-"+dist+"-"+angle;
			if(!q_map.containsKey(currentState)) {
				ArrayList<Double> tmp = new ArrayList<Double>();
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		        tmp.add(0.0);
		    	q_map.put(currentState, tmp);
			}
			
			if(afterSecondTurn) {
//				for inicialization of variables
				currentReward = getCurrentReward(energy, heat);
				if(hitWallPen!=0.0) {
					currentReward += hitWallPen;
					hitWallPen = 0.0;
				}
				if(hitEnemyPen!=0.0) {
					currentReward += hitEnemyPen;
					hitEnemyPen = 0.0;
				}
				if(bulletHitBulletPen!=0.0) {
					currentReward += bulletHitBulletPen;
					bulletHitBulletPen = 0.0;
				}
				if(enemyScannedPen!=0.0) {
					currentReward += enemyScannedPen;
					enemyScannedPen = 0.0;
				}
				if(bulletMissedPen!=0.0) {
					currentReward += bulletMissedPen;
					bulletMissedPen = 0.0;
				}
				if(bulletHitPen!=0.0) {
					currentReward += bulletHitPen;
					bulletHitPen = 0.0;
				}
				if(hitByBullet!=0.0) {
					currentReward += hitByBullet;
					hitByBullet = 0.0;
				}
				if(lastReward!=0.0) {
					calcQ();
				}
				if(!doLoad) {
					epsilon = minEpsilon + (maxEpsilon - minEpsilon)*Math.exp(-decayRate*battles);
				}
			}
		
			lastEnergy = energy;
			lastState = currentState;
			lastAction = currentAction;
			lastReward = currentReward;
			currentReward = 0.0;
			afterSecondTurn = true;
		}
		afterFirstTurn = true;
	}
    
    public double getCurrentReward(int energy, int heat) {
    	double result = 0.0;
    	int diffEnergy = energy-lastEnergy;
    	
    	if(diffEnergy>0.0) {
			result += 25.0;
		}

		if(diffEnergy < 0.0){
			result -=  25.0;
		}
   	
    	if(result==0.0) {
    		return 0.0;
    	}

    	return result;
    }

    public void onHitWall(HitWallEvent e) {
    	double vel =  this.getVelocity();
    	if (this.getDistanceRemaining()>0 && vel>0)
    	{
    		hitWallPen = -100.0;
    	}
    }
    
    public void onHitRobot(HitRobotEvent e) {
    	hitEnemyPen = -15.0;
    }
    
    public void onBulletHitBullet(BulletHitBulletEvent e) {
    	bulletHitBulletPen += 15;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
    	hitByBullet = -15.0;
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
    	bulletMissedPen -= 15;
    }
    
    public void onBulletHit(BulletHitEvent e) {
    	bulletHitPen += 35;
    }
    
	public void onScannedRobot(ScannedRobotEvent e) {	
		angle = (int) Math.round((e.getBearing()+180)/10);
		dist = (int) Math.round(e.getDistance()/10);
		double power = 3.0;
		
		enemyScannedPen += 15;
		
		if(getOthers()==1) {
			power = 2;
			if(dist<30&&dist>=15) {power = 2.5;};
			if(dist<15) {power = 3;};
		}

		
//		************************************************************
//		*******Source: https://robowiki.net/wiki/Linear_Targeting ****
		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = e.getHeadingRadians();
		double enemyVelocity = e.getVelocity();
		 
		 
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight(), 
		       battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while((++deltaTime) * (20.0 - 3.0 * power) < 
		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
			predictedX += Math.sin(enemyHeading) * enemyVelocity;	
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			if(	predictedX < 18.0 
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0){
				predictedX = Math.min(Math.max(18.0, predictedX), 
		                    battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), 
		                    battleFieldHeight - 18.0);
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(
		    predictedX - getX(), predictedY - getY()));
		 
		setTurnRadarRightRadians(
		    Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
//		***********************************************************
//		***********************************************************
		if(doFire) {
			fire(power);
			numFire++;
			if(numFire!=2) {
				scan();
			}
			numFire = 0;
			doFire = false;
			setTurnRadarRight(360);
		}

	}
	
	public void onBattleEnded(BattleEndedEvent e) {
		try {
			if(doSave) {
				saveFile();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		battles++;
	}

    public void saveFile() throws IOException
    {
    	PrintStream w = null;
		try {
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("q_map.dat")));
			for(Map.Entry<String, ArrayList<Double>> entry: q_map.entrySet()) {
				w.println(entry.getKey()+":"+entry.getValue());
			}

			if (w.checkError()) {
				out.println("I could not write the count!");
			}
		} catch (IOException e) {
			out.println("IOException trying to write: ");
			e.printStackTrace(out);
		} finally {
			if (w != null) {
				w.close();
			}
		}
    	
    }
    
    public void loadQMap() {

    	try {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(getDataFile("q_map.dat")));
				String[] line;
				String tmp = reader.readLine();
				while (tmp != null) {
					line = tmp.split(":");
					ArrayList<Double> tmpAr = new ArrayList<Double>();
					for(String s: line[1].replace("[", "").replace("]","").split(",")) {
						tmpAr.add(Double.valueOf(s));
					}
					q_map.put(line[0], tmpAr);
				    tmp = reader.readLine();
				}
				out.println("Size of map durring load -- "+q_map.size());
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (IOException e) {
    }
    }
    
}
