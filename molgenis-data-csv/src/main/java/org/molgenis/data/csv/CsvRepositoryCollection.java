package org.molgenis.data.csv;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads csv and tsv files. Can be bundled together in a zipfile.
 * <p>
 * The exposes the files as {@link org.molgenis.data.Repository}. The names of the repositories are the names of the
 * files without the extension
 */
public class CsvRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "CSV";
	static final String MAC_ZIP = "__MACOSX";
	private final File file;
	private EntityTypeFactory entityTypeFactory;
	private AttributeFactory attrMetaFactory;
	private List<String> entityTypeIds;
	private List<String> entityTypeIdsLowerCase;

	public CsvRepositoryCollection(File file) throws MolgenisInvalidFormatException, IOException
	{
		this(file, (CellProcessor[]) null);
	}

	public CsvRepositoryCollection(File file, CellProcessor... cellProcessors)
	{
		super(CsvFileExtensions.getCSV(), cellProcessors);
		this.file = file;

		loadEntityNames();
	}

	@Override
	public void init() throws IOException
	{
		// no operation
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return entityTypeIds;
	}

	@Override
	public Repository<Entity> getRepository(String id)
	{
		if (!entityTypeIdsLowerCase.contains(id.toLowerCase()))
		{
			return null;
		}

		return new CsvRepository(file, entityTypeFactory, attrMetaFactory, id, cellProcessors);
	}

	private void loadEntityNames()
	{
		String extension = StringUtils.getFilenameExtension(file.getName());
		entityTypeIds = Lists.newArrayList();
		entityTypeIdsLowerCase = Lists.newArrayList();

		if (extension.equalsIgnoreCase("zip"))
		{
			ZipFile zipFile = null;
			try
			{
				zipFile = new ZipFile(file);

				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); )
				{
					ZipEntry entry = e.nextElement();
					if (!entry.getName().contains(MAC_ZIP) && !entry.isDirectory())
					{
						String name = getRepositoryName(entry.getName());
						entityTypeIds.add(name);
						entityTypeIdsLowerCase.add(name.toLowerCase());
					}
				}
			}
			catch (Exception e)
			{
				throw new MolgenisDataException(e);
			}
			finally
			{
				IOUtils.closeQuietly(zipFile);
			}
		}
		else
		{
			String name = getRepositoryName(file.getName());
			entityTypeIds.add(name);
			entityTypeIdsLowerCase.add(name.toLowerCase());
		}
	}

	private static String getRepositoryName(String fileName)
	{
		return StringUtils.stripFilenameExtension(StringUtils.getFilename(fileName));
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return new Iterator<Repository<Entity>>()
		{
			Iterator<String> it = getEntityTypeIds().iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository<Entity> next()
			{
				return getRepository(it.next());
			}

		};
	}

	@Override
	public boolean hasRepository(String name)
	{
		return entityTypeIds.contains(name);
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getId());
	}

	@Autowired
	public void setEntityTypeFactory(EntityTypeFactory entityTypeFactory)
	{
		this.entityTypeFactory = entityTypeFactory;
	}

	@Autowired
	public void setAttributeFactory(AttributeFactory attrMetaFactory)
	{
		this.attrMetaFactory = attrMetaFactory;
	}
}
