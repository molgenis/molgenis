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
			BufferedWriter logger = new BufferedWriter(new FileWriter("/Users/Roan/logger.txt"));
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
