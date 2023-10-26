package sample;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
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

    public class Network {

        private NeuralNetwork<?> qNetwork;
        public int updates;

        public double[] evaluate(double[] input) {
            qNetwork.setInput(input);
            qNetwork.calculate();
            return qNetwork.getOutput();
        }

        public void downloadNetwork(String address, File dataFile) {
            try {
                FileUtils.copyURLToFile(new URL(address), dataFile);
                IHDF5Reader reader = HDF5Factory.openForReading(dataFile);
                this.updates = reader.int32().getAttr("/", "updates");

                System.out.format("Loaded network %s %d\n", dataFile.getName(), this.updates);

                this.qNetwork = new MultiLayerPerceptron(8, 32, 32, 6);
                setupLayer(this.qNetwork.getLayerAt(1), reader.readFloatMatrix("/fc1/w"), reader.readFloatArray("/fc1/b"),
                        new RectifiedLinear());
                setupLayer(this.qNetwork.getLayerAt(2), reader.readFloatMatrix("/fc2/w"), reader.readFloatArray("/fc2/b"),
                        new RectifiedLinear());
                setupLayer(this.qNetwork.getLayerAt(3), reader.readFloatMatrix("/out/w"), reader.readFloatArray("/out/b"),
                        new Linear());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setupLayer(Layer layer, float[][] weight, float[] bias, TransferFunction function) throws Exception {
            int w_i = 0;
            int w_j = 0;
            int b_i = 0;

            for (Neuron neuron : layer.getNeurons()) {
                if (neuron instanceof BiasNeuron)
                    continue;

                for (Connection conn : neuron.getInputConnections()) {
                    neuron.setTransferFunction(function);
                    if (conn.getFromNeuron() instanceof BiasNeuron) {
                        conn.setWeight(new Weight((double) bias[b_i++]));
                    } else {
                        conn.setWeight(new Weight((double) weight[w_i][w_j++]));
                    }
                }
                w_i++;
                w_j = 0;
            }

            if (b_i != bias.length || w_i != weight.length) {
                throw new Exception("Does the network description match between the client and the server?");
            }
        }

    }