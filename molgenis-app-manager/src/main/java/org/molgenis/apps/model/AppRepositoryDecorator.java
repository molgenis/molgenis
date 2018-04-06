package org.molgenis.apps.model;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static java.io.File.separatorChar;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;

public class AppRepositoryDecorator extends AbstractRepositoryDecorator<App>
{
	private static final Logger LOG = LoggerFactory.getLogger(AppRepositoryDecorator.class);

	private final FileStore fileStore;

	public AppRepositoryDecorator(Repository<App> delegateRepository, FileStore fileStore)
	{
		super(delegateRepository);
		this.fileStore = requireNonNull(fileStore);
	}

	@Override
	public void add(App app)
	{
		addApp(app);
		super.add(app);
	}

	@Override
	public Integer add(Stream<App> appStream)
	{
		return super.add(appStream.filter(app ->
		{
			addApp(app);
			return true;
		}));
	}

	@Override
	public void update(App app)
	{
		updateApp(app, findOneById(app.getId()));
		super.update(app);
	}

	@Override
	public void update(Stream<App> appStream)
	{
		super.update(appStream.filter(app ->
		{
			updateApp(app, findOneById(app.getId()));
			return true;
		}));
	}

	@Override
	public void delete(App app)
	{
		deleteApp(app);
		super.delete(app);
	}

	@Override
	public void deleteById(Object id)
	{
		deleteApp(findOneById(id));
		super.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		query().findAll().forEach(this::deleteApp);
		super.deleteAll();
	}

	@Override
	public void delete(Stream<App> appStream)
	{
		super.delete(appStream.filter(app ->
		{
			this.deleteApp(app);
			return true;
		}));
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		super.deleteAll(ids.filter(id ->
		{
			this.deleteApp(findOneById(id));
			return true;
		}));
	}

	private void addApp(App app)
	{
		validateResourceZip(app);
		if (app.isActive())
		{
			activateApp(app);
		}
	}

	private void updateApp(App app, App existingApp)
	{
		FileMeta appSourceFiles = app.getSourceFiles();
		FileMeta existingAppSourceFiles = existingApp.getSourceFiles();

		if (appSourceFiles != null)
		{
			if (existingAppSourceFiles != null)
			{
				if (!appSourceFiles.getId().equals(existingAppSourceFiles.getId()))
				{
					validateResourceZip(app);
					if (existingApp.isActive())
					{
						deactivateApp(existingApp);
					}
					if (app.isActive())
					{
						activateApp(app);
					}
				}
				else
				{
					if (app.isActive() && !existingApp.isActive())
					{
						activateApp(app);
					}
					else if (!app.isActive() && existingApp.isActive())
					{
						deactivateApp(app);
					}
				}
			}
			else
			{
				validateResourceZip(app);
				if (app.isActive())
				{
					activateApp(app);
				}
			}
		}
		else
		{
			//noinspection VariableNotUsedInsideIf
			if (existingAppSourceFiles != null)
			{
				if (existingApp.isActive())
				{
					deactivateApp(existingApp);
				}
			}
		}
	}

	private void deleteApp(App app)
	{
		if (app.isActive())
		{
			deactivateApp(app);
		}
	}

	private void activateApp(App app)
	{
		FileMeta appSourceArchive = app.getSourceFiles();
		if (appSourceArchive != null)
		{
			File fileStoreFile = fileStore.getFile(appSourceArchive.getId());
			if (fileStoreFile == null)
			{
				LOG.error("Source archive '{}' for app '{}' missing in file store", appSourceArchive.getId(),
						app.getName());
				throw new RuntimeException("An error occurred trying to activate app");
			}

			try
			{
				ZipFile zipFile = new ZipFile(fileStoreFile);
				if (!app.getUseFreemarkerTemplate())
				{
					FileHeader fileHeader = zipFile.getFileHeader("index.html");
					if (fileHeader == null)
					{
						LOG.error(
								"Missing index.html in {} while option Use freemarker template as index.html was set 'No'",
								app.getName());
						throw new RuntimeException(
								format("Missing index.html in %s while option 'Use freemarker template as index.html' was set 'No'",
										app.getName()));
					}
				}
				//noinspection StringConcatenationMissingWhitespace
				zipFile.extractAll(
						fileStore.getStorageDir() + separatorChar + FILE_STORE_PLUGIN_APPS_PATH + separatorChar
								+ app.getId() + separatorChar);
			}
			catch (ZipException e)
			{
				LOG.error("", e);
				throw new RuntimeException(format("An error occurred activating app '%s'", app.getName()));
			}
		}
	}

	private void deactivateApp(App app)
	{
		try
		{
			//noinspection StringConcatenationMissingWhitespace
			fileStore.deleteDirectory(FILE_STORE_PLUGIN_APPS_PATH + separatorChar + app.getId());
		}
		catch (IOException e)
		{
			LOG.error("", e);
			throw new RuntimeException(format("An error occurred deactivating app '%s'", app.getName()));
		}
	}

	private void validateResourceZip(App app)
	{
		FileMeta appZipMeta = app.getSourceFiles();
		if (appZipMeta != null)
		{
			File fileStoreFile = fileStore.getFile(appZipMeta.getId());
			if (fileStoreFile == null)
			{
				LOG.error("Resource zip '{}' for app '{}' missing in file store", appZipMeta.getId(), app.getName());
				throw new RuntimeException("An error occurred trying to create or update app");
			}

			ZipFile zipFile;
			try
			{
				zipFile = new ZipFile(fileStoreFile);
			}
			catch (ZipException e)
			{
				LOG.error("Error creating zip file object", e);
				throw new RuntimeException("An error occurred trying to create or update app");
			}
			if (!zipFile.isValidZipFile())
			{
				throw new MolgenisValidationException(new ConstraintViolation(
						String.format("'%s' is not a valid zip file.", appZipMeta.getFilename())));
			}
		}
	}
}
