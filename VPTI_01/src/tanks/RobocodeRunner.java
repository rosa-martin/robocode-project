package tanks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import sample.ReLU;
import sample.Sample;
import sample.SigmoidalTransfer;
import sample.MultiLayerPerceptron;
import sample.QLearningRobotV2;
import sample.LeakyRelu;

public class RobocodeRunner {

	public static final double ALPHA = 0.0001;
	public static final int NUM_OF_INPUTS = 19;
    public static final int NUM_OF_OUTPUTS = QLearningRobotV2.Action.values().length; 
	public static int[] NUM_OF_NEURONS_PER_LAYER = new int[]{NUM_OF_INPUTS, 64, 128, 256, 512, NUM_OF_OUTPUTS};
	
	public static MultiLayerPerceptron mainNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, ALPHA, new ReLU());
    public static MultiLayerPerceptron targetNetwork = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, ALPHA, new ReLU());
	public static MultiLayerPerceptron weightsHolder = new MultiLayerPerceptron(NUM_OF_NEURONS_PER_LAYER, ALPHA, new ReLU());

	public static int NUM_OF_ROUNDS = 1000;
	public static int CURRENT_EPISODE = 0;
	public static int STEPS_DONE = 0;                  // How many times we have made a decision
	// THIS SHOULD DO IT
    public static ArrayList<Sample> memory = new ArrayList<Sample>();

	public static boolean isInitialTurn = true;

	public static void main(String[] args) throws IOException {
		String nazevTridyMehoRobota = "QLearningRobotV2";
		String seznamProtivniku = "X_GPbot_2_169";
		//String seznamProtivniku = "RL_DeadlyTurttle, GeneticTankBlueprint, X_GPbot_2_169";
		//String seznamProtivniku = "GeneticTankBlueprint";

		runRobocode(nazevTridyMehoRobota, seznamProtivniku);
	}

	public static void runRobocode(String mujRobot, String seznamProtivniku) throws IOException {

		// create src and dest path for compiling
		String src = "src/sample/" + mujRobot + ".java";
		String dst = "robots/sample/" + mujRobot + ".java";
		// compile our created robot and store it to robots/samples
		File source = new File(src);
		File dest = new File(dst);
		Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, System.out, System.out, dst);
		// remove all whitespaces
		seznamProtivniku = seznamProtivniku.replaceAll("\\s", "");
		// create list of tanks to fight

		String tanks[] = seznamProtivniku.split(",");
		String finalListOfTanks = "";
		for (String string : tanks) {
			src = "src/sample/" + string + ".java";
			dst = "robots/sample/" + string + ".java";
			// compile our created robot and store it to robots/samples
			source = new File(src);
			dest = new File(dst);
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			compiler.run(null, System.out, System.out, dst);

			string = "sample." + string + ",";
			finalListOfTanks += string;
		}

		finalListOfTanks += "sample." + mujRobot;

		// ("sample.Corners, sample.MujRobot"

		// Battle listener used for receiving battle events
		BattleObserver battleListener = new BattleObserver();

		// Create the RobocodeEngine
		RobocodeEngine engine = new RobocodeEngine(); // Run from current
														// working directory

		// Add battle listener to our RobocodeEngine
		engine.addBattleListener(battleListener);

		// Show the battles
		engine.setVisible(true);

		// Setup the battle specification

		int numberOfRounds = NUM_OF_ROUNDS;
		BattlefieldSpecification battlefield = new BattlefieldSpecification(800, 600); // 800x600
		// RobotSpecification[] selectedRobots =
		// engine.getLocalRepository("sample.Corners, sample.MujRobot");
		RobotSpecification[] selectedRobots = engine.getLocalRepository(finalListOfTanks);

		BattleSpecification battleSpec = new BattleSpecification(numberOfRounds, battlefield, selectedRobots);
		// Run our specified battle and let it run till it's over
		engine.runBattle(battleSpec, true/* wait till the battle is over */);

		for (BattleResults result : battleListener.getResults()) {
			System.out.println(result.getTeamLeaderName() + " - " + result.getScore());
		}

		// Cleanup our RobocodeEngine
		engine.close();

		// Make sure that the Java VM is shut down properly
		System.exit(0);
	}
}