package org.molgenis.lifelines.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.molgenis.io.processor.TrimProcessor;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

/**
 * Convert a transformed LifeLines data dictionary to Observ-OMX format
 * 
 * TODO support multiple sheets
 */
public class LifeLinesDataDictionarytoOMXConvertor
{
	private static final Logger logger = Logger.getLogger(LifeLinesDataDictionarytoOMXConvertor.class);

	private static final String COL_INCLUDE = "ja/nee";
	private static final String COL_GROUP = "Group";
	private static final String COL_CODE = "Code";
	private static final String COL_DISPLAY_NAME = "Dysplay name";
	private static final String COL_DESCRIPTION = "EN Description";
	private static final String COL_SECTION = "Section";
	private static final String COL_SUB_SECTION = "Sub-section";
	private static final String COL_SUB_SECTION2 = "Sub-section 2";
	private static final String COL_COHORT = "Cohort";
	private static final String COL_TIME = "Time";
	private static final String COL_TYPE = "Type";
	private static final String COL_VALUE = "Value";
	private static final String COL_VALUE_DESCRIPTION = "EN Value Description";

	private static final String ENTITY_IDENTIFIER = "identifier";
	private static final String ENTITY_NAME = "name";
	private static final String ENTITY_DESCRIPTION = "description";
	private static final String FEATURE_DATATYPE = "datatype";
	private static final String VALUE_CODE = "valuecode";
	private static final String IS_MISSING = "ismissing";
	private static final String FEATURES_IDENTIFIER = "features_identifier";
	private static final String SUB_PROTOCOLS_IDENTIFIER = "subprotocols_identifier";
	private static final String OBSERVABLE_FEATURE_IDENTIFIER = "observableFeature_identifier";
	private static final String PROTOCOLUSED_IDENTIFIER = "protocolused_identifier";

	private static final String SHEET_FEATURE = "observablefeature";
	private static final String SHEET_PROTOCOL = "protocol";
	private static final String SHEET_CATEGORY = "category";
	private static final String SHEET_DATASET = "dataset";

	private static final Map<String, Integer> HEADER_PROTOCOL;
	private static final Map<String, Integer> HEADER_FEATURE;
	private static final Map<String, Integer> HEADER_CATEGORY;
	private static final Map<String, Integer> HEADER_DATASET;

	static
	{
		HEADER_PROTOCOL = new LinkedHashMap<String, Integer>();
		HEADER_PROTOCOL.put(ENTITY_IDENTIFIER, 0);
		HEADER_PROTOCOL.put(ENTITY_NAME, 1);
		HEADER_PROTOCOL.put(ENTITY_DESCRIPTION, 2);
		HEADER_PROTOCOL.put(FEATURES_IDENTIFIER, 3);
		HEADER_PROTOCOL.put(SUB_PROTOCOLS_IDENTIFIER, 4);

		HEADER_FEATURE = new LinkedHashMap<String, Integer>();
		HEADER_FEATURE.put(ENTITY_IDENTIFIER, 0);
		HEADER_FEATURE.put(ENTITY_NAME, 1);
		HEADER_FEATURE.put(ENTITY_DESCRIPTION, 2);
		HEADER_FEATURE.put(FEATURE_DATATYPE, 3);

		HEADER_CATEGORY = new LinkedHashMap<String, Integer>();
		HEADER_CATEGORY.put(ENTITY_IDENTIFIER, 0);
		HEADER_CATEGORY.put(ENTITY_NAME, 1);
		HEADER_CATEGORY.put(ENTITY_DESCRIPTION, 2);
		HEADER_CATEGORY.put(VALUE_CODE, 3);
		HEADER_CATEGORY.put(IS_MISSING, 4);
		HEADER_CATEGORY.put(OBSERVABLE_FEATURE_IDENTIFIER, 5);

		HEADER_DATASET = new LinkedHashMap<String, Integer>();
		HEADER_DATASET.put(ENTITY_IDENTIFIER, 0);
		HEADER_DATASET.put(ENTITY_NAME, 1);
		HEADER_DATASET.put(PROTOCOLUSED_IDENTIFIER, 2);
	}

	public void convert(InputStream in, OutputStream out) throws IOException
	{
		ExcelReader excelReader = new ExcelReader(in);
		excelReader.addCellProcessor(new TrimProcessor(false, true));
		ExcelWriter excelWriter = new ExcelWriter(out);
		excelWriter.addCellProcessor(new LowerCaseProcessor(true, false));

		try
		{
			// create entity sheets
			Map<String, TupleWriter> sheetMap = createOMXSheets(excelWriter);

			Protocol rootProtocol = new Protocol("protocol_root", "Cohort");
			Map<String, Protocol> protocolMap = new LinkedHashMap<String, Protocol>();
			protocolMap.put(null, rootProtocol);

			// write data
			for (String tableName : excelReader.getTableNames())
			{
				logger.debug("converting sheet: " + tableName);
				convertSheet(excelReader.getTupleReader(tableName), sheetMap, protocolMap, rootProtocol);
			}

			// write protocols
			TupleWriter protocolWriter = sheetMap.get(SHEET_PROTOCOL);
			for (Protocol protocol : protocolMap.values())
			{
				KeyValueTuple row = new KeyValueTuple();
				row.set(ENTITY_IDENTIFIER, protocol.getIdentifier());
				row.set(ENTITY_NAME, protocol.getName());
				row.set(FEATURES_IDENTIFIER, StringUtils.join(protocol.getFeatureIdentifiers(), ','));
				row.set(SUB_PROTOCOLS_IDENTIFIER, StringUtils.join(protocol.getSubProtocols(), ','));
				protocolWriter.write(row);
			}

			TupleWriter dataSetWriter = sheetMap.get(SHEET_DATASET);
			KeyValueTuple row = new KeyValueTuple();
			row.set(ENTITY_IDENTIFIER, "dataset_lifelines");
			row.set(ENTITY_NAME, "LifeLines");
			row.set(PROTOCOLUSED_IDENTIFIER, rootProtocol.getIdentifier());
			dataSetWriter.write(row);
		}
		finally
		{
			try
			{
				excelReader.close();
			}
			catch (IOException e)
			{
			}
			try
			{
				excelWriter.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private Map<String, TupleWriter> createOMXSheets(ExcelWriter excelWriter) throws IOException
	{
		Map<String, TupleWriter> sheetMap = new HashMap<String, TupleWriter>();

		TupleWriter protocolWriter = excelWriter.createTupleWriter(SHEET_PROTOCOL);
		protocolWriter.writeColNames(HEADER_PROTOCOL.keySet());
		sheetMap.put(SHEET_PROTOCOL, protocolWriter);

		TupleWriter featureWriter = excelWriter.createTupleWriter(SHEET_FEATURE);
		featureWriter.writeColNames(HEADER_FEATURE.keySet());
		sheetMap.put(SHEET_FEATURE, featureWriter);

		TupleWriter categoryWriter = excelWriter.createTupleWriter(SHEET_CATEGORY);
		categoryWriter.writeColNames(HEADER_CATEGORY.keySet());
		sheetMap.put(SHEET_CATEGORY, categoryWriter);

		TupleWriter dataSetWriter = excelWriter.createTupleWriter(SHEET_DATASET);
		dataSetWriter.writeColNames(HEADER_DATASET.keySet());
		sheetMap.put(SHEET_DATASET, dataSetWriter);

		return sheetMap;
	}

	private void convertSheet(TupleReader sheet, Map<String, TupleWriter> sheetMap, Map<String, Protocol> protocolMap,
			Protocol rootProtocol) throws IOException
	{
		List<String> featureIdentifiers = new ArrayList<String>();

		String lastFeatureIdentifier = null;

		int rownr = 1; // 0-based, skip header
		for (Tuple row : sheet)
		{
			if (!includeRow(row))
			{
				logger.debug("skipping row " + rownr++);
				continue;
			}

			String group = row.getString(COL_GROUP);
			if (group == null || group.isEmpty())
			{
				if (lastFeatureIdentifier == null)
				{
					logger.debug("skipping non-feature row " + rownr++);
					continue;
				}
				else
				{
					// value row for the last detected feature
					String value = row.getString(COL_VALUE);
					String description = row.getString(COL_VALUE_DESCRIPTION);
					if (value != null && (description == null || description.isEmpty())) throw new IOException(
							"missing translation for value '" + value + "'");

					if (value == null || value.isEmpty() || description == null || description.isEmpty())
					{
						logger.info("skipping non-feature row " + rownr++);
						continue;
					}
					else
					{
						KeyValueTuple categoryMap = new KeyValueTuple();
						categoryMap.set(ENTITY_IDENTIFIER, "category_" + lastFeatureIdentifier + "_" + value);
						categoryMap.set(ENTITY_NAME, description);
						categoryMap.set(VALUE_CODE, value);
						categoryMap.set(OBSERVABLE_FEATURE_IDENTIFIER, lastFeatureIdentifier);
						sheetMap.get(SHEET_CATEGORY).write(categoryMap);

						++rownr;
						continue;
					}
				}
			}
			String code = row.getString(COL_CODE);

			logger.debug("converting row: " + (rownr + 1) + " - " + row.getString(COL_GROUP) + ", "
					+ row.getString(COL_CODE));

			// add feature
			String featureIdentifier = group + '.' + code;
			String featureName = row.getString(COL_DISPLAY_NAME);
			String featureDescription = row.getString(COL_DESCRIPTION);
			if (featureName == null || featureName.isEmpty()) throw new IOException();
			if (featureDescription == null || featureDescription.isEmpty()) throw new IOException();
			// featureName = "UNKNOWN FEATURE NAME";
			KeyValueTuple featureMap = new KeyValueTuple();
			featureMap.set(ENTITY_IDENTIFIER, featureIdentifier);
			featureMap.set(ENTITY_NAME, featureName);
			featureMap.set(ENTITY_DESCRIPTION, featureDescription);
			if (row.getString(COL_VALUE) != null && !row.getString(COL_VALUE).isEmpty()) featureMap.set(
					FEATURE_DATATYPE, "categorical");
			sheetMap.get(SHEET_FEATURE).write(featureMap);
			featureIdentifiers.add(featureIdentifier);
			lastFeatureIdentifier = featureIdentifier;

			// add category
			String categoryValue = row.getString(COL_VALUE);
			String categoryDescription = row.getString(COL_VALUE_DESCRIPTION);
			if (categoryValue != null && (categoryDescription == null || categoryDescription.isEmpty())) throw new IOException(
					"missing translation for value '" + categoryValue + "'");

			if (categoryValue != null && categoryDescription != null)
			{
				KeyValueTuple categoryMap = new KeyValueTuple();
				categoryMap.set(ENTITY_IDENTIFIER, "category_" + lastFeatureIdentifier + "_" + categoryValue);
				categoryMap.set(ENTITY_NAME, categoryDescription);
				categoryMap.set(VALUE_CODE, categoryValue);
				categoryMap.set(OBSERVABLE_FEATURE_IDENTIFIER, lastFeatureIdentifier);
				sheetMap.get(SHEET_CATEGORY).write(categoryMap);
			}

			// get protocol
			String cohort = row.getString(COL_COHORT);
			if (cohort == null) throw new IOException("missing cohort");
			String time = row.getString(COL_TIME);
			String type = row.getString(COL_TYPE);
			String section = row.getString(COL_SECTION);
			String subSection = row.getString(COL_SUB_SECTION);
			String subSubSection = row.getString(COL_SUB_SECTION2);

			String protocolIdentifier = cohort.replace(",", "");
			String protocolName = cohort;

			// TODO use recursive function
			Protocol protocol = rootProtocol;
			Protocol subProtocol = protocolMap.get(protocolIdentifier);
			if (subProtocol == null)
			{
				subProtocol = new Protocol(protocolIdentifier, protocolName);
				protocolMap.put(protocolIdentifier, subProtocol);
				protocol.addSubProtocol(subProtocol);
			}
			if (time == null || time.isEmpty()) subProtocol.addFeatureIdentifier(featureIdentifier);
			else
			{
				protocolIdentifier = protocolIdentifier + '.' + time.replace(",", "");
				protocolName = time;

				protocol = subProtocol;
				subProtocol = protocolMap.get(protocolIdentifier);
				if (subProtocol == null)
				{
					subProtocol = new Protocol(protocolIdentifier, protocolName);
					protocolMap.put(protocolIdentifier, subProtocol);
					protocol.addSubProtocol(subProtocol);
				}

				if (type == null || type.isEmpty()) subProtocol.addFeatureIdentifier(featureIdentifier);
				else
				{
					protocolIdentifier = protocolIdentifier + '.' + type.replace(",", "");
					protocolName = type;

					protocol = subProtocol;
					subProtocol = protocolMap.get(protocolIdentifier);
					if (subProtocol == null)
					{
						subProtocol = new Protocol(protocolIdentifier, protocolName);
						protocolMap.put(protocolIdentifier, subProtocol);
						protocol.addSubProtocol(subProtocol);
					}

					if (section == null || section.isEmpty()) subProtocol.addFeatureIdentifier(featureIdentifier);
					else
					{
						protocolIdentifier = protocolIdentifier + '.' + section.replace(",", "");
						protocolName = section;

						protocol = subProtocol;
						subProtocol = protocolMap.get(protocolIdentifier);
						if (subProtocol == null)
						{
							subProtocol = new Protocol(protocolIdentifier, protocolName);
							protocolMap.put(protocolIdentifier, subProtocol);
							protocol.addSubProtocol(subProtocol);
						}

						if (subSection == null || subSection.isEmpty()) subProtocol
								.addFeatureIdentifier(featureIdentifier);
						else
						{
							protocolIdentifier = protocolIdentifier + '.' + subSection.replace(",", "");
							protocolName = subSection;

							protocol = subProtocol;
							subProtocol = protocolMap.get(protocolIdentifier);
							if (subProtocol == null)
							{
								subProtocol = new Protocol(protocolIdentifier, protocolName);
								protocolMap.put(protocolIdentifier, subProtocol);
								protocol.addSubProtocol(subProtocol);
							}

							if (subSubSection == null || subSubSection.isEmpty()) subProtocol
									.addFeatureIdentifier(featureIdentifier);
							else
							{
								protocolIdentifier = protocolIdentifier + '.' + subSubSection.replace(",", "");
								protocolName = subSubSection;

								protocol = subProtocol;
								subProtocol = protocolMap.get(protocolIdentifier);
								if (subProtocol == null)
								{
									subProtocol = new Protocol(protocolIdentifier, protocolName);
									protocolMap.put(protocolIdentifier, subProtocol);
									protocol.addSubProtocol(subProtocol);
								}

								// end of the line, add feature
								subProtocol.addFeatureIdentifier(featureIdentifier);
							}
						}
					}
				}
			}

			++rownr;
		}
	}

	private boolean includeRow(Tuple row)
	{
		String value = row.getString(COL_INCLUDE);
		return value != null ? !value.equals("x") : true;
	}

	private static class Protocol
	{
		private final String identifier;
		private final String name;
		private Set<Protocol> subProtocols;
		private Set<String> featureIdentifiers;

		public Protocol(String identifier, String name)
		{
			this.identifier = identifier;
			this.name = name;
		}

		public String getIdentifier()
		{
			return identifier;
		}

		public String getName()
		{
			return name;
		}

		public Set<Protocol> getSubProtocols()
		{
			return subProtocols != null ? subProtocols : Collections.<Protocol> emptySet();
		}

		private void addSubProtocol(Protocol subProtocol)
		{
			if (subProtocols == null) subProtocols = new LinkedHashSet<Protocol>();
			subProtocols.add(subProtocol);
		}

		public Set<String> getFeatureIdentifiers()
		{
			return featureIdentifiers != null ? featureIdentifiers : Collections.<String> emptySet();
		}

		public void addFeatureIdentifier(String featureIdentifier)
		{
			if (featureIdentifiers == null) featureIdentifiers = new LinkedHashSet<String>();
			featureIdentifiers.add(featureIdentifier);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Protocol other = (Protocol) obj;
			if (identifier == null)
			{
				if (other.identifier != null) return false;
			}
			else if (!identifier.equals(other.identifier)) return false;
			return true;
		}

		@Override
		public String toString()
		{
			return identifier;
		}
	}

	public static void main(String[] args) throws IOException
	{
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);

		if (args.length != 2)
		{
			System.err.println("usage: java " + LifeLinesDataDictionarytoOMXConvertor.class.getSimpleName()
					+ " inputfile outputfile");
			return;
		}

		File inFile = new File(args[0]);
		File outFile = new File(args[1]);
		// if (outFile.exists()) throw new IOException("file already exists: " +
		// outFile);

		FileInputStream fis = new FileInputStream(inFile);
		FileOutputStream fos = new FileOutputStream(outFile);
		new LifeLinesDataDictionarytoOMXConvertor().convert(fis, fos);
	}

}
