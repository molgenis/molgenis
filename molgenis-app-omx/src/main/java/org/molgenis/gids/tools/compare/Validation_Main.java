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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
