package org.molgenis.compute.db.executor;

import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 6/14/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PilotManager
{
	private static Calendar calendar;

	public void checkExperiredPilots()
	{
		Database database = ApplicationUtil.getUnauthorizedPrototypeDatabase();
		try
		{
			List<Pilot> pilots = database.query(Pilot.class)
					.equals(Pilot.STATUS, PilotService.PILOT_SUBMITTED).find();

			for(Pilot pilot : pilots)
			{
				calendar = Calendar.getInstance();
				long now = calendar.getTimeInMillis();
				long creationTime = pilot.getCteationTime().getTime();

				long difference = now - creationTime;
				long lifeTerm = pilot.getLifeTerm() * 60 * 1000; //in milliseconds

				if (difference > lifeTerm)
				{
					pilot.setStatus(PilotService.PILOT_EXPIRED);
					database.update(pilot);
				}
			}
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}
}
