package sample;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;


public class X_GPbot_2_169 extends AdvancedRobot {

private double moveDirection = 1.0;
private final static int width = 800;
private final static int height = 600;
private final static double threshold = 50.0;

public void run() {
		setColors(Color.red,Color.green,Color.blue);
		setAdjustGunForRobotTurn(true);
		
		while (this.getScannedRobotEvents().size() == 0){
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}

	public void onStatus(StatusEvent e)
	{
		if ((this.getX() > width - threshold) || (this.getX() < threshold) || (this.getY() > height - threshold) || (this.getY() < threshold)) {
				out.println("We have reached the threshold");

				if (this.getDistanceRemaining() < threshold) {
					
					out.println("We are moving towards the wall.");
					this.setAhead(threshold * -2);
				}
			} 
	}

	public void onScannedRobot(ScannedRobotEvent e) {

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
 	while((++deltaTime) * (20.0 - 3.0 * 3.0) < 
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
 	// --- PHENOME 1 ---
		setFire((((Math.sin(((((Math.abs(((e.getHeadingRadians()) > 0 ? (e.getVelocity()) : ((Math.min((Math.random()*2 - 1), (e.getVelocity()))) / (Math.floor((Math.random()*10))))))) * (Math.min((Math.max(((Math.random()*2 - 1) * (Math.max((getGunTurnRemainingRadians()), (0.3075964993333914)))), ((Math.toDegrees((getWidth()))) / ((getRadarTurnRemainingRadians()) - (Math.random()*2 - 1))))), (Math.tan((Math.PI)))))) - ((0.0001) > 0 ? (((Math.acos((getHeading()))) / (getVelocity())) * ((getGunTurnRemainingRadians()) / (e.getDistance()))) : (Math.tan((Math.floor((Math.random()*10))))))) - (((getHeadingRadians()) * ((Math.acos(((getEnergy()) * -1))) > 0 ? (Math.min((Math.min(((getGunHeadingRadians()) > 0 ? (e.getVelocity()) : (getY())), (getGunTurnRemainingRadians()))), (getHeight()))) : (getEnergy()))) - ((((((Math.floor((Math.random()*10))) / (Math.PI)) + ((Math.random()) + (e.getDistance()))) == (Math.max((Math.PI), ((e.getVelocity()) - (getHeading())))) ? ((0.0001) - ((0.1533886167374584) + (getHeading()))) : (getVelocity())) / (e.getHeadingRadians())) / (e.getHeadingRadians())))))) > ((Math.abs(((0.0969977546273465) * -1))) + (Math.max((Math.cos((getHeight()))), (Math.floor((Math.random()*10)))))) ? (Math.toRadians((Math.max(((Math.max((Math.floor((Math.random()*10))), (((Math.random()*2 - 1) == ((Math.PI) / (0.32867609952490173)) ? ((Math.cos((Math.PI))) - (Math.min((Math.random()), (Math.floor((Math.random()*10)))))) : (Math.max((Math.PI), (Math.max((e.getVelocity()), (Math.random()*2 - 1)))))) * (((getRadarHeadingRadians()) > (Math.min((0.9575687335807079), (getGunHeadingRadians()))) ? (getY()) : (Math.abs((getGunHeadingRadians())))) - (Math.floor((Math.random()*10))))))) > (getRadarTurnRemainingRadians()) ? ((Math.max(((getGunTurnRemainingRadians()) * -1), (getRadarHeadingRadians()))) + (((((getHeight()) > 0 ? (Math.floor((Math.random()*10))) : (0.0001)) * (Math.toDegrees((Math.floor((Math.random()*10)))))) + (Math.floor((Math.random()*10)))) * (getVelocity()))) : ((Math.random()*2 - 1) > (0.0001) ? (Math.max((Math.toRadians((((e.getBearingRadians()) == (Math.floor((Math.random()*10))) ? (getHeading()) : (getY())) - ((getWidth()) > (0.0001) ? (getRadarTurnRemainingRadians()) : (0.6200850985696764))))), ((((Math.random()*2 - 1) - (e.getHeadingRadians())) - (Math.max((getGunHeadingRadians()), (Math.random()*2 - 1)))) > (Math.random()*2 - 1) ? ((Math.floor((Math.random()*10))) - ((getWidth()) > 0 ? (0.0001) : (getGunTurnRemainingRadians()))) : ((Math.floor((Math.random()*10))) * ((e.getVelocity()) - (Math.floor((Math.random()*10)))))))) : ((Math.random()) / (e.getHeadingRadians())))), (Math.max((getGunTurnRemainingRadians()), ((getY()) > 0 ? (Math.toDegrees((0.9966724796013953))) : (Math.max(((0.0001) == ((Math.max((0.0001), (getGunHeadingRadians()))) - ((Math.random()*2 - 1) > (e.getHeadingRadians()) ? (Math.floor((Math.random()*10))) : (Math.random()))) ? (0.0001) : (Math.max((Math.acos((Math.random()))), (Math.random()*2 - 1)))), (((0.7400698121929788) + ((getY()) * (0.9062704264712566))) / (e.getBearingRadians()))))))))))) : ((getX()) * (getRadarHeadingRadians()))) + (((Math.random()*2 - 1) > (Math.random()*2 - 1) ? ((Math.PI) + (((Math.max((getHeading()), (Math.random()))) > ((e.getBearingRadians()) > 0 ? ((getY()) == (((0.0001) + (getRadarHeadingRadians())) / ((e.getVelocity()) + (0.0001))) ? (getHeight()) : (Math.tan(((getVelocity()) * (getX()))))) : (Math.abs((((getHeading()) > (Math.random()) ? (e.getVelocity()) : (Math.random())) / (0.4972082035135241))))) ? ((Math.random()) * (Math.max((Math.PI), (Math.floor((Math.random()*10)))))) : ((Math.max((Math.PI), (Math.min(((Math.random()*2 - 1) / (getHeading())), ((Math.random()*2 - 1) > (e.getDistance()) ? (Math.PI) : (Math.random()*2 - 1)))))) - (0.0001))) * -1)) : ((((0.11713356309572698) + (Math.floor((Math.random()*10)))) * (Math.random()*2 - 1)) + (getHeading()))) == ((Math.max((Math.min((0.770698358239267), (0.0001))), ((Math.random()) == (Math.random()*2 - 1) ? (Math.asin((Math.toDegrees((Math.tan((((getY()) - (getGunHeadingRadians())) * (Math.atan((Math.random()*2 - 1)))))))))) : (((Math.max((getGunHeadingRadians()), (getRadarHeadingRadians()))) / (Math.random()*2 - 1)) + (((getEnergy()) == (Math.min((getHeadingRadians()), ((getGunTurnRemainingRadians()) == (Math.random()) ? (0.0001) : (getEnergy())))) ? (Math.abs(((e.getVelocity()) / (Math.random())))) : (Math.min(((e.getVelocity()) == (getWidth()) ? (getEnergy()) : (0.0001)), (Math.min((Math.floor((Math.random()*10))), (e.getBearingRadians())))))) * (Math.min((Math.max(((getY()) * (Math.PI)), ((Math.PI) + (0.0001)))), (((Math.floor((Math.random()*10))) - (getHeading())) / (Math.floor((Math.random()*10))))))))))) + (Math.max(((Math.max(((getY()) > ((Math.toRadians(((Math.random()*2 - 1) == (getRadarHeadingRadians()) ? (getX()) : (e.getBearingRadians())))) - ((Math.sin((0.0001))) / ((getRadarHeadingRadians()) == (Math.random()) ? (e.getBearingRadians()) : (Math.random()*2 - 1)))) ? (getWidth()) : (getHeading())), ((Math.max((((Math.random()*2 - 1) > (getX()) ? (Math.random()) : (Math.random()*2 - 1)) + ((Math.floor((Math.random()*10))) * (getGunTurnRemainingRadians()))), (Math.abs((getGunHeadingRadians()))))) == (Math.random()) ? (Math.PI) : (((0.6581991210477112) + (getGunTurnRemainingRadians())) / (Math.random()))))) - ((getHeight()) - (0.6039176757476712))), ((Math.floor((Math.random()*10))) == (getVelocity()) ? ((((Math.atan(((e.getDistance()) * (getX())))) - (((getRadarHeadingRadians()) * (e.getDistance())) / (Math.max((Math.random()), (Math.floor((Math.random()*10))))))) * ((Math.asin((e.getVelocity()))) + ((Math.min((e.getDistance()), (getWidth()))) > ((0.29209551823705493) > 0 ? (getGunTurnRemainingRadians()) : (e.getDistance())) ? ((Math.floor((Math.random()*10))) / (getHeight())) : (Math.asin((0.5001879352444828)))))) + (0.6264822301300004)) : ((Math.floor((Math.random()*10))) > 0 ? (0.0001) : ((((Math.floor((Math.random()*10))) + ((e.getVelocity()) == (0.7500294228502735) ? (getGunHeadingRadians()) : (getEnergy()))) > (((e.getBearingRadians()) - (Math.random())) - (getHeadingRadians())) ? ((Math.toDegrees((Math.floor((Math.random()*10))))) > (Math.PI) ? (Math.max((getGunHeadingRadians()), (e.getDistance()))) : (Math.toDegrees((0.7594845902767958)))) : ((Math.max((getRadarHeadingRadians()), (getX()))) - ((Math.floor((Math.random()*10))) == (Math.random()) ? (0.7517325142778227) : (getGunHeadingRadians())))) + (getRadarTurnRemainingRadians()))))))) ? (Math.min(((0.14157418046343828) - (getRadarTurnRemainingRadians())), (e.getHeadingRadians()))) : ((Math.abs((Math.toRadians((Math.asin((Math.max((Math.random()*2 - 1), ((Math.min((0.9702541524270647), (e.getDistance()))) > (getY()) ? (((getX()) - (Math.random()*2 - 1)) == (Math.toDegrees((Math.PI))) ? (getRadarHeadingRadians()) : (Math.PI)) : (((0.6465341480104029) * (getEnergy())) * -1)))))))))) - (Math.sin((Math.min((Math.min(((Math.max((0.5056728577343234), ((Math.toRadians((getX()))) / (Math.min((e.getVelocity()), (e.getBearingRadians())))))) > ((Math.toRadians((Math.random()*2 - 1))) - (e.getVelocity())) ? ((e.getBearingRadians()) - (Math.min((getVelocity()), ((Math.floor((Math.random()*10))) > 0 ? (0.0001) : (Math.floor((Math.random()*10))))))) : (getHeight())), ((((Math.floor((Math.random()*10))) + (e.getHeadingRadians())) * (Math.PI)) * (Math.min((Math.max((Math.max((getRadarTurnRemainingRadians()), (e.getDistance()))), ((Math.random()*2 - 1) - (Math.random())))), (0.0001)))))), (((((Math.acos((Math.floor((Math.random()*10))))) + ((getHeading()) / (getVelocity()))) > 0 ? ((Math.abs((getEnergy()))) + (Math.floor((Math.random()*10)))) : ((Math.max((Math.floor((Math.random()*10))), (getGunTurnRemainingRadians()))) / ((Math.PI) / (Math.PI)))) * (Math.cos((((getRadarHeadingRadians()) * (getEnergy())) / ((0.35056952370645933) > 0 ? (0.013373105900998383) : (getRadarHeadingRadians())))))) / ((Math.min((getY()), (Math.sin(((0.27562165537382777) * (getRadarTurnRemainingRadians())))))) + ((getY()) / (getVelocity()))))))))))));

 	// --- PHENOME 2 ---
 	setAhead((e.getDistance() - (Math.min((Math.max((((Math.cos(((Math.atan((getWidth()))) / (Math.min((((getHeading()) / ((getX()) + (0.9246468604062812))) / ((getEnergy()) + (0.0001))), (Math.tan((Math.max((Math.atan((getHeading()))), (Math.asin((getWidth())))))))))))) - (getGunTurnRemainingRadians())) > 0 ? ((Math.toRadians((((0.0001) * ((Math.sin(((getRadarTurnRemainingRadians()) / (getRadarTurnRemainingRadians())))) > 0 ? (Math.random()*2 - 1) : ((Math.max((getWidth()), (getVelocity()))) == ((e.getBearingRadians()) / (Math.PI)) ? (getGunTurnRemainingRadians()) : (Math.abs((getY())))))) - (getRadarTurnRemainingRadians())))) + ((Math.max(((Math.max(((Math.min((getRadarTurnRemainingRadians()), (0.0001))) + (Math.min((e.getVelocity()), (e.getVelocity())))), ((e.getDistance()) + ((e.getHeadingRadians()) / (0.0001))))) - (Math.asin((Math.min((Math.max((getGunTurnRemainingRadians()), (0.9549541893934053))), (Math.max((Math.random()), (Math.PI)))))))), (e.getDistance()))) / (Math.PI))) : (Math.floor((Math.random()*10)))), ((Math.PI) > (Math.min((Math.acos(((getHeading()) + (getVelocity())))), (Math.random()))) ? (Math.min((getGunHeadingRadians()), (Math.asin(((Math.PI) * (((Math.min((getRadarTurnRemainingRadians()), (Math.max((e.getVelocity()), (Math.random()))))) * (Math.abs((e.getHeadingRadians())))) / ((getWidth()) == (getVelocity()) ? (Math.random()) : ((Math.max((getGunTurnRemainingRadians()), (Math.floor((Math.random()*10))))) * ((getGunHeadingRadians()) - (Math.floor((Math.random()*10)))))))))))) : ((0.0001) == (Math.cos((Math.max((((0.0001) * -1) / (e.getVelocity())), (Math.toRadians((Math.floor((Math.random()*10))))))))) ? ((getGunHeadingRadians()) > (Math.min((((Math.tan(((Math.floor((Math.random()*10))) - (getHeight())))) * -1) * (((Math.atan((getHeading()))) > 0 ? (e.getDistance()) : ((Math.floor((Math.random()*10))) + (getHeading()))) + (Math.min((0.9267134265352734), (Math.random()*2 - 1))))), (getHeading()))) ? (((Math.random()*2 - 1) + ((getWidth()) * (0.0001))) > 0 ? ((((getGunHeadingRadians()) / (Math.toRadians((getRadarHeadingRadians())))) / ((e.getDistance()) * (Math.min((getY()), (e.getVelocity()))))) / (e.getDistance())) : (((Math.max(((Math.random()) / (getRadarTurnRemainingRadians())), (Math.floor((Math.random()*10))))) > 0 ? ((Math.max((getHeading()), (0.0001))) / (getRadarHeadingRadians())) : (Math.PI)) / ((Math.min((getEnergy()), ((getRadarHeadingRadians()) + (0.0001)))) - (Math.min((getEnergy()), (Math.random()*2 - 1)))))) : (Math.min((getHeadingRadians()), (Math.asin(((Math.toRadians(((0.31633211337278366) - (0.0001)))) - (e.getDistance()))))))) : (Math.max((Math.toDegrees(((Math.max(((0.09935543083130638) > (Math.min((e.getBearingRadians()), (getGunTurnRemainingRadians()))) ? (Math.max((Math.random()), (e.getHeadingRadians()))) : ((0.9420668141534717) + (getGunTurnRemainingRadians()))), (Math.cos((e.getHeadingRadians()))))) > ((e.getHeadingRadians()) - ((Math.abs((getHeight()))) / (Math.min((e.getHeadingRadians()), (Math.random()*2 - 1))))) ? (Math.max(((getGunTurnRemainingRadians()) / ((getHeading()) == (getGunHeadingRadians()) ? (0.0001) : (Math.PI))), ((Math.PI) > (getHeadingRadians()) ? (Math.max((getHeight()), (getEnergy()))) : (Math.floor((Math.random()*10)))))) : ((getGunHeadingRadians()) > 0 ? (((0.6999176874327382) - (getHeading())) * ((e.getBearingRadians()) / (0.0001))) : (getGunHeadingRadians()))))), (Math.abs((((((getRadarHeadingRadians()) > (getHeading()) ? (getEnergy()) : (0.0001)) > 0 ? (getGunTurnRemainingRadians()) : ((0.0001) / (e.getBearingRadians()))) - (Math.floor((Math.random()*10)))) + ((getWidth()) == (getHeading()) ? (0.17071169105315742) : ((e.getHeadingRadians()) - (getVelocity())))))))))))), (Math.max((Math.min((((getRadarTurnRemainingRadians()) * -1) * (Math.tan((getY())))), ((((getRadarHeadingRadians()) * (((Math.random()) / ((Math.random()*2 - 1) == ((Math.random()) > (Math.floor((Math.random()*10))) ? (e.getBearingRadians()) : (Math.floor((Math.random()*10)))) ? (Math.PI) : ((Math.random()*2 - 1) - (getVelocity())))) + (e.getHeadingRadians()))) * (getRadarTurnRemainingRadians())) + (getVelocity())))), (Math.min((((((Math.tan((0.6910634176439724))) * -1) - (((Math.abs((Math.PI))) / (((getEnergy()) > 0 ? (getHeight()) : (Math.random()*2 - 1)) > (Math.floor((Math.random()*10))) ? ((e.getVelocity()) + (Math.floor((Math.random()*10)))) : (getHeadingRadians()))) - ((Math.floor((Math.random()*10))) / (0.023782151914116922)))) + ((((e.getVelocity()) == ((Math.PI) > ((0.0001) / (0.0001)) ? ((getGunHeadingRadians()) > (e.getDistance()) ? (Math.floor((Math.random()*10))) : (Math.random())) : (e.getVelocity())) ? ((getRadarHeadingRadians()) + ((getY()) * (Math.floor((Math.random()*10))))) : (((Math.random()) * (getRadarTurnRemainingRadians())) == (Math.min((0.0001), (getGunTurnRemainingRadians()))) ? (getGunHeadingRadians()) : ((e.getBearingRadians()) > (Math.random()*2 - 1) ? (getEnergy()) : (0.6792869585351345)))) * (((Math.min((Math.random()), (Math.floor((Math.random()*10))))) / (Math.max((0.08683522666071108), (e.getDistance())))) * -1)) + (((Math.max(((getY()) + (0.06079886014057323)), (0.0001))) * (Math.toDegrees((0.9063845454390707)))) + (0.0001)))) / ((e.getHeadingRadians()) / (e.getDistance()))), (getRadarHeadingRadians()))))))))*moveDirection);

 	// --- PHENOME 3 ---
		setTurnRightRadians((Math.min((((((0.0001) - (getGunTurnRemainingRadians())) - ((Math.random()) * (Math.random()))) + (((((Math.acos(((Math.random()*2 - 1) / (Math.random())))) - ((Math.toDegrees(((0.35632555337382477) == (Math.random()) ? (getX()) : (e.getDistance())))) + (Math.min((getRadarTurnRemainingRadians()), (Math.PI))))) - (Math.random()*2 - 1)) - (Math.random()*2 - 1)) == (getRadarTurnRemainingRadians()) ? ((Math.random()) == (getEnergy()) ? (Math.random()*2 - 1) : (getGunTurnRemainingRadians())) : (getHeadingRadians()))) * ((Math.min((Math.min((Math.min((Math.max((0.5482152335883951), ((Math.min(((Math.random()*2 - 1) / (e.getBearingRadians())), ((getRadarTurnRemainingRadians()) == (getVelocity()) ? (getVelocity()) : (0.0001)))) * (Math.max(((e.getHeadingRadians()) > (getHeading()) ? (getHeadingRadians()) : (Math.floor((Math.random()*10)))), ((e.getHeadingRadians()) > (0.0001) ? (Math.random()*2 - 1) : (0.9485402596318999))))))), (getRadarTurnRemainingRadians()))), (((0.8520449959745594) * -1) / (((getX()) * (getY())) == (((Math.random()*2 - 1) + (Math.atan((0.10054811711284228)))) / (getHeadingRadians())) ? (Math.min((Math.PI), (0.0001))) : (((Math.min((Math.random()), (0.8998271081420732))) - (e.getBearingRadians())) > (getX()) ? (Math.abs((getX()))) : (getRadarHeadingRadians())))))), (Math.max(((Math.min(((e.getBearingRadians()) / ((getHeadingRadians()) > (0.6213821834608961) ? (0.44286533096458747) : (Math.random()))), (e.getBearingRadians()))) + (0.6183686642316266)), (((getGunHeadingRadians()) / (0.9486925338366068)) > 0 ? ((Math.acos((Math.abs(((getRadarTurnRemainingRadians()) + (Math.PI)))))) / ((Math.max((Math.min((Math.PI), (getGunTurnRemainingRadians()))), ((Math.random()) * (Math.floor((Math.random()*10)))))) * -1)) : (Math.min((Math.tan((((getX()) / (0.0001)) - (Math.min((getRadarHeadingRadians()), (0.0001)))))), (e.getVelocity())))))))) == (Math.random()*2 - 1) ? (Math.asin((Math.min((Math.asin((Math.max(((((0.13511692583065005) / (Math.random()*2 - 1)) + (Math.floor((Math.random()*10)))) - ((Math.min((getGunTurnRemainingRadians()), (Math.PI))) - (Math.PI))), (0.9435840198896798))))), ((0.0001) == (Math.max(((Math.tan((Math.max((e.getHeadingRadians()), (Math.random()))))) > (0.16272071356320517) ? (Math.min(((getHeight()) + (Math.PI)), ((e.getDistance()) * (getHeadingRadians())))) : (Math.max((e.getBearingRadians()), ((Math.PI) * (getHeight()))))), ((Math.min(((getWidth()) - (e.getBearingRadians())), ((getGunTurnRemainingRadians()) == (e.getBearingRadians()) ? (getWidth()) : (0.31952338649196854)))) * (Math.acos((getX())))))) ? (((getY()) - (Math.min(((0.0001) + (getGunHeadingRadians())), ((getVelocity()) > 0 ? (Math.PI) : (getRadarTurnRemainingRadians()))))) - ((Math.max((Math.random()*2 - 1), (Math.sin((getHeadingRadians()))))) * ((getRadarTurnRemainingRadians()) + ((getX()) + (getRadarHeadingRadians()))))) : (((getWidth()) / (((getHeading()) + (getHeight())) + ((e.getVelocity()) + (e.getDistance())))) * (Math.abs(((e.getHeadingRadians()) == (getHeight()) ? (Math.atan((e.getVelocity()))) : ((getVelocity()) * (Math.random()))))))))))) : ((getGunTurnRemainingRadians()) + (((Math.random()) / ((Math.max((Math.max((Math.floor((Math.random()*10))), ((0.5337590121870149) / (Math.random()*2 - 1)))), (Math.PI))) == (Math.random()*2 - 1) ? (Math.asin(((Math.floor((Math.random()*10))) * (Math.max((Math.random()), (e.getDistance())))))) : ((Math.floor((Math.random()*10))) / ((Math.abs((Math.random()*2 - 1))) == (getVelocity()) ? (0.09856797074381907) : (Math.random()*2 - 1))))) + ((Math.random()*2 - 1) + ((Math.max((Math.PI), (getRadarHeadingRadians()))) - ((((Math.random()*2 - 1) == (getHeight()) ? (0.6546980317685577) : (getGunHeadingRadians())) / (Math.random())) / (getGunHeadingRadians())))))))), (Math.min(((getX()) * -1), ((Math.random()) + (Math.atan((Math.tan(((getHeading()) - (getGunHeadingRadians()))))))))))));
	}

public void onHitByBullet(HitByBulletEvent e) {
		setTurnRight(90 - e.getBearing());
		setAhead(Math.floor((Math.random()*10))*(Math.pow(e.getPower(),2)*e.getVelocity())*(Math.random()*2-1));
	}

/* 
public void onHitRobot(HitRobotEvent e) {
		// If he's in front of us, set back up a bit.
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			back(100);
		} // else he's in back of us, so set ahead a bit.
		else {
			ahead(100);
		}
	}
*/

public void onHitWall(HitWallEvent e) {
		moveDirection=-moveDirection;
}
}