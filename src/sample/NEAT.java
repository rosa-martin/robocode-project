package sample;

import java.util.ArrayList;

import com.evo.NEAT.Environment;
import com.evo.NEAT.Genome;

public class NEAT implements Environment {
    @Override
    public void evaluateFitness(ArrayList<Genome> population) {
		float[] inputs = NeatRobot.getInputs();
        
		for(Genome gene : population){
            float fitness = 0.0f;
            gene.setFitness(0);
            // Predict next move
            float[] outputs = gene.evaluateNetwork(inputs);
            float expected = 1.0f;
            fitness += (1 - Math.abs(expected - outputs[0]));
            gene.setFitness(fitness);
        }
    }
}
