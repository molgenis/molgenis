package org.molgenis.data.csv;

import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;

/**
 * Repository implementation for csv files.
 * <p>
 * The filename without the extension is considered to be the entityname
 */
public class CsvRepository extends AbstractRepository
{
	private final File file;
	private final EntityTypeFactory entityMetaFactory;
	private final AttributeMetaDataFactory attrMetaFactory;
	private final String sheetName;
	private List<CellProcessor> cellProcessors;
	private EntityMetaData entityMetaData;
	private Character separator = null;

	public CsvRepository(String file, EntityTypeFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory)
	{
		this(new File(file), entityMetaFactory, attrMetaFactory, null);
	}

	public CsvRepository(File file, EntityTypeFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors, Character separator)
	{
		this(file, entityMetaFactory, attrMetaFactory, StringUtils.stripFilenameExtension(file.getName()), null);
		this.separator = separator;
	}

	public CsvRepository(File file, EntityTypeFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors)
	{
		this(file, entityMetaFactory, attrMetaFactory, StringUtils.stripFilenameExtension(file.getName()), null);
	}

	public CsvRepository(File file, EntityTypeFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory,
			String sheetName, @Nullable List<CellProcessor> cellProcessors)
	{
		this.file = file;
		this.entityMetaFactory = requireNonNull(entityMetaFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.sheetName = sheetName;
		this.cellProcessors = cellProcessors;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new CsvIterator(file, sheetName, cellProcessors, separator, getEntityMetaData());
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = entityMetaFactory.create().setSimpleName(sheetName);

			for (String attrName : new CsvIterator(file, sheetName, null, separator).getColNamesMap().keySet())
			{
				AttributeMetaData attr = attrMetaFactory.create().setName(attrName).setDataType(STRING);
				entityMetaData.addAttribute(attr);
			}
		}

		return entityMetaData;
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
