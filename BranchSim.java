import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.*;
import java.util.Arrays;

class TwoBitPredictor {

	public enum TwoBit {

		PNT_STRONG,
		PNT_WEAK,
		PT_STRONG,
		PT_WEAK

	}

	TwoBit predictor;

	TwoBitPredictor() {

		this.predictor = TwoBit.PNT_STRONG ;

	}

	public void changeState(boolean branchTaken) {


		switch (this.predictor) {
		case PNT_STRONG:
			if (branchTaken) {this.predictor = TwoBit.PNT_WEAK;}
			break;
		case PNT_WEAK:
			if (branchTaken) {this.predictor = TwoBit.PT_STRONG;}
			else {this.predictor = TwoBit.PNT_STRONG;}
			break;
		case PT_WEAK:
			if (branchTaken) {this.predictor = TwoBit.PT_STRONG;}
			else {
				this.predictor  = TwoBit.PNT_STRONG;
			}
			break;
		case PT_STRONG:
			if ( !branchTaken) {this.predictor = TwoBit.PT_WEAK;}
			break;
		}


	}

	public boolean reportState() {

		switch (this.predictor) {
		case PNT_STRONG:
			//System.out.println("PNT_STRONG");
			return false;
		//break;
		case PNT_WEAK:
			//System.out.println("PNT_WEAK");
			return false;
		//break;
		case PT_WEAK:
			//System.out.println("PT_WEAK");
			return true;
		//break;
		case PT_STRONG:
			//System.out.println("PT_STRONG");
			return true;
			//break;

		}

		return false;
	}
}

public class BranchSim {

	static boolean n1State = false;
	static short m ;
	static short n ;
	static short lsb;
	static int table_count ;
	static int global_branch_history;
	static int  address_max;
	static double misFire = 0;
	static double branchCount = 0;

	public static void main(String args[]) {

		//Receive tracefile path and create a file handle
		String trace_file_path = args[0];
		File trace_file = new File(trace_file_path);

		m = shortParse(args[1]);
		n = shortParse(args[2]);
		lsb = shortParse(args[3]);

		int table_count = (int) Math.pow(2, m);
		int address_max =  (int)Math.pow(2, lsb);
		int  global_branch_history = 0;

		//Validate the directory
		if (trace_file.exists()) {

			System.out.println("Simulating " + args[0] + " with this config (" + m + " , " + n + ")");

			//Creating and initializing the 2 bit predictor
			TwoBitPredictor[][] branchHistoryTable = new TwoBitPredictor[table_count][address_max];
			for (int x = 0; x < table_count; x++) {

				for (int q = 0; q < address_max; q++) {
					branchHistoryTable[x][q] = new TwoBitPredictor();
					//System.out.println(""+x+" "+q);
				}
			}

			//Creating an initializing a one bit predictor
			Boolean[][] oneBitPredictor = new Boolean[table_count][address_max];
			for (int x = 0; x < table_count; x++) {
				Arrays.fill(oneBitPredictor[x], Boolean.FALSE);
			}

			//System.out.print("Bool boye" + oneBitPredictor[0][0].toString());


			try (BufferedReader reader = new BufferedReader (new FileReader(trace_file))) {

				boolean predicted_state = false;
				String line = reader.readLine();
				while (line != null) {



					int int_representation = Integer.parseInt(line.split(" ")[0], 16);
					int tnt = ((line.split(" ")[1].equals("T"))) ? 1 : 0 ;
					//System.out.println(line);
					boolean tnt_bool = (tnt == 1) ? true : false;

					String bin_rep = Integer.toBinaryString(int_representation);

					String leastbits = bin_rep.substring(bin_rep.length() - lsb);

					int least_bits_lookup = Integer.parseInt(leastbits, 2);

					if (n != 1) {
						predicted_state = branchHistoryTable[global_branch_history][least_bits_lookup].reportState();
						branchHistoryTable[global_branch_history][least_bits_lookup].changeState(tnt_bool);
						if ( predicted_state != tnt_bool) {

							misFire++;

						}
					} else {

						predicted_state = oneBitPredictor[global_branch_history][least_bits_lookup];
						if ( predicted_state != tnt_bool) {

							misFire++;
							oneBitPredictor[global_branch_history][least_bits_lookup] = (predicted_state)?false:true;

						}


					}


					branchCount++;

					//System.out.println("LBLOOKUP: "+least_bits_lookup+ " GBH: "+ global_branch_history );

					if (m != 0) {
						global_branch_history = global_branch_history >> 1;
						global_branch_history = modifyBit(global_branch_history, m - 1, tnt);
					}
					//System.out.println(global_branch_history);


					line = reader.readLine();
				}


				System.out.println("MmisFire " + misFire);
				System.out.println("Branch Count = " + branchCount);
				System.out.println("Misprediction rate = " + (misFire / branchCount) * 100);


			} catch (Exception e) {}




		}






	}


	public static Short shortParse(String text) {
		try {
			return Short.parseShort(text);
		} catch (NumberFormatException e) {

			System.out.println("Wrong input type for m & n values, Required - Integer");
			System.exit(0);
			return null;
		}
	}

	public static int modifyBit(int n,
	                            int p,
	                            int b) {
		int mask = 1 << p;
		return (n & ~mask) |
		       ((b << p) & mask);
	}


}