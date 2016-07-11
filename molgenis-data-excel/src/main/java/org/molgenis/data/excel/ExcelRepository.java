package org.molgenis.data.excel;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;

/**
 * ExcelSheet {@link org.molgenis.data.Repository} implementation
 * <p>
 * It is assumed that the first row of the sheet is the header row.
 * <p>
 * All attributes will be of the string type. The cell values are converted to string.
 * <p>
 * The url of this Repository is defined as excel://${filename}/${sheetname}
 */
public class ExcelRepository extends AbstractRepository
{
	private final Sheet sheet;
	private final EntityMetaDataFactory entityMetaFactory;
	private final AttributeMetaDataFactory attrMetaFactory;

	/**
	 * process cells after reading
	 */
	private List<CellProcessor> cellProcessors;
	/**
	 * column names index
	 */
	private Map<String, Integer> colNamesMap;
	private EntityMetaData entityMetaData;

	public ExcelRepository(String fileName, Sheet sheet, EntityMetaDataFactory entityMetaFactory,
			AttributeMetaDataFactory attrMetaFactory)
	{
		this(fileName, sheet, entityMetaFactory, attrMetaFactory, null);
	}

	public ExcelRepository(String fileName, Sheet sheet, EntityMetaDataFactory entityMetaFactory,
			AttributeMetaDataFactory attrMetaFactory, List<CellProcessor> cellProcessors)
	{
		this.sheet = requireNonNull(sheet);
		if (sheet.getNumMergedRegions() > 0)
		{
			throw new MolgenisDataException(
					format("Sheet [%s] contains merged regions which is not supported", sheet.getSheetName()));
		}
		this.entityMetaFactory = requireNonNull(entityMetaFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.cellProcessors = cellProcessors;
	}

	public int getNrRows()
	{
		return sheet.getLastRowNum() + 1; // getLastRowNum is 0-based
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<Row> it = sheet.iterator();
		if (!it.hasNext()) return Collections.<Entity>emptyList().iterator();

		// create column header index once and reuse
		Row headerRow = it.next();
		if (colNamesMap == null)
		{
			colNamesMap = toColNamesMap(headerRow);
		}

		if (!it.hasNext()) return Collections.<Entity>emptyList().iterator();

		return new Iterator<Entity>()
		{
			ExcelEntity next = null;

			@Override
			public boolean hasNext()
			{
				// iterator skips empty lines.
				if (it.hasNext() && next == null)
				{
					ExcelEntity entity = new ExcelEntity(it.next(), colNamesMap, cellProcessors, getEntityMetaData());

					// check if there is any column containing a value
					for (String name : entity.getAttributeNames())
					{
						if (StringUtils.isNotEmpty(entity.getString(name)))
						{
							next = entity;
							break;
						}
					}
					// next line not empty?
					if (next == null)
					{
						hasNext();
					}
				}
				return next != null;
			}

			@Override
			public ExcelEntity next()
			{
				hasNext();
				ExcelEntity result = next;
				next = null;
				return result;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			EntityMetaData entityMetaData = entityMetaFactory.create().setName(sheet.getSheetName());

			if (colNamesMap == null)
			{
				Iterator<Row> it = sheet.iterator();
				if (it.hasNext())
				{
					// First row contains the headers
					colNamesMap = toColNamesMap(it.next());
				}
			}

			if (colNamesMap != null)
			{
				for (String colName : colNamesMap.keySet())
				{
					entityMetaData.addAttribute(attrMetaFactory.create().setName(colName).setDataType(STRING));
				}
			}
			this.entityMetaData = entityMetaData;
		}

		return entityMetaData;
	}

	private Map<String, Integer> toColNamesMap(Row headerRow)
	{
		if (headerRow == null) return null;

		Map<String, Integer> columnIdx = new LinkedCaseInsensitiveMap<>();
		int i = 0;
		for (Iterator<Cell> it = headerRow.cellIterator(); it.hasNext(); )
		{
			try
			{
				String header = AbstractCellProcessor.processCell(ExcelUtils.toValue(it.next()), true, cellProcessors);
				if (null != header) columnIdx.put(header, i++);
			}
			catch (final IllegalStateException ex)
			{
				final int row = headerRow.getRowNum();
				final String column = CellReference.convertNumToColString(i);
				throw new IllegalStateException("Invalid value at [" + sheet.getSheetName() + "] " + column + row + 1,
						ex);
			}
		}
		return columnIdx;
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
