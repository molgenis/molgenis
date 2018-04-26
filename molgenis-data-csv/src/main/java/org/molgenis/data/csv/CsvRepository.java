package org.molgenis.data.csv;

import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;

/**
 * Repository implementation for csv files.
 * <p>
 * The filename without the extension is considered to be the entityname
 */
public class CsvRepository extends AbstractRepository
{
	private final File file;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;
	private final String sheetName;
	private List<CellProcessor> cellProcessors;
	private EntityType entityType;
	private Character separator = null;

	public CsvRepository(String file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		this(new File(file), entityTypeFactory, attrMetaFactory, null);
	}

	public CsvRepository(File file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors, Character separator)
	{
		this(file, entityTypeFactory, attrMetaFactory, StringUtils.stripFilenameExtension(file.getName()), null);
		this.separator = separator;
	}

	public CsvRepository(File file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors)
	{
		this(file, entityTypeFactory, attrMetaFactory, StringUtils.stripFilenameExtension(file.getName()), null);
	}

	public CsvRepository(File file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory,
			String sheetName, @Nullable List<CellProcessor> cellProcessors)
	{
		this.file = file;
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.sheetName = sheetName;
		this.cellProcessors = cellProcessors;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new CsvIterator(file, sheetName, cellProcessors, separator, getEntityType());
	}

	public EntityType getEntityType()
	{
		if (entityType == null)
		{
			entityType = entityTypeFactory.create(sheetName);

			for (String attrName : new CsvIterator(file, sheetName, null, separator).getColNamesMap().keySet())
			{
				Attribute attr = attrMetaFactory.create().setName(attrName).setDataType(STRING);
				entityType.addAttribute(attr);
			}
		}

		return entityType;
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public long count()
	{
		return Iterables.size(this);
	}

}
