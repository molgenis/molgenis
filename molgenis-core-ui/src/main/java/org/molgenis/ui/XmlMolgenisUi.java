package org.molgenis.ui;

import java.io.IOException;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;

public class XmlMolgenisUi implements MolgenisUi
{
	static final String DEFAULT_APP_HREF_LOGO = "/img/logo_molgenis_letterbox.png";
	static final String KEY_APP_NAME = "app.name";
	static final String KEY_APP_HREF_LOGO = "app.href.logo";
	static final String KEY_APP_HREF_CSS = "app.href.css";

	private final Molgenis molgenisUi;
	private final MolgenisPermissionService molgenisPermissionService;
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public XmlMolgenisUi(XmlMolgenisUiLoader xmlMolgenisUiLoader, MolgenisSettings molgenisSettings,
			MolgenisPermissionService molgenisPermissionService) throws IOException
	{
		if (xmlMolgenisUiLoader == null) throw new IllegalArgumentException("XmlMolgenisUiLoader is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("MolgenisSettings is null");
		this.molgenisUi = xmlMolgenisUiLoader.load();
		this.molgenisSettings = molgenisSettings;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@Override
	public String getTitle()
	{
		String title = molgenisSettings.getProperty(KEY_APP_NAME);
		if (title == null) title = molgenisUi.getLabel();
		if (title == null) title = molgenisUi.getName();
		return title;
	}

	@Override
	public String getHrefLogo()
	{
		return molgenisSettings.getProperty(KEY_APP_HREF_LOGO, DEFAULT_APP_HREF_LOGO);
	}

	@Override
	public String getHrefCss()
	{
		return molgenisSettings.getProperty(KEY_APP_HREF_CSS);
	}

	@Override
	public MolgenisUiMenu getMenu()
	{
		// TODO skip any forms and plugins in the root, see molgenis_ui.xsd for proposed changes
		for (Object menuItem : molgenisUi.getFormOrMenuOrPlugin())
		{
			if (menuItem instanceof MenuType)
			{
				return new XmlMolgenisUiMenu(molgenisPermissionService, (MenuType) menuItem);
			}
		}
		throw new RuntimeException("missing required menu item");
	}
}
