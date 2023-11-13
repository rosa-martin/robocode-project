package sample;
import sample.TransferFunction;

public class LeakyRelu implements TransferFunction {

    private double neg_slope;

    public LeakyRelu(){
        this.neg_slope = 0.01;   
    }

    public LeakyRelu(double neg_slope){
        this.neg_slope = neg_slope;
    }

    @Override
    public double evaluate(double value) {
        return Math.max(0, value) + neg_slope * Math.min(0, value);
    }

    @Override
    public double evaluateDerivate(double value) {
        
        if(value < 0){
            return 0 + neg_slope;
        } else {
            return 1;
        }
    }
    
}
