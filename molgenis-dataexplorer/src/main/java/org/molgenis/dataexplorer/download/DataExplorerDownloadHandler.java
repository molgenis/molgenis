package org.molgenis.dataexplorer.download;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractWritable.AttributeWriteMode;
import org.molgenis.data.support.AbstractWritable.EntityWriteMode;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Objects.requireNonNull;

public class DataExplorerDownloadHandler
{
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

		QueryImpl<Entity> query = dataRequest.getQuery();
		ExcelSheetWriter excelSheetWriter = null;
		try (ExcelWriter excelWriter = new ExcelWriter(outputStream, attrMetaFactory, FileFormat.XLSX))
		{
			EntityType entityType = dataService.getEntityType(entityTypeId);
			final Set<String> attributeNames = new LinkedHashSet<>(dataRequest.getAttributeNames());
			Iterable<Attribute> attributes = filter(entityType.getAtomicAttributes(),
					attribute -> attributeNames.contains(attribute.getName()));

			switch (dataRequest.getColNames())
			{
				case ATTRIBUTE_LABELS:
					excelSheetWriter = excelWriter.createWritable(entityTypeId, attributes,
							AttributeWriteMode.ATTRIBUTE_LABELS);
					break;
				case ATTRIBUTE_NAMES:
					excelSheetWriter = excelWriter.createWritable(entityTypeId, attributes,
							AttributeWriteMode.ATTRIBUTE_NAMES);
					break;
			}
			switch (dataRequest.getEntityValues())
			{
				case ENTITY_IDS:
					excelSheetWriter.setEntityWriteMode(EntityWriteMode.ENTITY_IDS);
					break;
				case ENTITY_LABELS:
					excelSheetWriter.setEntityWriteMode(EntityWriteMode.ENTITY_LABELS);
					break;
				default:
					break;
			}

			excelSheetWriter.add(dataService.findAll(entityTypeId, query));
			excelSheetWriter.close();
		}
	}

	public void writeToCsv(DataRequest request, OutputStream outputStream, char separator) throws IOException
	{
		writeToCsv(request, outputStream, separator, false);
	}

	public void writeToCsv(DataRequest dataRequest, OutputStream outputStream, char separator, boolean noQuotes)
			throws IOException
	{
		CsvWriter csvWriter = new CsvWriter(outputStream, separator, noQuotes);
		switch (dataRequest.getEntityValues())
		{
			case ENTITY_IDS:
				csvWriter.setEntityWriteMode(EntityWriteMode.ENTITY_IDS);
				break;
			case ENTITY_LABELS:
				csvWriter.setEntityWriteMode(EntityWriteMode.ENTITY_LABELS);
				break;
			default:
				break;
		}
		String entityTypeId = dataRequest.getEntityName();

		try
		{
			EntityType entityType = dataService.getEntityType(entityTypeId);
			final Set<String> attributeNames = new HashSet<>(dataRequest.getAttributeNames());
			Iterable<Attribute> attributes = filter(entityType.getAtomicAttributes(),
					attribute -> attributeNames.contains(attribute.getName()));

			switch (dataRequest.getColNames())
			{
				case ATTRIBUTE_LABELS:
					csvWriter.writeAttributes(attributes);
					break;
				case ATTRIBUTE_NAMES:
					csvWriter.writeAttributeNames(transform(attributes, Attribute::getName));
					break;
			}

			QueryImpl<Entity> query = dataRequest.getQuery();
			csvWriter.add(dataService.findAll(entityTypeId, query));
		}
		finally
		{
			csvWriter.close();
		}
	}
}
