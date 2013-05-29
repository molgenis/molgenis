package org.molgenis.lifelines.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.lifelines.studydefinition.StudyDefinitionLoaderController;

/**
 * StudyDefinitionLoader plugin.
 * 
 * IFramePluging for the StudyDefinitionLoaderController
 * 
 * @author erwin
 * 
 */
public class StudyDefinitionLoaderPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public StudyDefinitionLoaderPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return StudyDefinitionLoaderController.BASE_URL + StudyDefinitionLoaderController.LOAD_LIST_URI;
	}

}
