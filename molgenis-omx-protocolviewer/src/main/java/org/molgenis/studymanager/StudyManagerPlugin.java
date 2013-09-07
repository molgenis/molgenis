package org.molgenis.studymanager;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class StudyManagerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public StudyManagerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return StudyManagerController.URI;
	}
}
