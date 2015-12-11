package org.molgenis.data.i18n;

import java.io.IOException;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.security.core.runas.RunAsSystemProxy;

import com.google.common.collect.Lists;

public class MolgenisResourceBundleControl extends ResourceBundle.Control
{
	private final DataService dataService;

	public MolgenisResourceBundleControl(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException
	{
		if (!baseName.equals(I18nStringMetaData.ENTITY_NAME)) return null;
		// if (dataService.findOne(LanguageMetaData.ENTITY_NAME, locale.getLanguage()) == null) return null;

		return new MolgenisResourceBundle(dataService, locale.getLanguage());
	}

	protected static class MolgenisResourceBundle extends ListResourceBundle
	{
		private final DataService dataService;
		private final String languageCode;

		public MolgenisResourceBundle(DataService dataService, String languageCode)
		{
			this.dataService = dataService;
			this.languageCode = languageCode;
			setParent(ResourceBundle.getBundle("i18n", new Locale(languageCode)));
		}

		@Override
		protected Object[][] getContents()
		{
			List<Entity> entities = RunAsSystemProxy.runAsSystem(() -> Lists.newArrayList(dataService
					.findAll(I18nStringMetaData.ENTITY_NAME)));

			Object[][] contents = new Object[entities.size()][2];

			int i = 0;
			for (Entity entity : entities)
			{
				contents[i++] = new Object[]
				{ entity.getString(I18nStringMetaData.MSGID), entity.getString(languageCode) };
			}

			return contents;
		}
	}

}
