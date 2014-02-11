package org.molgenis.gaf;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.molgenis.data.Entity;
import org.molgenis.gaf.GafListValidator.GafListValidationReport;
import org.molgenis.googlespreadsheet.GoogleSpreadsheetRepository;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;

/**
 * Google spreadsheet repository that only exposes valid GAF list runs
 */
public class GafListRepository extends GoogleSpreadsheetRepository
{
	private final GafListValidationReport report;

	public GafListRepository(SpreadsheetService spreadsheetService, String spreadsheetKey, String worksheetId,
			Visibility visibility, GafListValidationReport report) throws IOException, ServiceException
	{
		super(spreadsheetService, spreadsheetKey, worksheetId, visibility);
		if (report == null) throw new IllegalArgumentException("report is null");
		this.report = report;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<Entity> it = super.iterator();
		return new Iterator<Entity>()
		{
			private Entity nextEntity = null;

			@Override
			public boolean hasNext()
			{
				if (nextEntity != null)
				{
					return true;
				}
				else
				{
					nextEntity = getNext();
					return nextEntity != null;
				}
			}

			@Override
			public Entity next()
			{
				if (hasNext())
				{
					Entity tmpEntity = nextEntity;
					nextEntity = null;
					return tmpEntity;
				}
				else throw new NoSuchElementException();
			}

			private Entity getNext()
			{
				Entity nextEntity = null;
				if (it.hasNext())
				{
					do
					{
						Entity entity = it.next();
						String runId = entity.getString(GafListValidator.COL_RUN);
						if (runId != null && !report.hasErrors(runId))
						{
							nextEntity = entity;
							break;
						}
					}
					while (it.hasNext());
				}
				return nextEntity;
			}

			@Override
			public void remove()
			{
				it.remove();
			}
		};
	}
}
