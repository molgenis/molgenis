package org.molgenis.ui.style;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.settings.AppSettings;

import java.util.stream.Stream;

/**
 * Repository decorator that updates {@link StyleSheet} on {@link StyleSheet} changes.
 */
public class StyleSheetRepositoryDecorator extends AbstractRepositoryDecorator<StyleSheet>
{
	private final AppSettings appSettings;

	public StyleSheetRepositoryDecorator(Repository<StyleSheet> delegateRepository, AppSettings appSettings)
	{
		super(delegateRepository);
		this.appSettings = appSettings;
	}

	@Override
	public void delete(StyleSheet sheet)
	{
		checkAndUpdateAppSettings(sheet.getId());
		super.delete(sheet);
	}

	@Override
	public void deleteById(Object id)
	{
		checkAndUpdateAppSettings(id);
		super.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		throw new CannotDeleteAllThemesException();
	}

	@Override
	public void delete(Stream<StyleSheet> styleSheetStream)
	{
		styleSheetStream.forEach(sheet -> checkAndUpdateAppSettings(sheet.getId()));
		super.delete(styleSheetStream);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids.forEach(id -> checkAndUpdateAppSettings(id));
		super.deleteAll(ids);
	}

	private void checkAndUpdateAppSettings(Object id)
	{
		if (appSettings.getBootstrapTheme().equals(id))
		{
			throw new CannotDeleteCurrentThemeException();
		}
	}
}
