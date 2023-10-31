package sample;

@SuppressWarnings("WeakerAccess")
public class NN {

	public static double NNtrain(double[] Xtrain, double[] Ytrain, double[][] w_hx, double[][] w_yh, boolean doTraining) {
		int no_h = 19;  // number of neurons in hidden layer
		int c_b = 6;    // number of neurons in input layer
		double rho = 0.00001;  // learning rate
		double alpha = 0.9;
		int d_x = Xtrain.length;
		int n_y = Ytrain.length;
		int d_y = 1;

		double[][] prev_delw_y = new double[d_y][no_h+1];
		double[][] prev_delw_h = new double[no_h][d_x];
		double[] tj = new double[no_h+1];
        double beta_2, curr_delw_y;

		for (int i = 0; i < no_h; ++i) {
            for (int j = 0; j < d_x; ++j) {
                prev_delw_h[i][j] = 0;
            }
		}
		      
        for (int i = 0; i < d_y; ++i){
            for (int j = 0; j < no_h+1; ++j){
                prev_delw_y[i][j] = 0;
            }
        }
		      
        //forward propogation:
        double[] h = new double[no_h+1];
        for (int i = 0; i < no_h+1; ++i){
            h[i] = 0;
        }

        //hardcoding bias term to 1:
		h[no_h] = 1;
        double[] y_hat = new double[n_y];
		for (int i = 0; i < n_y; ++i){
		    y_hat[i] = 0;
		}
		      
		MatrixMultiplication matrix = new MatrixMultiplication();
        double nn_qvalue = 0;

        for (int i = 0; i < no_h; ++i) {
            double s = 0;
            for (int j = 0; j < d_x; ++j) {
                s = s + w_hx[i][j] * Xtrain[j];
            }
            h[i] = matrix.sig(s);
        }
                
        if(doTraining) { //if true train weights
            double s1 = 0;
            for(int i = 0; i < d_y; ++i) {
                for(int j = 0; j < no_h+1; ++j) {
                    s1 = s1 + w_yh[i][j] * h[j];
                }
                y_hat[0] = s1;
            }
		    		  
            beta_2 = Ytrain[0] - y_hat[0];
            for (int i = 0; i <w_yh[0].length; ++i) {
                 curr_delw_y = beta_2 * h[i];
                 w_yh[0][i] = w_yh[0][i] + rho * curr_delw_y + alpha * prev_delw_y[0][i];
                 prev_delw_y[0][i] = rho * curr_delw_y + alpha * prev_delw_y[0][i];
            }

            for (int i = 0; i < no_h; ++i) {
                double s = 0;
                for (int j = 0; j < d_x; ++j) {
                    s = s + w_hx[i][j] * Xtrain[j];
                }
                tj[i] = s;
            }

		    double[][] delw_h = new double[no_h][c_b];
		    for (int i = 0; i < no_h; ++i) {
		        for (int j = 0; j < c_b; ++j) {
		            delw_h[i][j] = beta_2 * matrix.sigbi(tj[i]) * (1 - matrix.sigbi(tj[i])) * Xtrain[j];
		            w_hx[i][j] = w_hx[i][j] + rho * delw_h[i][j] + alpha * prev_delw_h[i][j];
		            prev_delw_h[i][j] = rho * delw_h[i][j] + alpha * prev_delw_h[i][j];
		        }
		    }
        } else {
            for(int i = 0; i < d_y; ++i) {
                double s1 = 0;
                for(int j = 0; j < no_h+1; ++j) {
                    s1 = s1 + w_yh[i][j] * h[j];
                }
                nn_qvalue = s1;
            }
        }
        return nn_qvalue;
	}
}
