package org.molgenis.gaf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.stereotype.Component;

@Component
public class GafListValidator
{
	private static final Logger logger = Logger.getLogger(GafListValidator.class);

	public static final String GAF_LIST_SETTINGS_PREFIX = "gafList.validator.";// RuntimeProperty.class.getSimpleName()
																				// +
																		// "_gafList.validator.";

	private static final List<String> COLUMNS;

	public static final String COL_INTERNAL_SAMPLE_ID;
	public static final String COL_LANE;
	public static final String COL_SEQUENCER;
	public static final String COL_SAMPLE;
	public static final String COL_SEQUENCING_START_DATE;
	public static final String COL_RUN;
	public static final String COL_FLOWCELL;
	public static final String COL_SEQ_TYPE;
	public static final String COL_BARCODE_1;
	public static final String End_Product_Concentration_nmol__l;
	public static final String COL_EXTERNAL_SAMPLE_ID;
	public static final String COL_PROJECT;
	public static final String COL_CONTACT;
	public static final String COL_SAMPLE_TYPE;
	public static final String COL_ARRAY_FILE;
	public static final String COL_ARRAY_ID;
	public static final String COL_CAPTURING_KIT;
	public static final String COL_PREP_KIT;
	public static final String COL_GAF_QC_NAME;
	public static final String COL_GAF_QC_DATE;
	public static final String COL_GAF_QC_STATUS;
	public static final String COL_GCC_ANALYSIS;
	public static final String COL_PLATES_IN_STOCK__DNA_SEQUENCING;
	public static final String COL_REJECTED_FOR_PROCESSING;
	public static final String COL_PLATES_IN_STOCK__DNA_SEQUENCING__WHOLE_GENOME_CAPTURING;
	public static final String COL_BARCODE_2;
	public static final String COL_BARCODE;
	public static final String COL_BARCODE_TYPE;
	
	static final String BARCODE_NONE = "None";

	static
	{
		COL_INTERNAL_SAMPLE_ID = "internalSampleID";
		COL_LANE = "lane";
		COL_SEQUENCER = "sequencer";
		COL_SAMPLE = "Sample";
		COL_SEQUENCING_START_DATE = "sequencingStartDate";
		COL_RUN = "run";
		COL_FLOWCELL = "flowcell";
		COL_SEQ_TYPE = "seqType";
		COL_BARCODE_1 = "Barcode 1";
		End_Product_Concentration_nmol__l = "End Product Concentration nmol/l";
		COL_EXTERNAL_SAMPLE_ID = "externalSampleID";
		COL_PROJECT = "project";
		COL_CONTACT = "contact";
		COL_SAMPLE_TYPE = "Sample Type";
		COL_ARRAY_FILE = "arrayFile";
		COL_ARRAY_ID = "arrayID";
		COL_CAPTURING_KIT = "capturingKit";
		COL_PREP_KIT = "prepKit";
		COL_GAF_QC_NAME = "GAF_QC_Name";
		COL_GAF_QC_DATE = "GAF_QC_Date";
		COL_GAF_QC_STATUS = "GAF_QC_Status";
		COL_GCC_ANALYSIS = "GCC_Analysis";
		COL_PLATES_IN_STOCK__DNA_SEQUENCING = "Plates in stock - RNA sequencing";
		COL_REJECTED_FOR_PROCESSING = "Rejected for processing";
		COL_PLATES_IN_STOCK__DNA_SEQUENCING__WHOLE_GENOME_CAPTURING = "Plates in stock - DNA sequencing (whole genome / capturing)";
		COL_BARCODE_2 = "Barcode 2";
		COL_BARCODE = "barcode";
		COL_BARCODE_TYPE = "barcodeType";

		COLUMNS = Arrays.asList(COL_INTERNAL_SAMPLE_ID, COL_EXTERNAL_SAMPLE_ID, COL_PROJECT, COL_SEQUENCER,
				COL_CONTACT, COL_SEQUENCING_START_DATE, COL_RUN, COL_FLOWCELL, COL_LANE, COL_BARCODE_1, COL_BARCODE_2,
				COL_SEQ_TYPE, COL_PREP_KIT, COL_CAPTURING_KIT, COL_ARRAY_FILE, COL_ARRAY_ID, COL_GAF_QC_NAME,
				COL_GAF_QC_DATE, COL_GAF_QC_STATUS, COL_GCC_ANALYSIS, COL_BARCODE, COL_BARCODE_TYPE, COL_SAMPLE,
				COL_SAMPLE_TYPE, COL_PLATES_IN_STOCK__DNA_SEQUENCING, COL_REJECTED_FOR_PROCESSING,
				COL_PLATES_IN_STOCK__DNA_SEQUENCING__WHOLE_GENOME_CAPTURING);
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	public GafListValidationReport validate(Repository repository) throws IOException
	{
		GafListValidationReport report = new GafListValidationReport();

		// retrieve validation patterns
		Map<String, Pattern> patternMap = new HashMap<String, Pattern>();
		for (String colName : COLUMNS)
		{
			String pattern = molgenisSettings.getProperty(GAF_LIST_SETTINGS_PREFIX + colName);
			if (pattern != null) patternMap.put(colName, Pattern.compile(pattern));
		}

		// retrieve look up lists
		Map<String, Set<String>> lookupLists = new HashMap<String, Set<String>>();
		for (String colName : COLUMNS)
		{
			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME,
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, colName), ObservableFeature.class);

			Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
					new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature), Category.class);
			if (categories != null)
			{
				Set<String> lookupList = new HashSet<String>();
				for (Category category : categories)
					lookupList.add(category.getValueCode());
				lookupLists.put(colName, lookupList);
			}
		}

		List<Entity> entities = new ArrayList<Entity>();
		Iterable<AttributeMetaData> attributes = repository.getEntityMetaData().getAttributes();
		
		Iterator<Entity> it = repository.iterator();
		while (it.hasNext())
		{
			Entity entity = it.next();
			String runId = entity.getString(COL_RUN);
			report.getAllRunIds().add(runId);
			entities.add(entity);
		}

		validateCellValues(entities, attributes, patternMap, lookupLists, report);
		validateInternalSampleIdIncremental(entities, report);
		validateRun(entities, report);
		report.populateStatusImportedRuns();
		
		return report;
	}

	private void validateCellValues(List<Entity> entities, Iterable<AttributeMetaData> attributes,
			Map<String, Pattern> patternMap, Map<String, Set<String>> lookupLists, GafListValidationReport report)
	{
		int row = 2;
		for (Entity entity : entities)
		{
			// skip empty rows
			if (isEmptyRow(entity)) continue;

			// validate individual cells
			String runId = entity.getString(COL_RUN);

			for (AttributeMetaData attributeMetaData : attributes)
			{
				String attributeName = attributeMetaData.getName().trim();
				if (attributeName.isEmpty()) continue;
				String value = entity.getString(attributeName);

				// validate cell
				validateCell(runId, row, attributeName, value, patternMap, lookupLists, report);
			}

			row++;
		}
	}

	/**
	 * Validate that internal sample id is incremental (and thus unique)
	 * 
	 * @param repository
	 * @param report
	 */
	private void validateInternalSampleIdIncremental(List<Entity> entities, GafListValidationReport report)
	{
		int row = 2;
		for (Entity entity : entities)
		{
			// skip empty rows
			if (isEmptyRow(entity)) continue;
			String runId = entity.getString(COL_RUN);
			Integer internalSampleId = null;
			try{
				internalSampleId = entity.getInt(COL_INTERNAL_SAMPLE_ID);
				if (internalSampleId == null)
				{
					// internal sample id can not be null
					report.addEntry(runId, new GafListValidationError(row, COL_INTERNAL_SAMPLE_ID, null,
							"value undefined"));
				}
			}
			catch (ConversionFailedException cfe)
			{
				new GafListValidationError(row, COL_INTERNAL_SAMPLE_ID, null,
						"value is not formatted correctly it need to be a number");
			}
			
			row++;
		}
	}

	/**
	 * Validate that: - all rows related to a run have the same sequencer type - all rows related to a run have the same
	 * sequence date - all lanes in a run have the same prepkit and capturingkit - all lanes in a run have the same
	 * barcodetype
	 * 
	 * @param repository
	 * @param lookupLists
	 * @param report
	 */
	private void validateRun(List<Entity> entities, GafListValidationReport report)
	{
		// group rows by run
		Map<String, List<EntityRowPair>> runMap = new HashMap<String, List<EntityRowPair>>();

		int row = 2;
		for (Entity entity : entities)
		{
			// skip empty rows
			if (isEmptyRow(entity)) continue;

			String runId = entity.getString(COL_RUN);
			if (runId != null)
			{
				List<EntityRowPair> entityRowPairs = runMap.get(runId);
				if (entityRowPairs == null)
				{
					entityRowPairs = new ArrayList<EntityRowPair>();
					runMap.put(runId, entityRowPairs);
				}
				entityRowPairs.add(new EntityRowPair(entity, row));
			}
			row++;
		}

		for (Map.Entry<String, List<EntityRowPair>> entry : runMap.entrySet())
		{
			String runId = entry.getKey();
			List<EntityRowPair> entityRowPairs = entry.getValue();

			// all rows related to a run must have the same sequencer type
			String runSeqType = null;
			for (EntityRowPair entityRowPair : entityRowPairs)
			{
				String seqType = entityRowPair.getEntity().getString(COL_SEQ_TYPE);
				if (runSeqType == null) runSeqType = seqType;
				else if (!runSeqType.equals(seqType))
				{
					report.addEntry(runId, new GafListValidationError(entityRowPair.getRow(), COL_SEQ_TYPE, seqType,
							"run has different " + COL_SEQ_TYPE));
				}
			}

			// all rows related to a run have the same sequence date
			String runSeqStartDate = null;
			for (EntityRowPair entityRowPair : entityRowPairs)
			{
				String seqStartDate = entityRowPair.getEntity().getString(COL_SEQUENCING_START_DATE);
				if (runSeqStartDate == null) runSeqStartDate = seqStartDate;
				else if (!runSeqStartDate.equals(seqStartDate))
				{
					report.addEntry(runId, new GafListValidationError(entityRowPair.getRow(),
							COL_SEQUENCING_START_DATE, seqStartDate, "run has different " + COL_SEQUENCING_START_DATE));
				}
			}

			// group run rows by lane
			Map<String, List<EntityRowPair>> laneMap = new HashMap<String, List<EntityRowPair>>();
			for (EntityRowPair entityRowPair : entityRowPairs)
			{
				Entity laneEntity = entityRowPair.getEntity();
				String lane = laneEntity.getString(COL_LANE);
				if (lane != null)
				{
					List<EntityRowPair> laneEntityRowPairs = laneMap.get(lane);
					if (laneEntityRowPairs == null)
					{
						laneEntityRowPairs = new ArrayList<EntityRowPair>();
						laneMap.put(lane, laneEntityRowPairs);
					}
					laneEntityRowPairs.add(new EntityRowPair(laneEntity, entityRowPair.getRow()));
				}
			}

			for (Map.Entry<String, List<EntityRowPair>> laneEntry : laneMap.entrySet())
			{
				List<EntityRowPair> laneEntityRowPairs = laneEntry.getValue();

				// all lanes in a run have the same barcodetype
				String laneBarcodeType = null;
				for (EntityRowPair laneEntityRowPair : laneEntityRowPairs)
				{
					Entity laneEntity = laneEntityRowPair.getEntity();
					String barcodeType = laneEntity.getString(COL_BARCODE_TYPE);
					if (laneBarcodeType == null) laneBarcodeType = barcodeType;
					else if (!laneBarcodeType.equals(barcodeType))
					{
						report.addEntry(runId, new GafListValidationError(laneEntityRowPair.getRow(), COL_BARCODE_TYPE,
								barcodeType, "run lane has different " + COL_BARCODE_TYPE + " (expected: "
										+ laneBarcodeType + ")"));
					}
				}

				// all lanes in a run have a different barcode or 'None'
				Set<String> barcodes = new HashSet<String>();
				for (EntityRowPair laneEntityRowPair : laneEntityRowPairs)
				{
					Entity laneEntity = laneEntityRowPair.getEntity();
					String barcode = laneEntity.getString(COL_BARCODE);
					if (!BARCODE_NONE.equals(barcode))
					{
						if (barcodes.contains(barcode)) report.addEntry(runId, new GafListValidationError(
								laneEntityRowPair.getRow(), COL_BARCODE, barcode, "run lane has duplicate "
										+ COL_BARCODE));
						else barcodes.add(barcode);
					}
				}
			}
		}
	}

	private static class EntityRowPair
	{
		private final Entity entity;
		private final int row;

		public EntityRowPair(Entity entity, int row)
		{
			this.entity = entity;
			this.row = row;
		}

		public Entity getEntity()
		{
			return entity;
		}

		public int getRow()
		{
			return row;
		}
	}

	/**
	 * Checks if row is empty.
	 * skip empty rows. skip rows containing only a (prefilled) internal sample id
	 * 
	 * @param entity
	 * @return
	 */
	private boolean isEmptyRow(Entity entity)
	{
		boolean isEmptyRow = true;
		for (String attributeName : entity.getAttributeNames())
		{
			if (!attributeName.equals(COL_INTERNAL_SAMPLE_ID))
			{
				if (StringUtils.isNotEmpty(entity.getString(attributeName)))
				{
					isEmptyRow = false;
					break;
				}
			}
		}
		return isEmptyRow;
	}

	private void validateCell(String runId, int row, String colName, String value, Map<String, Pattern> patterns,
			Map<String, Set<String>> lookupLists, GafListValidationReport report)
	{
		// validate
		if (colName.equalsIgnoreCase(COL_INTERNAL_SAMPLE_ID))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_LANE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_SEQUENCER))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_SAMPLE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_SEQUENCING_START_DATE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_RUN))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_FLOWCELL))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_SEQ_TYPE))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_BARCODE_1))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(End_Product_Concentration_nmol__l))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_EXTERNAL_SAMPLE_ID))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_PROJECT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_CONTACT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_SAMPLE_TYPE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_ARRAY_FILE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_ARRAY_ID))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_CAPTURING_KIT))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_PREP_KIT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_GAF_QC_NAME))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_GAF_QC_DATE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_GAF_QC_STATUS))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_GCC_ANALYSIS))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_PLATES_IN_STOCK__DNA_SEQUENCING))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_REJECTED_FOR_PROCESSING))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_PLATES_IN_STOCK__DNA_SEQUENCING__WHOLE_GENOME_CAPTURING))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_BARCODE_2))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_BARCODE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_BARCODE_TYPE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else
		{
			logger.warn("unknown col [" + colName + "]");
		}
	}

	private void validateCellWithPattern(String runId, int row, String colName, String value,
			Map<String, Pattern> patterns, boolean isRequired, GafListValidationReport report)
	{
		Pattern pattern = patterns.get(colName);
		if (isRequired)
		{
			if (StringUtils.isEmpty(value) || (pattern != null && !pattern.matcher(value).matches()))
			{
				report.addEntry(runId, new GafListValidationError(row, colName, value, "regex does not match"));
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(value) && pattern != null && !pattern.matcher(value).matches())
			{
				report.addEntry(runId, new GafListValidationError(row, colName, value, "regex does not match"));
			}
		}
	}

	private void validateCellWithLookupList(String runId, int row, String colName, String value,
			Map<String, Set<String>> lookupLists, boolean isRequired, GafListValidationReport report)
	{
		Set<String> lookupList = lookupLists.get(colName);
		if (isRequired)
		{
			if (StringUtils.isEmpty(value) || !lookupList.contains(value))
			{
				report.addEntry(runId, new GafListValidationError(row, colName, value, "regex does not match"));
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(value) && !lookupList.contains(value))
			{
				report.addEntry(runId, new GafListValidationError(row, colName, value, "regex does not match"));
			}
		}
	}
}
