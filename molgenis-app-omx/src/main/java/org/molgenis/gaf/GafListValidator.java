package org.molgenis.gaf;

import java.io.IOException;
import java.util.ArrayList;
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

	/**
	 * RuntimeProperty.class.getSimpleName() + "_gafList.validator.";
	 */
	public static final String GAF_LIST_VALIDATOR_PREFIX = "gafList.validator.";

	/**
	 * RuntimeProperty.class.getSimpleName() + "_gafList.validator.example.";
	 */
	public static final String GAF_LIST_VALIDATOR_EXAMPLE_PREFIX = "gafList.validator.example.";

	private static final List<String> COLUMNS = GAFCol.getAllColumnsNames();

	static final String BARCODE_NONE = "None";

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	public GafListValidationReport validate(GafListValidationReport report, Repository repository) throws IOException
	{
		// retrieve validation patterns
		Map<String, Pattern> patternMap = new HashMap<String, Pattern>();
		for (String colName : COLUMNS)
		{
			String pattern = molgenisSettings.getProperty(GAF_LIST_VALIDATOR_PREFIX + colName);
			if (pattern != null) patternMap.put(colName, Pattern.compile(pattern));
		}

		// retrieve validation patterns
		Map<String, String> patternExampleMap = new HashMap<String, String>();
		for (String colName : COLUMNS)
		{
			String example = molgenisSettings.getProperty(GAF_LIST_VALIDATOR_EXAMPLE_PREFIX + colName);
			if (example != null) patternExampleMap.put(colName, example);
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
			String runId = entity.getString(GAFCol.RUN.toString());
			report.getAllRunIds().add(runId);
			entities.add(entity);
		}

		validateCellValues(entities, attributes, patternMap, patternExampleMap, lookupLists, report);
		validateInternalSampleIdIncremental(entities, report);
		validateRun(entities, report);
		report.populateStatusImportedRuns();
		
		return report;
	}

	private void validateCellValues(List<Entity> entities, Iterable<AttributeMetaData> attributes,
			Map<String, Pattern> patternMap, Map<String, String> patternExampleMap,
			Map<String, Set<String>> lookupLists, GafListValidationReport report)
	{
		int row = 2;
		for (Entity entity : entities)
		{
			// skip empty rows
			if (isEmptyRow(entity)) continue;

			// validate individual cells
			String runId = entity.getString(GAFCol.RUN.toString());

			for (AttributeMetaData attributeMetaData : attributes)
			{
				String attributeName = attributeMetaData.getName().trim();
				if (attributeName.isEmpty()) continue;
				String value = entity.getString(attributeName);

				// validate cell
				validateCell(runId, row, attributeName, value, patternMap, patternExampleMap, lookupLists, report);
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
			String runId = entity.getString(GAFCol.RUN.toString());
			Integer internalSampleId = null;
			try{
				internalSampleId = entity.getInt(GAFCol.INTERNAL_SAMPLE_ID.toString());
				if (internalSampleId == null)
				{
					// internal sample id can not be null
					report.addEntry(runId, new GafListValidationError(row, GAFCol.INTERNAL_SAMPLE_ID.toString(),
							null,
							"value undefined"));
				}
			}
			catch (ConversionFailedException cfe)
			{
				new GafListValidationError(row, GAFCol.INTERNAL_SAMPLE_ID.toString(), null,
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

			String runId = entity.getString(GAFCol.RUN.toString());
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
				String seqType = entityRowPair.getEntity().getString(GAFCol.SEQ_TYPE.toString());
				if (runSeqType == null) runSeqType = seqType;
				else if (!runSeqType.equals(seqType))
				{
					report.addEntry(runId,
							new GafListValidationError(entityRowPair.getRow(), GAFCol.SEQ_TYPE.toString(), seqType,
									"run has different " + GAFCol.SEQ_TYPE.toString()));
				}
			}

			// all rows related to a run have the same sequence date
			String runSeqStartDate = null;
			for (EntityRowPair entityRowPair : entityRowPairs)
			{
				String seqStartDate = entityRowPair.getEntity().getString(GAFCol.SEQUENCING_START_DATE.toString());
				if (runSeqStartDate == null) runSeqStartDate = seqStartDate;
				else if (!runSeqStartDate.equals(seqStartDate))
				{
					report.addEntry(runId, new GafListValidationError(entityRowPair.getRow(),
							GAFCol.SEQUENCING_START_DATE.toString(), seqStartDate, "run has different "
									+ GAFCol.SEQUENCING_START_DATE.toString()));
				}
			}

			// group run rows by lane
			Map<String, List<EntityRowPair>> laneMap = new HashMap<String, List<EntityRowPair>>();
			for (EntityRowPair entityRowPair : entityRowPairs)
			{
				Entity laneEntity = entityRowPair.getEntity();
				String lane = laneEntity.getString(GAFCol.LANE.toString());
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
					String barcodeType = laneEntity.getString(GAFCol.BARCODE_TYPE.toString());
					if (laneBarcodeType == null) laneBarcodeType = barcodeType;
					else if (!laneBarcodeType.equals(barcodeType))
					{
						report.addEntry(runId, new GafListValidationError(laneEntityRowPair.getRow(),
								GAFCol.BARCODE_TYPE.toString(), barcodeType, "run lane has different "
										+ GAFCol.BARCODE_TYPE + " (expected: "
										+ laneBarcodeType + ")"));
					}
				}

				// all lanes in a run have a different barcode or 'None'
				Set<String> barcodes = new HashSet<String>();
				for (EntityRowPair laneEntityRowPair : laneEntityRowPairs)
				{
					Entity laneEntity = laneEntityRowPair.getEntity();
					String barcode = laneEntity.getString(GAFCol.BARCODE.toString());
					if (!BARCODE_NONE.equals(barcode))
					{
						if (barcodes.contains(barcode)) report.addEntry(runId, new GafListValidationError(
								laneEntityRowPair.getRow(), GAFCol.BARCODE.toString(), barcode,
								"run lane has duplicate " + GAFCol.BARCODE.toString()));
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
			if (!attributeName.equals(GAFCol.INTERNAL_SAMPLE_ID.toString()))
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
			Map<String, String> patternExampleMap,
			Map<String, Set<String>> lookupLists, GafListValidationReport report)
	{
		// validate
		if (colName.equalsIgnoreCase(GAFCol.INTERNAL_SAMPLE_ID.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.LANE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.SEQUENCER.toString()))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report, patternExampleMap);
		}
		else if (colName.equalsIgnoreCase(GAFCol.SAMPLE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.SEQUENCING_START_DATE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.RUN.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.FLOWCELL.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.SEQ_TYPE.toString()))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report, patternExampleMap);
		}
		else if (colName.equalsIgnoreCase(GAFCol.BARCODE_1.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.End_Product_Concentration_nmol__l.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.EXTERNAL_SAMPLE_ID.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.PROJECT.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.CONTACT.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, true, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.SAMPLE_TYPE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.ARRAY_FILE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.ARRAY_ID.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.CAPTURING_KIT.toString()))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report, patternExampleMap);
		}
		else if (colName.equalsIgnoreCase(GAFCol.PREP_KIT.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.GAF_QC_NAME.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.GAF_QC_DATE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.GAF_QC_STATUS.toString()))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, true, report, patternExampleMap);
		}
		else if (colName.equalsIgnoreCase(GAFCol.GCC_ANALYSIS.toString()))
		{
			validateCellWithLookupList(runId, row, colName, value, lookupLists, false, report, patternExampleMap);
		}
		else if (colName.equalsIgnoreCase(GAFCol.PLATES_IN_STOCK__DNA_SEQUENCING.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.REJECTED_FOR_PROCESSING.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.PLATES_IN_STOCK__DNA_SEQUENCING__WHOLE_GENOME_CAPTURING.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.BARCODE_2.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.BARCODE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else if (colName.equalsIgnoreCase(GAFCol.BARCODE_TYPE.toString()))
		{
			validateCellWithPattern(runId, row, colName, value, patterns, patternExampleMap, false, report);
		}
		else
		{
			logger.warn("unknown col [" + colName + "]");
		}
	}

	private void validateCellWithPattern(String runId, int row, String colName, String value,
			Map<String, Pattern> patterns, Map<String, String> patternExampleMap, boolean isRequired,
			GafListValidationReport report)
	{
		Pattern pattern = patterns.get(colName);
		if (isRequired)
		{
			if (StringUtils.isEmpty(value) || (pattern != null && !pattern.matcher(value).matches()))
			{
				report.addEntry(
						runId,
						new GafListValidationError(row, colName, value, this.getPatternErrorMessage(colName,
								patternExampleMap)));
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(value) && pattern != null && !pattern.matcher(value).matches())
			{
				report.addEntry(
						runId,
						new GafListValidationError(row, colName, value, this.getPatternErrorMessage(colName,
								patternExampleMap)));
			}
		}
	}

	private void validateCellWithLookupList(String runId, int row, String colName, String value,
			Map<String, Set<String>> lookupLists, boolean isRequired, GafListValidationReport report,
			Map<String, String> patternExampleMap)
	{
		Set<String> lookupList = lookupLists.get(colName);
		if (isRequired)
		{
			if (StringUtils.isEmpty(value) || !lookupList.contains(value))
			{
				report.addEntry(
						runId,
						new GafListValidationError(row, colName, value, this.getPatternErrorMessage(colName,
								patternExampleMap)));
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(value) && !lookupList.contains(value))
			{
				report.addEntry(
						runId,
						new GafListValidationError(row, colName, value, this.getPatternErrorMessage(colName,
								patternExampleMap)));
			}
		}
	}
	
	private String getPatternErrorMessage(String colName, Map<String, String> patternExampleMap)
	{
		String message = patternExampleMap.get(colName);
		if(null == message){
			message = "Something went wrong!";
		}
		return message;
	}
}
