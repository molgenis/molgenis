package org.molgenis.ui.style;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.settings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * Repository decorator that updates {@link StyleSheet} on {@link StyleSheet} changes.
 */
public class StyleSheetRepositoryDecorator extends AbstractRepositoryDecorator<StyleSheet>
{
	private static final Logger LOG = LoggerFactory.getLogger(StyleSheetRepositoryDecorator.class);
	private final AppSettings appSettings;

	public StyleSheetRepositoryDecorator(Repository<StyleSheet> delegateRepository, AppSettings appSettings)
	{
		super(delegateRepository);
		this.appSettings = appSettings;
	}

	@Override
	public void delete(StyleSheet sheet) throws MolgenisDataException
	{
		checkAndUpdateAppSettings(sheet.getId());
		super.delete(sheet);
	}

	@Override
	public void deleteById(Object id) throws MolgenisDataException
	{
		checkAndUpdateAppSettings(id);
		super.deleteById(id);
	}

	@Override
	public void deleteAll() throws MolgenisDataException
	{
		throw new MolgenisDataException(
				"Cannot delete all boostrap themes, at least one theme is needed for the application");
	}

	@Override
	public void delete(Stream<StyleSheet> styleSheetStream) throws MolgenisDataException
	{
		styleSheetStream.forEach(sheet -> checkAndUpdateAppSettings(sheet.getId()));
		super.delete(styleSheetStream);
	}

	@Override
	public void deleteAll(Stream<Object> ids) throws MolgenisDataException
	{
		ids.forEach(id -> checkAndUpdateAppSettings(id));
		super.deleteAll(ids);
	}

	private void checkAndUpdateAppSettings(Object id)
	{
		if (appSettings.getBootstrapTheme().equals(id))
		{
			throw new MolgenisDataException("Cannot delete the currently selected bootstrap theme");
		}
	}
}
