package org.molgenis.ngs.decorators;

import org.molgenis.omx.ngs.Flowcell;

/**
 * Generic decorator for NGS values. Used to alert the user when values do not
 * met certain criteria.
 * 
 * @version 0.1.0.0
 * 
 * @author Marcel Burger
 * 
 * @param <E>
 */
public class ValidateFlowcellValue<E extends Flowcell>
{
	private static boolean show = false;
	private static String message = "";

	public ValidateFlowcellValue(String flowcellName, String run)
	{
		checkFlowcellName(flowcellName);
		checkRunNr(run);
	}

	public void showMessage(String message)
	{
		// TODO: Show message to the user.
	}

	public void checkFlowcellName(String flowcellName)
	{
		try
		{
			// TODO: Check if value is ok and generate message if not

			if (show)
			{
				showMessage(message);
			}
		}
		catch (Exception e)
		{
			e.getStackTrace();
		}
	}

	public void checkRunNr(String run)
	{
		try
		{
			// TODO: Check if value is ok and generate message if not

			if (show)
			{
				showMessage(message);
			}
		}
		catch (Exception e)
		{
			e.getStackTrace();
		}
	}

}
