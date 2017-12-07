package org.molgenis.dataexplorer.download;

import com.google.common.collect.Streams;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractWritable.AttributeWriteMode;
import org.molgenis.data.support.AbstractWritable.EntityWriteMode;
import org.molgenis.dataexplorer.controller.DataRequest;
import org.molgenis.util.UnexpectedEnumException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DataExplorerDownloadHandler
{
	// Magic value: https://github.com/molgenis/molgenis/issues/6687
	private static final long MAX_EXCEL_CELLS = 500000L;
	private final DataService dataService;
	private final AttributeFactory attrMetaFactory;

	public DataExplorerDownloadHandler(DataService dataService, AttributeFactory attrMetaFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
	}

	public void writeToExcel(DataRequest dataRequest, OutputStream outputStream) throws IOException
	{
		String entityTypeId = dataRequest.getEntityName();
		List<Attribute> attributes = filterAttributes(dataRequest);
		checkNumberOfCells(dataRequest, entityTypeId, attributes.size());
		AttributeWriteMode attributeWriteMode = getAttributeWriteMode(dataRequest.getColNames());
		try (ExcelWriter excelWriter = new ExcelWriter(outputStream, attrMetaFactory, FileFormat.XLSX);
				ExcelSheetWriter excelSheetWriter = excelWriter.createWritable(entityTypeId, attributes,
						attributeWriteMode))
		{
			excelSheetWriter.setEntityWriteMode(getEntityWriteMode(dataRequest.getEntityValues()));
			excelSheetWriter.add(dataService.findAll(entityTypeId, dataRequest.getQuery()));
		}
	}

	private List<Attribute> filterAttributes(DataRequest dataRequest)
	{
		EntityType entityType = dataService.getEntityType(dataRequest.getEntityName());
		final Set<String> attributeNames = newHashSet(dataRequest.getAttributeNames());
		return Streams.stream(entityType.getAtomicAttributes())
					  .filter(attribute -> attributeNames.contains(attribute.getName()))
					  .collect(toList());
	}

	private void checkNumberOfCells(DataRequest dataRequest, String entityTypeId, int cols)
	{
		long rows = dataService.count(entityTypeId, dataRequest.getQuery());
		if (rows * cols >= MAX_EXCEL_CELLS)
		{
			throw new MolgenisDataException(String.format(
					"Total number of cells for this download exceeds the maximum of %s for .xlsx downloads, please use .csv instead",
					MAX_EXCEL_CELLS));
		}
	}

	public void writeToCsv(DataRequest request, OutputStream outputStream, char separator) throws IOException
	{
		writeToCsv(request, outputStream, separator, false);
	}

	public void writeToCsv(DataRequest dataRequest, OutputStream outputStream, char separator, boolean noQuotes)
			throws IOException
	{
		try (CsvWriter csvWriter = new CsvWriter(outputStream, separator, noQuotes))
		{
			csvWriter.setEntityWriteMode(getEntityWriteMode(dataRequest.getEntityValues()));
			String entityTypeId = dataRequest.getEntityName();
			writeCsvHeaders(dataRequest, csvWriter);
			csvWriter.add(dataService.findAll(entityTypeId, dataRequest.getQuery()));
		}
	}

	private void writeCsvHeaders(DataRequest dataRequest, CsvWriter csvWriter) throws IOException
	{
		List<Attribute> attributes = filterAttributes(dataRequest);
		switch (dataRequest.getColNames())
		{
			case ATTRIBUTE_LABELS:
				csvWriter.writeAttributes(attributes);
				break;
			case ATTRIBUTE_NAMES:
				csvWriter.writeAttributeNames(attributes.stream().map(Attribute::getName).collect(toList()));
				break;
			default:
				throw new UnexpectedEnumException(dataRequest.getColNames());
		}
	}

	private EntityWriteMode getEntityWriteMode(DataRequest.EntityValues entityValues)
	{
		switch (entityValues)
		{
			case ENTITY_IDS:
				return EntityWriteMode.ENTITY_IDS;
			case ENTITY_LABELS:
				return EntityWriteMode.ENTITY_LABELS;
			default:
				throw new UnexpectedEnumException(entityValues);
		}
	}

	private AttributeWriteMode getAttributeWriteMode(DataRequest.ColNames colNames)
	{
		switch (colNames)
		{
			case ATTRIBUTE_LABELS:
				return AttributeWriteMode.ATTRIBUTE_LABELS;
			case ATTRIBUTE_NAMES:
				return AttributeWriteMode.ATTRIBUTE_NAMES;
			default:
				throw new UnexpectedEnumException(colNames);
		}
	}
}
