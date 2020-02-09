import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.*;


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

	public TwoBit changeState(boolean branchTaken) {


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

		return this.predictor;

	}

	public void reportState() {

		switch (this.predictor) {
		case PNT_STRONG:
			System.out.println("PNT_STRONG");
			break;
		case PNT_WEAK:
			System.out.println("PNT_WEAK");
			break;
		case PT_WEAK:
			System.out.println("PT_WEAK");
			break;
		case PT_STRONG:
			System.out.println("PT_STRONG");
			break;

		}




	}
}

public class BranchSim {

	static short m = 0;
	static short n = 2;
	static short lsb = 8;
	static int table_count  = (int) Math.pow(2, m);
	static int  address_max =  (int)Math.pow(2, lsb);
	static int global_branch_history = 0;

	public static void main(String args[]) {

		//Receive tracefile path and create a file handle
		String trace_file_path = args[0];
		File trace_file = new File(trace_file_path);

		m = shortParse(args[1]);
		n = shortParse(args[2]);
		lsb = shortParse(args[3]);


		//Validate the directory
		if (trace_file.exists()) {

			System.out.println("Simulating " + args[0] + " with this config (" + m + " , " + n + ")");

			TwoBitPredictor[][] branchHistoryTable = new TwoBitPredictor[table_count][address_max];




			try (BufferedReader reader = new BufferedReader (new FileReader(trace_file))) {

				String line = reader.readLine();
				while (line != null) {

					int int_representation = Integer.parseInt(line.split(" ")[0], 16);
					int tnt = ((line.split(" ")[1].equals("T"))) ? 1 : 0 ;
					//System.out.println(Integer.toBinaryString(global_branch_history));
					global_branch_history = global_branch_history >> 1;
					global_branch_history = modifyBit(global_branch_history, m - 1, tnt);
					System.out.print("BGH: " + Integer.toBinaryString(global_branch_history));
					System.out.println(" INT: " + global_branch_history);


					String bin_rep = Integer.toBinaryString(int_representation);

					String leastbits = bin_rep.substring(bin_rep.length() - lsb);

					//System.out.println(Integer.parseInt(leastbits, 2));





					line = reader.readLine();
				}




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