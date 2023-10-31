package sample;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import static robocode.util.Utils.normalRelativeAngleDegrees;

@SuppressWarnings("all")
public class RL_NN_Robot extends AdvancedRobot {
    private int actionCount = 8;

    private double qrl_x = 0;
    private double qrl_y = 0;
    private double qdistancetoenemy = 0;
    private double q_absbearing = 0;
    private double reward = 0;

    private int random_action = 0;
    private double[] q_possible = new double[actionCount];
    private double gunTurnAmt;
    private double getBearing;

	private double[] Ytrain = new double[1];
	private static int iter = 0;
    private double oldDistance, oldAngle, enemyDistance = 9999;

    private static double[][] w_hx = new double[19][6];
	private static double[][] w_yh = new double[1][19+1];
	private String[][] w_hxs = new String[19][6];
	private String[][] w_yhs = new String[1][19+1];

    public void run(){
        setColors(Color.PINK, Color.PINK, Color.PINK, Color.PINK, Color.PINK);
        Random rand = new Random();
        double[] Xtrain, Xtrain_next;

        final double alpha = 0.1;
        final double gamma = 0.9;
        boolean explore = true;
        boolean greedy = true;
        double q_present_double, q_next_double;

        for(;;){
            if(iter == 0) {
                setAdjustGunForRobotTurn(true);
                setAdjustRadarForGunTurn(true);

                try {
                    loadHiddenWeights();
                    loadOutputWeights();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                for(int i = 0; i < 19; ++i){
                    for(int j = 0; j < 6; ++j){
                        w_hx[i][j] = Double.valueOf(w_hxs[i][j]);
                    }
                }
                for(int i = 0; i < 1; ++i) {
                    for(int j = 0; j < 20; ++j) {
                        w_yh[i][j] = Double.valueOf(w_yhs[i][j]);
                    }
                }
                iter += 1;
            }

            if(explore) {
                random_action = rand.nextInt(actionCount) + 1;
                Xtrain = new double[]{qrl_x, qrl_y, qdistancetoenemy, q_absbearing, random_action, 1};
                q_present_double = NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, false);

                reward = 0;
                rl_action(random_action);
                getRewardChange();

                Xtrain_next = new double[]{qrl_x, qrl_y, qdistancetoenemy, q_absbearing, random_action, 1};
                q_next_double = NN.NNtrain(Xtrain_next, Ytrain, w_hx, w_yh, false);

                q_present_double = q_present_double + alpha *(reward+ gamma * q_next_double - q_present_double);
                Ytrain[0] = q_present_double;
                NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, true);
                saveHiddenWeights();
                saveOutputWeights();
            }

            if(greedy) {
                for(int j = 1; j <= actionCount; ++j) {
                    Xtrain = new double[]{qrl_x, qrl_y, qdistancetoenemy, q_absbearing, j, 1};
                    q_possible[j-1] = NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, false);
                }

                int qmax_action = getMax(q_possible) + 1;

                Xtrain = new double[]{qrl_x, qrl_y, qdistancetoenemy, q_absbearing, qmax_action, 1};
                q_present_double = NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, false);
                reward = 0;

                rl_action(qmax_action);
                getRewardChange();

                Xtrain_next = new double[]{qrl_x, qrl_y, qdistancetoenemy, q_absbearing, random_action, qmax_action};
                q_next_double = NN.NNtrain(Xtrain_next, Ytrain, w_hx, w_yh, false);

                q_present_double = q_present_double + alpha *(reward+ gamma * q_next_double - q_present_double);
                Ytrain[0] = q_present_double;
                NN.NNtrain(Xtrain, Ytrain, w_hx, w_yh, true);
            }
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        this.getBearing = e.getBearing();
        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));

        double distance = e.getDistance();
        qdistancetoenemy = quantize_distance(distance);
        if (distance != enemyDistance) {
            enemyDistance = distance;
        }

        if (getGunHeading() >= (getRadarHeading() - 10) && getGunHeading() <= (getRadarHeading() + 10)) {
            if (qdistancetoenemy <= 2.50) {
                fire(3);
            }
            if (qdistancetoenemy > 2.50 && qdistancetoenemy < 5.00) {
                fire(2);
            }
            if (qdistancetoenemy > 5.00 && qdistancetoenemy < 7.50) {
                fire(1);
            }
        }

        qrl_x = quantize_position(getX());
        qrl_y = quantize_position(getY());

        double angleToEnemy = e.getBearing();
        double angle = Math.toRadians((getHeading() + angleToEnemy % 360));
        double enemyX = (getX() + Math.sin(angle) * e.getDistance());
        double enemyY = (getY() + Math.cos(angle) * e.getDistance());
        double absbearing = absoluteBearing((float) getX(), (float) getY(), (float) enemyX, (float) enemyY);
        q_absbearing = quantize_angle(absbearing);

        widthLock(e);
	}

	private void widthLock(ScannedRobotEvent e){
        double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
        double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );
        double extraTurn = Math.min( Math.atan( 36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );

        if (radarTurn < 0)
            radarTurn -= extraTurn;
        else
            radarTurn += extraTurn;

        setTurnRadarRightRadians(radarTurn);
    }

    private void getRewardChange() {
        double change = 0;
        double tempAngle = Math.abs(getGunHeading() - getRadarHeading());

        if (oldAngle > tempAngle) {
            change += 1.5;
        } else if (oldDistance < enemyDistance) {
            change += 0.3;
        } else {
            change -= 0.3;
        }

        if (getGunHeading() < getRadarHeading() + 10 && getGunHeading() > getRadarHeading() - 10) {
            change += 3;
        }

        oldAngle = tempAngle;
        oldDistance = enemyDistance;
        reward += change;
    }

    public void onWallHit(HitWallEvent event) {reward-=3;}
    public void onHitRobot(HitRobotEvent event) {reward-=2;}

    private double quantize_angle(double absbearing) {
        q_absbearing = Math.ceil(absbearing/90);
        return absbearing / 90;
    }

    private double quantize_distance(double distance2) {
        qdistancetoenemy = distance2 / 100;
        return qdistancetoenemy;
    }

    private double absoluteBearing(float x1, float y1, float x2, float y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Math.hypot(x1 - x2, y1 - y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) {
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) {
            bearing = 360 + arcSin;
        } else if (yo < 0) {
            bearing = 180 - arcSin;
        }
        return bearing;
    }

    private double quantize_position(double rl_x2) {
		qrl_x = rl_x2 == 0 ? 0 : Math.ceil(rl_x2 / 100);
	    return rl_x2 / 100;
	}

    @Override
    public void onStatus(StatusEvent e) {
        if (getRadarTurnRemaining() == 0.0) {
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    protected void rl_action(int x) {
        System.out.println(x);
        switch(x){
            case 1:
                int moveDirection =+ 1;
                setTurnRight(getBearing + 90);
                setAhead(150 * moveDirection);
                break;

            case 2:
                int moveDirection1 =- 1;
                setTurnRight(getBearing + 90);
                setAhead(150 * moveDirection1);
                break;

            case 3:
                turnGunRight(gunTurnAmt);
                turnRight(getBearing-25);
                ahead(150);
                break;

            case 4:
                turnGunRight(gunTurnAmt);
                turnRight(getBearing - 25);
                back(150);
                break;

            case 5:
                turnGunRight(5);
                break;

            case 6:
                turnGunLeft(5);
                break;

            case 7:
                turnGunRight(40);
                break;

            case 8:
                turnGunLeft(40);
                break;
        }
    }

    private void saveHiddenWeights() {
        PrintStream w1 = null;
        try {
            w1 = new PrintStream(new RobocodeFileOutputStream(getDataFile("weights_hidden.txt")));
            for (double[] aW_hx : w_hx) {
                w1.println(aW_hx[0] + "    " + aW_hx[1] + "    " + aW_hx[2] + "    " +
                        aW_hx[3] + "    " + aW_hx[4] + "    " + aW_hx[5]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            assert w1 != null;
            w1.flush();
            w1.close();
        }
    }

    private void saveOutputWeights() {
        PrintStream w2 = null;
        try {
            w2 = new PrintStream(new RobocodeFileOutputStream(getDataFile("weights_output.txt")));
            w2.println(w_yh[0][0] + "    " + w_yh[0][1] + "    " + w_yh[0][2] + "    " + w_yh[0][3] + "    " +
                    w_yh[0][4] + "    " + w_yh[0][5] + "    " + w_yh[0][6] + "    " + w_yh[0][7] + "    " +
                    w_yh[0][8] + "    " + w_yh[0][9] + "    " + w_yh[0][10] + "    " + w_yh[0][11] + "    " +
                    w_yh[0][12] + "    " + w_yh[0][13] + "    " + w_yh[0][14] + "    " + w_yh[0][15] + "    " +
                    w_yh[0][16] + "    " + w_yh[0][17] + "    " + w_yh[0][18] + "    " + w_yh[0][19]);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            assert w2 != null;
            w2.flush();
            w2.close();
        }
    }

    private void loadHiddenWeights() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getDataFile("weights_hidden.txt")));
        String line = reader.readLine();
        try {
            int zz=0;
            while (line != null) {
                String splitLine[] = line.split(" {4}");
                System.arraycopy(splitLine, 0, w_hxs[zz], 0, 6);
                zz += 1;
                line= reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            reader.close();
        }
    }

    private void loadOutputWeights() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getDataFile("weights_output.txt")))) {
            String line;
            int zz = 0;
            do {
                line = reader.readLine();
                String splitLine[] = line.split(" {4}");
                System.arraycopy(splitLine, 0, w_yhs[zz], 0, 20);

                zz = 0;
                line = reader.readLine();
            } while (line != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getMax(double[] array) {
        double largest = array[0];
        int index = 0;
        for (int i = 1; i < array.length; ++i) {
            if (array[i] >= largest) {
                largest = array[i];
                index = i;
            }
        }
        return index;
    }
}
