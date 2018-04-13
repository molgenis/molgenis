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
import org.molgenis.data.file.processor.AbstractCellProcessor;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;

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
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;

	/**
	 * process cells after reading
	 */
	private List<CellProcessor> cellProcessors;
	/**
	 * column names index
	 */
	private Map<String, Integer> colNamesMap;
	private EntityType entityType;

	public ExcelRepository(String fileName, Sheet sheet, EntityTypeFactory entityTypeFactory,
			AttributeFactory attrMetaFactory)
	{
		this(fileName, sheet, entityTypeFactory, attrMetaFactory, null);
	}

	public ExcelRepository(String fileName, Sheet sheet, EntityTypeFactory entityTypeFactory,
			AttributeFactory attrMetaFactory, List<CellProcessor> cellProcessors)
	{
		this.sheet = requireNonNull(sheet);
		if (sheet.getNumMergedRegions() > 0)
		{
			throw new MolgenisDataException(
					format("Sheet [%s] contains merged regions which is not supported", sheet.getSheetName()));
		}
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
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
				getAndUpdateNext();
				return next != null;
			}

			@Override
			public ExcelEntity next()
			{
				boolean hasNext = hasNext();
				if (!hasNext)
				{
					throw new NoSuchElementException();
				}
				ExcelEntity result = next;
				next = null;
				return result;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			private void getAndUpdateNext()
			{
				// iterator skips empty lines.
				if (it.hasNext() && next == null)
				{
					ExcelEntity entity = new ExcelEntity(it.next(), colNamesMap, cellProcessors, getEntityType());

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
						getAndUpdateNext();
					}
				}
			}
		};
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<>();
		cellProcessors.add(cellProcessor);
	}

	public EntityType getEntityType()
	{
		if (entityType == null)
		{
			String sheetName = sheet.getSheetName();
			EntityType entityType = entityTypeFactory.create(sheetName).setLabel(sheetName);

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
					entityType.addAttribute(attrMetaFactory.create().setName(colName).setDataType(STRING));
				}
			}
			this.entityType = entityType;
		}

		return entityType;
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
				if (header != null)
				{
					if (columnIdx.containsKey(header))
					{
						throw new MolgenisDataException(
								format("Duplicate column header '%s' in sheet '%s' not allowed", header,
										headerRow.getSheet().getSheetName()));
					}
					columnIdx.put(header, i++);
				}
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
