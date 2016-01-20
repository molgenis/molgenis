package org.molgenis.data.i18n;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.io.IOException;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResourceBundle Control that gets it content from the i18nstrings repository
 */
public class MolgenisResourceBundleControl extends ResourceBundle.Control
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisResourceBundleControl.class);
	private final DataService dataService;

	public MolgenisResourceBundleControl(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException
	{
		String languageCode = locale.getLanguage();

		// Only handle i18nstrings bundle
		if (!baseName.equals(I18nStringMetaData.ENTITY_NAME)) return null;

		// Only handle languages that are present in the languages repository
		if (runAsSystem(() -> dataService.query(LanguageMetaData.ENTITY_NAME).eq(LanguageMetaData.CODE, languageCode)
				.count()) == 0)
			return null;

		return new MolgenisResourceBundle(dataService, languageCode);
	}

	protected static class MolgenisResourceBundle extends ListResourceBundle
	{
		private final DataService dataService;
		private final String languageCode;

		public MolgenisResourceBundle(DataService dataService, String languageCode)
		{
			this.dataService = dataService;
			this.languageCode = languageCode;
		}

		@Override
		protected Object[][] getContents()
		{
			List<Entity> entities = runAsSystem(
					() -> dataService.findAll(I18nStringMetaData.ENTITY_NAME).collect(Collectors.toList()));

			Object[][] contents = new Object[entities.size()][2];

			int i = 0;
			for (Entity entity : entities)
			{
				String msgid = entity.getString(I18nStringMetaData.MSGID);
				String msg = entity.getString(languageCode);
				if (msg == null)
				{
					// Missing translation for this language
					LOG.warn("Missing '{}' msg for language '{}'", msgid, languageCode);
					msg = "#" + msgid + "#";
				}

				contents[i++] = new Object[]
				{ msgid, msg };
			}

			return contents;
		}
	}

}
