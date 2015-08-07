package org.molgenis.dataexplorer.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class EntityReport extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "EntityReport";

	public static final EntityMetaData META_DATA = new EntityReportMeta();

	public static final String ENTITY = "entity";
	public static final String REPORT = "report";

	public EntityReport(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public String getEntity()
	{
		return getString(ENTITY);
	}

	public String getReport()
	{
		return getString(REPORT);
	}
}