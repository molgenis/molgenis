package org.molgenis.dataexplorer.download;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Writable;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class DataExplorerDownloadHandler
{
	private DataService dataService;

	@Autowired
	public DataExplorerDownloadHandler(DataService dataService)
	{
		this.dataService = dataService;
	}

	public void writeToExcel(DataRequest dataRequest, OutputStream outputStream) throws IOException
	{
		WritableFactory writableFactory = new ExcelWriter(outputStream, FileFormat.XLSX);
		String entityName = dataRequest.getEntityName();

		QueryImpl query = dataRequest.getQuery();
		Writable writable = null;
		try
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			final Set<String> attributeNames = new LinkedHashSet<String>(dataRequest.getAttributeNames());
			Iterable<AttributeMetaData> attributes = filter(entityMetaData.getAtomicAttributes(),
					attributeMetaData -> attributeNames.contains(attributeMetaData.getName()));

			switch (dataRequest.getColNames())
			{
				case ATTRIBUTE_LABELS:
					writable = writableFactory.createWritable(entityName, Lists.newArrayList(attributeNames));
					break;
				case ATTRIBUTE_NAMES:
					List<String> attributeNamesList = newArrayList(transform(attributes, AttributeMetaData::getName));
					writable = writableFactory.createWritable(entityName, attributeNamesList);
					break;
			}
			writable.add(dataService.findAll(entityName, query));
			writable.close();
		}
		finally
		{
			writableFactory.close();
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
		String entityName = dataRequest.getEntityName();

		try
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			final Set<String> attributeNames = new HashSet<String>(dataRequest.getAttributeNames());
			Iterable<AttributeMetaData> attributes = filter(entityMetaData.getAtomicAttributes(),
					attributeMetaData -> attributeNames.contains(attributeMetaData.getName()));

			switch (dataRequest.getColNames())
			{
				case ATTRIBUTE_LABELS:
					csvWriter.writeAttributes(attributes);
					break;
				case ATTRIBUTE_NAMES:
					csvWriter.writeAttributeNames(transform(attributes, AttributeMetaData::getName));
					break;
			}

			QueryImpl query = dataRequest.getQuery();
			csvWriter.add(dataService.findAll(entityName, query));
		}
		finally
		{
			csvWriter.close();
		}
	}
}
