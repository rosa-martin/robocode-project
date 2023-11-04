package sample;

public class ReLU implements TransferFunction 
{
	@Override
	public double evaluate(double value) 
	{
		if(value > 0.0)
			return value;
		else
			return 0.0;
	}

	@Override
	public double evaluateDerivate(double value) 
	{
		if(value > 0.0)
			return 1.0;
		else
			return 0.0;
	}

}