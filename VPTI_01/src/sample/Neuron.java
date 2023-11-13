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

public class Neuron 
{
	public double		Value;
	public double[]		Weights;
	public double		Bias;
	public double		Delta;
	
	public Neuron(int prevLayerSize)
	{
		Weights = new double[prevLayerSize];
		Bias = Math.random() / 1000000;
		Delta = Math.random() / 1000000;
		Value = Math.random() / 1000000;
		
		for(int i = 0; i < Weights.length; i++)
			Weights[i] = Math.random();
	}

	public String stringifyWeights(){
		String res = "[";
		for(int i = 0; i < Weights.length+1; i++){
			if(Weights.length - i == 1){
				res += Weights[i] + "]";
				break;
			}
			res += Weights[i] + ", ";
		}

		return res;
	}
}
