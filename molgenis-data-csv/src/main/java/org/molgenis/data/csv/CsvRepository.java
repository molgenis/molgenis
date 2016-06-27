package org.molgenis.data.csv;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.STRING;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.springframework.util.StringUtils;

import com.google.common.collect.Iterables;

/**
 * Repository implementation for csv files.
 * 
 * The filename without the extension is considered to be the entityname
 */
public class CsvRepository extends AbstractRepository
{
	private final File file;
	private final EntityMetaDataFactory entityMetaFactory;
	private final AttributeMetaDataFactory attrMetaFactory;
	private final String sheetName;
	private List<CellProcessor> cellProcessors;
	private EntityMetaData entityMetaData;
	private Character separator = null;

	public CsvRepository(String file, EntityMetaDataFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory)
	{
		this(new File(file), entityMetaFactory, attrMetaFactory, null);
	}

	public CsvRepository(File file, EntityMetaDataFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors, Character separator)
	{
		this(file, entityMetaFactory, attrMetaFactory, StringUtils.stripFilenameExtension(file.getName()), null);
		this.separator = separator;
	}

	public CsvRepository(File file, EntityMetaDataFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory,
			@Nullable List<CellProcessor> cellProcessors)
	{
		this(file, entityMetaFactory, attrMetaFactory, StringUtils.stripFilenameExtension(file.getName()), null);
	}

	public CsvRepository(File file, EntityMetaDataFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory,
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
