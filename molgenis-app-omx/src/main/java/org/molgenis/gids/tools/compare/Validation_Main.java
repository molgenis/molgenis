package org.molgenis.gids.tools.compare;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Validation_Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.err
					.println("To run this script it needs 3 arguments; \n1) An excelfile\n2) a csvfile\n"
							+ "3) a path for the logfile\n"
							+ "e.g.\n"
							+ "args[0] /Users/Roan/Work/GIDS_8_may/Cohorts/Bloodbank/Export_Gids1_Bloodbank.xls\n"
							+ "args[1] /Users/Roan/Work/GIDS_8_may/Cohorts/Bloodbank/Export_Gids2_Bloodbank_dataset125_2013-05-08.csv/\n"
							+ "args[2] /Users/Roan/logger.txt");
			return;
		}
		ValidationChecker vc = new ValidationChecker();
		try
		{
			BufferedWriter logger = new BufferedWriter(new FileWriter(args[2]));
			logger.write("file1: " + args[0] + "\nfile2: " + args[1]);
			vc.check(args[0], args[1], logger);
			logger.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

	}
}
