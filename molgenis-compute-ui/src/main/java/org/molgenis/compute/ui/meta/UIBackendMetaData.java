package org.molgenis.compute.ui.meta;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIBackendMetaData extends DefaultEntityMetaData
{
	public static final UIBackendMetaData INSTANCE = new UIBackendMetaData();

	public static final String ENTITY_NAME = "Backend";
	public static final String URL = "url";
	public static final String WORK_DIR = "workDir";
	public static final String BACKEND_TYPE = "backendType";
	public static final String SCHEDULER_TYPE = "scheduleType";

	private UIBackendMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(URL).setIdAttribute(true).setNillable(false);
		addAttribute(WORK_DIR);
		addAttribute(BACKEND_TYPE);
		addAttribute(SCHEDULER_TYPE);
	}

}
