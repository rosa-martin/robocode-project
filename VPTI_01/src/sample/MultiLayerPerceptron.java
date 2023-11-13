/*
 *     mlp-java, Copyright (C) 2012 Davide Gessa
 * 
 * 	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import robocode.RobocodeFileOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import net.sf.robocode.io.Logger;

public class MultiLayerPerceptron implements Cloneable
{
	protected double			fLearningRate = 0.6;
	protected Layer[]			fLayers;
	protected TransferFunction 	fTransferFunction;
	private long numOfWeights = 0;

	private double[] currY;
	private double[] formerY = null;

	//private String rootLocation = System.getProperty("user.dir");
	private String rootLocation = "/home/miggs/coding/java/eclipse-wspace/robocode-project/VPTI_01/robots/sample/QLearningRobotV2.data/";
	
	/**
	 * Crea una rete neuronale mlp
	 * 
	 * @param layers Numero di neuroni per ogni layer
	 * @param learningRate Costante di apprendimento
	 * @param fun Funzione di trasferimento
	 */
	public MultiLayerPerceptron(int[] layers, double learningRate, TransferFunction fun)
	{
		fLearningRate = learningRate;
		fTransferFunction = fun;
		
		fLayers = new Layer[layers.length];
		
		for(int i = 0; i < layers.length; i++)
		{			
			if(i != 0)
			{
				fLayers[i] = new Layer(layers[i], layers[i - 1]);
			}
			else
			{
				fLayers[i] = new Layer(layers[i], 0);
			}
		}

		numOfWeights = this.getNumOfWeights();
	}
	

	
	/**
	 * Esegui la rete
	 * 
	 * @param input Valori di input
	 * @return Valori di output restituiti dalla rete
	 */
	public double[] execute(double[] input)
	{
		int i;
		int j;
		int k;
		double new_value;
		
		double output[] = new double[fLayers[fLayers.length - 1].Length];
		
		// Put input
		for(i = 0; i < fLayers[0].Length; i++)
		{
			fLayers[0].Neurons[i].Value = input[i];
		}
		
		// Execute - hiddens + output
		for(k = 1; k < fLayers.length; k++)
		{
			for(i = 0; i < fLayers[k].Length; i++)
			{
				new_value = 0.0;
				for(j = 0; j < fLayers[k - 1].Length; j++)
					new_value += fLayers[k].Neurons[i].Weights[j] * fLayers[k - 1].Neurons[j].Value;
				
				new_value += fLayers[k].Neurons[i].Bias;
				
				fLayers[k].Neurons[i].Value = fTransferFunction.evaluate(new_value);
			}
		}
		
		// Get output
		for(i = 0; i < fLayers[fLayers.length - 1].Length; i++)
		{
			output[i] = fLayers[fLayers.length - 1].Neurons[i].Value;
		}

		output = sigmoid(output);
		
		return output;
	}

	public double[][] executeBatch(double[][] inputs)
{
    int i, j, k, b;
    double new_value;

    double[][] outputs = new double[inputs.length][fLayers[fLayers.length - 1].Length];

    // Loop over each input in the batch
    for(b = 0; b < inputs.length; b++) {
        double[] input = inputs[b];

        // Put input
        for(i = 0; i < fLayers[0].Length; i++)
        {
            fLayers[0].Neurons[i].Value = input[i];
        }

        // Execute - hiddens + output
        for(k = 1; k < fLayers.length; k++)
        {
            for(i = 0; i < fLayers[k].Length; i++)
            {
                new_value = 0.0;
                for(j = 0; j < fLayers[k - 1].Length; j++)
                    new_value += fLayers[k].Neurons[i].Weights[j] * fLayers[k - 1].Neurons[j].Value;

                new_value += fLayers[k].Neurons[i].Bias;

                fLayers[k].Neurons[i].Value = fTransferFunction.evaluate(new_value);
            }
        }

        // Get output
        for(i = 0; i < fLayers[fLayers.length - 1].Length; i++)
        {
            outputs[b][i] = fLayers[fLayers.length - 1].Neurons[i].Value;
        }
    }

    return outputs;
}

	
	private static double getMaxValue(double[] values) {
        double maxVal = 0.0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > maxVal) {
                maxVal = values[i];
            }
        }
        return maxVal;
    }

	public static double[] softmax(double[] neuronValues) {
        double total = 0;
		double[] eXs = new double[neuronValues.length];

		for (int i = 0; i < neuronValues.length; i++) {
			total += Math.exp(neuronValues[i] - getMaxValue(neuronValues));
		}
		for (int i = 0; i < neuronValues.length; i++){
			eXs[i] = (Math.exp(neuronValues[i] - getMaxValue(neuronValues))) / total;
		}

        return eXs;
    }

	public static double[] sigmoid(double[] neuronValues){
		double[] result = new double[neuronValues.length];
		for(int i = 0; i < neuronValues.length; i++){
			result[i] = 1 / (1 + Math.pow(Math.E, neuronValues[i]));
		}
		
		return result;
	}


	public void copyWeights(MultiLayerPerceptron dest) {
		for(int k = 1; k < fLayers.length; k++)
		{
			for(int i = 0; i < fLayers[k].Length; i++)
			{
				dest.fLayers[k].Neurons[i].Weights = fLayers[k].Neurons[i].Weights;
				dest.fLayers[k].Neurons[i].Bias = fLayers[k].Neurons[i].Bias;
				dest.fLayers[k].Neurons[i].Delta = fLayers[k].Neurons[i].Delta;
				dest.fLayers[k].Neurons[i].Value = fLayers[k].Neurons[i].Value;
			}
		}
	}

	public void saveWeights(String fileName, PrintStream out){

		try{
			int fileCtr = 0;
			RobocodeFileOutputStream rfs = new RobocodeFileOutputStream(rootLocation + "/" + fileName + fileCtr);
			byte[] binD = new byte[8];

			for(int i = 1; i < fLayers.length; i++){
				for(int j = 0; j < fLayers[i].Length; j++){
					for(int k = 0; k < fLayers[i].Neurons[j].Weights.length; k++){
						ByteBuffer.wrap(binD).putDouble(fLayers[i].Neurons[j].Weights[k]);
						rfs.write(binD);
					}
				}
			}

			rfs.close();
			out.println("Weights saved");

		} catch (Exception e){
			//out.println("Location: " + rootLocation);
			out.println("FUCKING ERROR AGAIN: " + e.getMessage());
		}
		
	}
	
	/**
	 * Algoritmo di backpropagation per il learning assistito
	 * (Versione multi threads)
	 * 
	 * Convergenza non garantita e molto lenta; utilizzare come criteri
	 * di stop una norma tra gli errori precedente e corrente, ed un
	 * numero massimo di iterazioni.
	 * 
	 * Wikipedia:
	 * 	The training data is broken up into equally large batches for each 
	 * 	of the threads. Each thread executes the forward and backward propagations. 
	 * 	The weight and threshold deltas are summed for each of the threads. 
	 * 	At the end of each iteration all threads must pause briefly for the 
	 * 	weight and threshold deltas to be summed and applied to the neural network. 
	 * 	This process continues for each iteration. 
	 * 
	 * @param input Valori in input
	 * @param output Valori di output atteso
	 * @param nthread Numero di thread da spawnare per il learning
	 * @return Errore delta tra output generato ed output atteso
	 */
	public double backPropagateMultiThread(double[] input, double[] output, int nthread)
	{
		return 0.0;
	}

	
	
	/**
	 * Algoritmo di backpropagation per il learning assistito
	 * (Versione single thread)
	 * 
	 * Convergenza non garantita e molto lenta; utilizzare come criteri
	 * di stop una norma tra gli errori precedente e corrente, ed un
	 * numero massimo di iterazioni.
	 * 
	 * @param input Valori in input (scalati tra 0 ed 1)
	 * @param output Valori di output atteso (scalati tra 0 ed 1)
	 * @return Errore delta tra output generato ed output atteso
	 */

	public double backPropagate2(double[] input, int action, double output){
		double new_output[] = execute(input);
		double error;
		int i;
		int j;
		int k;
		
		/* doutput = correct output (output) */
		// error = reward + GAMMA * maxQ(target) - qPred
		// Calcoliamo l'errore dell'output
		for(i = 0; i < fLayers[fLayers.length - 1].Length; i++)
		{
			error = output - new_output[action];
			fLayers[fLayers.length - 1].Neurons[i].Delta = error * fTransferFunction.evaluateDerivate(new_output[i]);
		} 
	
		
		for(k = fLayers.length - 2; k >= 0; k--)
		{
			// Calcolo l'errore dello strato corrente e ricalcolo i delta
			for(i = 0; i < fLayers[k].Length; i++)
			{
				error = 0.0;
				for(j = 0; j < fLayers[k + 1].Length; j++)
					error += fLayers[k + 1].Neurons[j].Delta * fLayers[k + 1].Neurons[j].Weights[i];
								
				fLayers[k].Neurons[i].Delta = error * fTransferFunction.evaluateDerivate(fLayers[k].Neurons[i].Value);				
			}
			
			// Aggiorno i pesi dello strato successivo
			for(i = 0; i < fLayers[k + 1].Length; i++)
			{
				for(j = 0; j < fLayers[k].Length; j++)
					fLayers[k + 1].Neurons[i].Weights[j] += fLearningRate * fLayers[k + 1].Neurons[i].Delta * fLayers[k].Neurons[j].Value;
				fLayers[k + 1].Neurons[i].Bias += fLearningRate * fLayers[k + 1].Neurons[i].Delta;
			}
		}	
		
		// Calcoliamo l'errore 
		error = huberLoss2(new_output[action], output, 1.0);
		
		return error;
	}

	public double backPropagate(double[] input, double[] output)
	{
		double new_output[] = execute(input);
		double error;
		int i;
		int j;
		int k;
		
		/* doutput = correct output (output) */
		
		// Calcoliamo l'errore dell'output
		for(i = 0; i < fLayers[fLayers.length - 1].Length; i++)
		{
			error = output[i] - new_output[i];
			fLayers[fLayers.length - 1].Neurons[i].Delta = error * fTransferFunction.evaluateDerivate(new_output[i]);
		} 
	
		
		for(k = fLayers.length - 2; k >= 0; k--)
		{
			// Calcolo l'errore dello strato corrente e ricalcolo i delta
			for(i = 0; i < fLayers[k].Length; i++)
			{
				error = 0.0;
				for(j = 0; j < fLayers[k + 1].Length; j++)
					error += fLayers[k + 1].Neurons[j].Delta * fLayers[k + 1].Neurons[j].Weights[i];
								
				fLayers[k].Neurons[i].Delta = error * fTransferFunction.evaluateDerivate(fLayers[k].Neurons[i].Value);				
			}
			
			// Aggiorno i pesi dello strato successivo
			for(i = 0; i < fLayers[k + 1].Length; i++)
			{
				for(j = 0; j < fLayers[k].Length; j++)
					fLayers[k + 1].Neurons[i].Weights[j] += fLearningRate * fLayers[k + 1].Neurons[i].Delta * 
							fLayers[k].Neurons[j].Value;
				fLayers[k + 1].Neurons[i].Bias += fLearningRate * fLayers[k + 1].Neurons[i].Delta;
			}
		}	
		
		// Calcoliamo l'errore 
		currY = new_output;
		error = huberLoss(new_output, output, 1.0);
		formerY = currY;
		
		return error;
	}

	public static double huberLoss2(double y, double y_pred, double delta) {
		double error = 0;
		double diff = y - y_pred;
		if (Math.abs(diff) <= delta) {
			error += 0.5 * diff * diff;
		} else {
			error += delta * (Math.abs(diff) - 0.5 * delta);
		}
		return error;
	}
	
	public static double huberLoss(double[] y, double[] y_pred, double delta) {
		double error = 0;
		//if(QLearningRobotV2.zerosCheck(y) == true && QLearningRobotV2.zerosCheck(y_pred) == true){
		//		return Double.MAX_VALUE;
		//}

		for (int i = 0; i < y.length; i++) {
			double diff = y[i] - y_pred[i];
			if (Math.abs(diff) <= delta) {
				error += 0.5 * diff * diff;
			} else {
				error += delta * (Math.abs(diff) - 0.5 * delta);
			}
		}

		//error += closeToZeroPenalization(y, y_pred) / 2;

		return error;
	}

	public static double closeToZeroPenalization(double[] currY, double[] formerY){
		if(formerY == null){
			return 0;
		} else {
			double currSum = 0;
			double formerSum = 0;
			for(int i = 0; i < currY.length; i++){
				currSum += currY[i];
				formerSum += formerY[i];
			}
			currSum /= currY.length;
			formerSum /= formerY.length; 
			return -(currSum - formerSum);
		}
	}

	public double batchBackPropagate(double[][] inputs, double[][] outputs)
{
    double error = 0.0;
    double totalError = 0.0;
    int i, j, k, b;

    // Initialize weight updates to zero
    double[][][] weightUpdates = new double[fLayers.length][][];
    for(i = 0; i < fLayers.length; i++) {
        weightUpdates[i] = new double[fLayers[i].Length + 1][];  // Add +1 here
        for(j = 0; j < fLayers[i].Length; j++) {
            weightUpdates[i][j] = new double[fLayers[i].Neurons[j].Weights.length + 1];  // Add +1 here
        }
    }

    // Loop over each input-output pair in the batch
    for(b = 0; b < inputs.length; b++) {
        double[] input = inputs[b];
        double[] output = outputs[b];

        double[] new_output = execute(input);

        // Calculate error and deltas for output layer
        for(i = 0; i < fLayers[fLayers.length - 1].Length; i++) {
            error = output[i] - new_output[i];
            fLayers[fLayers.length - 1].Neurons[i].Delta = error * fTransferFunction.evaluateDerivate(new_output[i]);
        }

        // Backpropagate error and calculate deltas for hidden layers
        for(k = fLayers.length - 2; k >= 0; k--) {
            for(i = 0; i < fLayers[k].Length; i++) {
                error = 0.0;
                for(j = 0; j < fLayers[k + 1].Length; j++)
                    error += fLayers[k + 1].Neurons[j].Delta * fLayers[k + 1].Neurons[j].Weights[i];

                fLayers[k].Neurons[i].Delta = error * fTransferFunction.evaluateDerivate(fLayers[k].Neurons[i].Value);
            }
        }

        // Calculate weight updates
        for(k = 0; k < fLayers.length - 1; k++) {
            for(i = 0; i < fLayers[k + 1].Length; i++) {
                for(j = 0; j < fLayers[k].Length; j++)
                    weightUpdates[k + 1][i][j] += fLearningRate * fLayers[k + 1].Neurons[i].Delta * fLayers[k].Neurons[j].Value;
                weightUpdates[k + 1][i][fLayers[k].Length] += fLearningRate * fLayers[k + 1].Neurons[i].Delta;  // Bias update
            }
        }

        // Calculate total error for the batch
        totalError += huberLoss(new_output, output, 1.0);
    }

    // Apply average weight updates
    for(k = 0; k < fLayers.length - 1; k++) {
        for(i = 0; i < fLayers[k + 1].Length; i++) {
            for(j = 0; j < fLayers[k].Length; j++)
                fLayers[k + 1].Neurons[i].Weights[j] += weightUpdates[k + 1][i][j] / inputs.length;
            fLayers[k + 1].Neurons[i].Bias += weightUpdates[k + 1][i][fLayers[k].Length] / inputs.length;
        }
    }

    return totalError / inputs.length;
}

	
	/**
	 * Salva una rete MLP su file
	 * 
	 * @param path Path nel quale salvare la rete MLP
	 * @return true se salvata correttamente
	 */
	public boolean save(String path)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this);
			oos.close();
		}
		catch (Exception e) 
		{ 
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Carica una rete MLP da file
	 * @param path Path dal quale caricare la rete MLP
	 * @return Rete MLP caricata dal file o null
	 */
	public static MultiLayerPerceptron load(String path)
	{
		try
		{
			MultiLayerPerceptron net;
			
			FileInputStream fin = new FileInputStream(path);
			ObjectInputStream oos = new ObjectInputStream(fin);
			net = (MultiLayerPerceptron) oos.readObject();
			oos.close();
			
			return net;
		}
		catch (Exception e) 
		{ 
			return null;
		}
	}
	
	

	/**
	 * @return Costante di apprendimento
	 */
	public double getLearningRate()
	{
		return fLearningRate;
	}
	
	
	/**
	 * 
	 * @param rate
	 */
	public void	setLearningRate(double rate)
	{
		fLearningRate = rate;
	}
	
	
	/**
	 * Imposta una nuova funzione di trasferimento
	 * 
	 * @param fun Funzione di trasferimento
	 */
	public void setTransferFunction(TransferFunction fun)
	{
		fTransferFunction = fun;
	}
	
	
	
	/**
	 * @return Dimensione layer di input
	 */
	public int getInputLayerSize()
	{
		return fLayers[0].Length;
	}
	
	
	/**
	 * @return Dimensione layer di output
	 */
	public int getOutputLayerSize()
	{
		return fLayers[fLayers.length - 1].Length;
	}

	private long getNumOfWeights(){
		long iterator = 0;
		for(int i = 1; i < fLayers.length; i++){
			for(int j = 0; j < fLayers[i].Length; j++){
				for(int k = 0; k < fLayers[i].Neurons[j].Weights.length; k++){
					iterator++;
				}
			}
		}
		return iterator;
	}

	public long getFastNumOfWeights(){
		return this.numOfWeights;
	}
}

