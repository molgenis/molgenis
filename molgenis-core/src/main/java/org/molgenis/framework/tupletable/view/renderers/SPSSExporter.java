package org.molgenis.framework.tupletable.view.renderers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.CategoricalType;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;

/**
 * 
 * @author Daan Reid
 * 
 *         Exporter that writes to two streams; one of comma-separated values,
 *         and one SPSS script file to read them.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "Always use \n for newlines")
public class SPSSExporter extends CsvExporter
{
	public SPSSExporter(TupleTable matrix)
	{
		super(matrix);
	}

	public void export(OutputStream csvOs, OutputStream spssOs, String csvFileName) throws IOException, TableException
	{
		super.export(csvOs);
		writeSPSFile(spssOs, csvFileName);
	}

	private void writeSPSFile(OutputStream spssOs, String csvFileName) throws TableException,
			UnsupportedEncodingException
	{
		BufferedWriter spsWriter = new BufferedWriter(new OutputStreamWriter(spssOs, "UTF8"));

		List<Field> columns = tupleTable.getColumns();
		StringWriter valLabels = new StringWriter();
		StringWriter colNames = new StringWriter();
		List<Field> categoricalFields = new ArrayList<Field>();

		// write variable definitions
		for (Field field : columns)
		{
			FieldType fieldType = field.getType();
			if (fieldType.getEnumType() == FieldTypeEnum.CATEGORICAL)
			{
				categoricalFields.add(field);
			}
			colNames.write(String.format("%s %s ", field.getName(), colTypeToSPSSType(fieldType.getEnumType())));
		}

		// add category labels to variables if appropriate
		for (Field field : categoricalFields)
		{
			Map<String, String> categoryMapping = ((CategoricalType) field.getType()).getCategoryMapping();
			valLabels.write(String.format("ADD VALUE LABELS %s ", field.getName()));
			for (Entry<String, String> entry : categoryMapping.entrySet())
			{
				valLabels.write(String.format(" %s \'%s\' ", entry.getKey(), entry.getValue()));
			}
			valLabels.write("\n");
		}

		try
		{
			String spsFormatStr = String.format("GET DATA\n" + "/type = txt\n" + "/file = \'%s\'\n "
					+ "/qualifier = \'\"\'\n" + "/delimiters = \'\\t\'\n" + "/firstcase = 2\n" + "/variables = %s.\n"
					+ "/execute.", csvFileName, colNames.toString() + valLabels.toString());

			spsWriter.write(spsFormatStr);
			spsWriter.flush();
			spsWriter.close();
		}
		catch (IOException e)
		{
			throw new TableException(e);
		}
	}

	private static String colTypeToSPSSType(FieldTypeEnum columnType)
	{
		switch (columnType)
		{
			case CATEGORICAL:
				return "F";
			case DATE:
				return "ADATE";
			case DATE_TIME:
				return "ADATE";
			case DECIMAL:
				return "F";
			case INT:
				return "F";
			case STRING:
				return "A";
			default:
				throw new IllegalArgumentException("Unknown field type: " + columnType);
		}
	}
}
