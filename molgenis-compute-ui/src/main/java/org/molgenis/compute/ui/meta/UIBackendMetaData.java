package org.molgenis.compute.ui.meta;

import org.molgenis.data.support.DefaultEntityMetaData;

public class UIBackendMetaData extends DefaultEntityMetaData
{
	public static final UIBackendMetaData INSTANCE = new UIBackendMetaData();

	private static final String ENTITY_NAME = "Backend";
	public static final String IDENTIFIER = "identifier";
	public static final String HOST = "host";
	public static final String WORK_DIR = "workDir";
	public static final String BACKEND_TYPE = "backendType";
	public static final String SCHEDULER_TYPE = "scheduleType";
	public static final String HEADER_CALLBACK = "headerCallback";
	public static final String FOOTER_CALLBACK = "footerCallback";

	private UIBackendMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setVisible(false);
		addAttribute(HOST).setNillable(false);
		addAttribute(WORK_DIR);
		addAttribute(BACKEND_TYPE);
		addAttribute(SCHEDULER_TYPE);
		addAttribute(HEADER_CALLBACK);
		addAttribute(FOOTER_CALLBACK);

	}

}
