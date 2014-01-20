package org.molgenis.gaf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GafListValidator
{
	private static final Logger logger = Logger.getLogger(GafListValidator.class);

	static final String GAF_LIST_SETTINGS_PREFIX = RuntimeProperty.class.getSimpleName() + "_gafList.validator.";

	private static final List<String> COLUMNS;

	public static final String COL_INTERNAL_SAMPLE_ID;
	public static final String COL_EXTERNAL_SAMPLE_ID;
	public static final String COL_PROJECT;
	public static final String COL_SEQUENCER;
	public static final String COL_CONTACT;
	public static final String COL_VALIDATION_LOG;
	public static final String COL_LAB_STATUS_COMMENTS;
	public static final String COL_LAB_STATUS_PHASE;
	public static final String COL_LAST_UP_DATE;
	public static final String COL_SEQUENCING_START_DATE;
	public static final String COL_RUN;
	public static final String COL_FLOWCELL;
	public static final String COL_LANE;
	public static final String COL_BARCODE_MENU;
	public static final String COL_SEQ_TYPE;
	public static final String COL_PREP_KIT;
	public static final String COL_CAPTURING_KIT;
	public static final String COL_ARRAY_FILE;
	public static final String COL_ARRAY_ID;
	public static final String COL_GAF_QC_NAME;
	public static final String COL_GAF_QC_DATE;
	public static final String COL_GAF_QC_STATUS;
	public static final String COL_GCC_ANALYSIS;
	public static final String COL_GCC_QC_NAME;
	public static final String COL_GCC_QC_DATE;
	public static final String COL_GCC_QC_STATUS;
	public static final String COL_TARGET_DATE_SHIPMENT;
	public static final String COL_DATA_SHIPPED_DATE;
	public static final String COL_DATA_SHIPPED_TO;
	public static final String COL_DATA_SHIPPED_BY;
	public static final String COL_COMMENTS;
	public static final String COL_BARCODE;
	public static final String COL_BARCODE_TYPE;

	static final String LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY = "Finished Successfully";
	static final String BARCODE_NONE = "None";

	static
	{
		COL_INTERNAL_SAMPLE_ID = "internalSampleID";
		COL_EXTERNAL_SAMPLE_ID = "externalSampleID";
		COL_PROJECT = "project";
		COL_SEQUENCER = "sequencer";
		COL_CONTACT = "contact";
		COL_VALIDATION_LOG = "validationLog";
		COL_LAB_STATUS_COMMENTS = "labStatusComments";
		COL_LAB_STATUS_PHASE = "labStatusPhase";
		COL_LAST_UP_DATE = "lastUpDate";
		COL_SEQUENCING_START_DATE = "sequencingStartDate";
		COL_RUN = "run";
		COL_FLOWCELL = "flowcell";
		COL_LANE = "lane";
		COL_BARCODE_MENU = "barcodeMenu";
		COL_SEQ_TYPE = "seqType";
		COL_PREP_KIT = "prepKit";
		COL_CAPTURING_KIT = "capturingKit";
		COL_ARRAY_FILE = "arrayFile";
		COL_ARRAY_ID = "arrayID";
		COL_GAF_QC_NAME = "GAF_QC_Name";
		COL_GAF_QC_DATE = "GAF_QC_Date";
		COL_GAF_QC_STATUS = "GAF_QC_Status";
		COL_GCC_ANALYSIS = "GCC_Analysis";
		COL_GCC_QC_NAME = "GCC_QC_Name";
		COL_GCC_QC_DATE = "GCC_QC_Date";
		COL_GCC_QC_STATUS = "GCC_QC_Status";
		COL_TARGET_DATE_SHIPMENT = "TargetDateShipment";
		COL_DATA_SHIPPED_DATE = "DataShippedDate";
		COL_DATA_SHIPPED_TO = "DataShippedTo";
		COL_DATA_SHIPPED_BY = "DataShippedBy";
		COL_COMMENTS = "Comments";
		COL_BARCODE = "barcode";
		COL_BARCODE_TYPE = "barcodeType";

		COLUMNS = Arrays.asList(COL_INTERNAL_SAMPLE_ID, COL_EXTERNAL_SAMPLE_ID, COL_PROJECT, COL_SEQUENCER,
				COL_CONTACT, COL_VALIDATION_LOG, COL_LAB_STATUS_COMMENTS, COL_LAB_STATUS_PHASE, COL_LAST_UP_DATE,
				COL_SEQUENCING_START_DATE, COL_RUN, COL_FLOWCELL, COL_LANE, COL_BARCODE_MENU, COL_SEQ_TYPE,
				COL_PREP_KIT, COL_CAPTURING_KIT, COL_ARRAY_FILE, COL_ARRAY_ID, COL_GAF_QC_NAME, COL_GAF_QC_DATE,
				COL_GAF_QC_STATUS, COL_GCC_ANALYSIS, COL_GCC_QC_NAME, COL_GCC_QC_DATE, COL_GCC_QC_STATUS,
				COL_TARGET_DATE_SHIPMENT, COL_DATA_SHIPPED_DATE, COL_DATA_SHIPPED_TO, COL_DATA_SHIPPED_BY,
				COL_COMMENTS, COL_BARCODE, COL_BARCODE_TYPE);
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	public GafListValidationReport validate(Repository<Entity> repository) throws IOException
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
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, colName));
			Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
					new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature));
			if (categories != null)
			{
				Set<String> lookupList = new HashSet<String>();
				for (Category category : categories)
					lookupList.add(category.getValueCode());
				lookupLists.put(colName, lookupList);
			}
		}

		validateCellValues(repository, patternMap, lookupLists, report);
		validateInternalSampleId(repository, report);
		validateRun(repository, report);

		return report;
	}

	private void validateCellValues(Repository<Entity> repository, Map<String, Pattern> patternMap,
			Map<String, Set<String>> lookupLists, GafListValidationReport report)
	{
		int row = 2;
		Iterable<AttributeMetaData> attributes = repository.getAttributes();
		for (Iterator<Entity> it = repository.iterator(); it.hasNext(); ++row)
		{
			Entity entity = it.next();

			// skip empty rows
			if (isEmptyRow(entity)) continue;

			// validate individual cells
			String runId = entity.getString(COL_RUN);
			for (AttributeMetaData attributeMetaData : attributes)
			{
				String labStatusPhase = entity.getString(COL_LAB_STATUS_PHASE);
				if (labStatusPhase != null && labStatusPhase.equals(LAB_STATUS_PHASE_FINISHED_SUCCESSFULLY))
				{
					String attributeName = attributeMetaData.getName();
					if (attributeName.isEmpty()) continue;
					String value = entity.getString(attributeName);

					// validate cell
					validateCell(runId, row, attributeName, value, patternMap, lookupLists, report);
				}
			}
		}
	}

	/**
	 * Validate that internal sample id is incremental (and thus unique)
	 * 
	 * @param repository
	 * @param report
	 */
	private void validateInternalSampleId(Repository<Entity> repository, GafListValidationReport report)
	{
		Integer previousInternalSampleId = null;
		int row = 2;
		for (Iterator<Entity> it = repository.iterator(); it.hasNext(); ++row)
		{
			Entity entity = it.next();

			// skip empty rows
			if (isEmptyRow(entity)) continue;

			String runId = entity.getString(COL_RUN);
			Integer internalSampleId = entity.getInt(COL_INTERNAL_SAMPLE_ID);
			if (internalSampleId != null)
			{
				if (previousInternalSampleId != null)
				{
					if (internalSampleId != previousInternalSampleId + 1)
					{
						report.addEntry(runId,
								new GafListValidationError(row, COL_INTERNAL_SAMPLE_ID, internalSampleId.toString(),
										"non-incremental"));
					}
				}
				previousInternalSampleId = internalSampleId;
			}
			else
			{
				// internal sample id can not be null
				report.addEntry(runId, new GafListValidationError(row, COL_INTERNAL_SAMPLE_ID, null, "value undefined"));
			}
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
	private void validateRun(Repository<Entity> repository, GafListValidationReport report)
	{
		// group rows by run
		Map<String, List<EntityRowPair>> runMap = new HashMap<String, List<EntityRowPair>>();

		int row = 2;
		for (Iterator<Entity> it = repository.iterator(); it.hasNext(); ++row)
		{
			Entity entity = it.next();

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
					if (!barcode.equals(BARCODE_NONE))
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

	private boolean isEmptyRow(Entity entity)
	{
		// skip empty rows
		// skip rows containing only a (prefilled) internal sample id
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
		else if (colName.equalsIgnoreCase(COL_EXTERNAL_SAMPLE_ID))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_PROJECT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_SEQUENCER))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_CONTACT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_VALIDATION_LOG))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_LAB_STATUS_COMMENTS))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_LAST_UP_DATE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_LAB_STATUS_PHASE))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
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
		else if (colName.equalsIgnoreCase(COL_LANE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_BARCODE_MENU))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_SEQ_TYPE))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_PREP_KIT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_CAPTURING_KIT))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report);
		}
		else if (colName.equalsIgnoreCase(COL_ARRAY_FILE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_ARRAY_ID))
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
		else if (colName.equalsIgnoreCase(COL_GCC_QC_NAME))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_GCC_QC_DATE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_GCC_QC_STATUS))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_TARGET_DATE_SHIPMENT))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_DATA_SHIPPED_DATE))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_DATA_SHIPPED_TO))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_DATA_SHIPPED_BY))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, false, report);
		}
		else if (colName.equalsIgnoreCase(COL_COMMENTS))
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

	public static class GafListValidationReport
	{
		private final Map<String, List<GafListValidationError>> validationErrors;

		public GafListValidationReport()
		{
			validationErrors = new LinkedHashMap<String, List<GafListValidationError>>();
		}

		public void addEntry(String runId, GafListValidationError validationError)
		{
			List<GafListValidationError> runEntries = validationErrors.get(runId);
			if (runEntries == null)
			{
				runEntries = new ArrayList<GafListValidationError>();
				validationErrors.put(runId, runEntries);
			}
			runEntries.add(validationError);
		}

		public Map<String, List<GafListValidationError>> getEntries()
		{
			return Collections.unmodifiableMap(validationErrors);
		}

		public boolean hasErrors()
		{
			return !validationErrors.isEmpty();
		}

		public boolean hasErrors(String runId)
		{
			return validationErrors.containsKey(runId);
		}

		@Override
		public String toString()
		{
			StringBuilder strBuilder = new StringBuilder();
			for (Entry<String, List<GafListValidationError>> reportEntry : validationErrors.entrySet())
			{
				String runId = reportEntry.getKey();
				if (runId == null) runId = "<undefined>";
				strBuilder.append("Validation errors for run ").append(runId).append('\n');
				for (GafListValidationError validationError : reportEntry.getValue())
				{
					strBuilder.append('\t').append(validationError).append('\n');
				}
			}
			return strBuilder.toString();
		}
	}

	public static class GafListValidationError
	{
		private final int row;
		private final String colName;
		private final String value;
		private final String msg;

		public GafListValidationError(int row, String colName, String value, String msg)
		{
			this.row = row;
			this.colName = colName;
			this.value = value;
			this.msg = msg;
		}

		public int getRow()
		{
			return row;
		}

		public String getColName()
		{
			return colName;
		}

		public String getValue()
		{
			return value;
		}

		public String getMsg()
		{
			return msg;
		}

		@Override
		public String toString()
		{
			return "row: " + row + "\tcol: " + colName + "\tval: " + value + (msg != null ? "\tmsg: " + msg : "");
		}

	}
}
