package sample;

import java.util.ArrayList;

import com.evo.NEAT.Environment;
import com.evo.NEAT.Genome;
import com.evo.NEAT.com.evo.NEAT.config.NEAT_Config;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class NeatRobot extends AdvancedRobot implements Environment {

    private int wasHit = 0;
    private int wasHitByEnemy = 0;
    private int hitWall = 0;
    private int enemySpotted = 0;
    private int hitEnemy = 0;
    private int missEnemy = 0;
    private float lastReward = 0;
    
    private void setGlobalsToZero(){
        this.wasHit = 0;
        this.wasHitByEnemy = 0;
        this.hitWall = 0;
        this.enemySpotted = 0;
        this.hitEnemy = 0;
        this.missEnemy = 0;
    }

    public void onScannedRobot(ScannedRobotEvent e){
        this.wasHit = 1;
    }

    @Override
    public void evaluateFitness(ArrayList<Genome> population) {
        
        for(Genome gene : population){
            float fitness = (float) 0.0;
            gene.setFitness(fitness);
            for(int i = 0; i < NEAT_Config.INPUTS; i++){
                float inputs[] = {wasHit, wasHitByEnemy, hitWall, enemySpotted, hitEnemy, missEnemy, (float) this.getEnergy(), 
                    (float) this.getX(), (float) this.getY(), (float) this.getGunHeading(), (float) this.getGunHeat(), (float) this.getRadarHeading(),
                    (float) this.getVelocity(), (float) this.getOthers(), (float) this.getDistanceRemaining(), lastReward};
                
                    float output[] = gene.evaluateNetwork(inputs);
                    float expected[] = getQValue(); //TODO: Implement this shit somehow
                    fitness += (1-Math.abs(expected[0] - output[0]));
            }
            fitness *= fitness;
            gene.setFitness(fitness);
        }
        this.setGlobalsToZero(); //idk if this should be here
        
    }
    
}
