package org.molgenis.omx.biobankconnect.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

public class StoreMappingTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{

	private Database db;
	private static final String OBSERVATION_SET = "observation_set";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private static final String STORE_MAPPING_SCORE = "store_mapping_score";
	private static final String STORE_MAPPING_ABSOLUTE_SCORE = "store_mapping_absolute_score";
	private static final List<String> NON_XREF_FIELDS = Arrays.asList(STORE_MAPPING_ABSOLUTE_SCORE,
			STORE_MAPPING_SCORE, STORE_MAPPING_CONFIRM_MAPPING);
	private final List<ObservationSet> observationSets;
	private final ValueConverter valueConverter;
	private Integer numberOfRows = null;
	private final DataSet dataSet;
	private List<Field> columns;

	public StoreMappingTable(String dataSetIdentifier, Database db) throws DatabaseException
	{
		this.dataSet = db.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, dataSetIdentifier))
				.get(0);
		this.observationSets = db.find(ObservationSet.class, new QueryRule(ObservationSet.PARTOFDATASET_IDENTIFIER,
				Operator.EQUALS, dataSet.getIdentifier()));
		this.valueConverter = new ValueConverter(db);
		setDb(db);
	}

	public StoreMappingTable(String dataSetIdentifier, List<ObservationSet> observationSets, Database db)
			throws DatabaseException
	{
		this.dataSet = db.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, dataSetIdentifier))
				.get(0);
		this.observationSets = observationSets;
		this.valueConverter = new ValueConverter(db);
		setDb(db);
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();
		try
		{
			List<String> observationSetIdentifiers = new ArrayList<String>();
			Map<Integer, KeyValueTuple> storeMapping = new HashMap<Integer, KeyValueTuple>();

			for (ObservationSet observation : observationSets)
			{
				observationSetIdentifiers.add(observation.getIdentifier());
			}

			for (ObservedValue ov : db.find(ObservedValue.class, new QueryRule(ObservedValue.OBSERVATIONSET_IDENTIFIER,
					Operator.IN, observationSetIdentifiers)))
			{
				KeyValueTuple tuple = null;
				Integer observationId = ov.getObservationSet_Id();
				if (storeMapping.containsKey(observationId)) tuple = storeMapping.get(observationId);
				else tuple = new KeyValueTuple();
				if (NON_XREF_FIELDS.contains(ov.getFeature_Identifier()))
				{
					tuple.set(ov.getFeature_Identifier(), valueConverter.toCell(ov.getValue()));
				}
				else
				{
					Characteristic xrefCharacteristic = ((XrefValue) ov.getValue()).getValue();
					tuple.set(ov.getFeature_Identifier(), xrefCharacteristic.getId());
				}
				if (tuple.get(OBSERVATION_SET) == null) tuple.set(OBSERVATION_SET, observationId);
				storeMapping.put(observationId, tuple);
			}

			for (KeyValueTuple tuple : storeMapping.values())
				tuples.add(tuple);
		}
		catch (Exception e)
		{
			new RuntimeException("Failed to index mapping table : " + dataSet.getName() + " error : " + e.getMessage());
		}
		return tuples.iterator();
	}

	@Override
	public Database getDb()
	{
		return this.db;
	}

	@Override
	public void setDb(Database db)
	{
		this.db = db;
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		if (columns == null) initColumnsFromDb();
		columns.add(new Field(OBSERVATION_SET));
		return Collections.unmodifiableList(columns);
	}

	private void initColumnsFromDb() throws TableException
	{
		try
		{
			Protocol protocol = dataSet.getProtocolUsed();

			// if dataset has protocol-used, determine columns from protocol
			if (protocol != null)
			{
				List<ObservableFeature> features = new ArrayList<ObservableFeature>();
				getFeatures(protocol, features);

				if (features != null && !features.isEmpty())
				{
					columns = new ArrayList<Field>(features.size());
					for (ObservableFeature feature : features)
					{
						FieldType fieldType = MolgenisFieldTypes.getType(feature.getDataType());

						Field field = new Field(feature.getIdentifier());
						field.setEntity(db.getMetaData().getEntity(ObservableFeature.class.getSimpleName()));
						field.setLabel(feature.getName());
						field.setType(fieldType);
						field.setName(feature.getIdentifier());

						FieldTypeEnum enumType = fieldType.getEnumType();
						if (enumType.equals(FieldTypeEnum.XREF) || enumType.equals(FieldTypeEnum.MREF))
						{
							field.setXRefEntity(Characteristic.class.getSimpleName());
							field.setXrefField(Characteristic.NAME);
						}
						else if (enumType.equals(FieldTypeEnum.CATEGORICAL))
						{
							field.setXRefEntity(Category.class.getSimpleName());
							field.setXrefField(org.molgenis.omx.observ.Category.NAME);
						}

						columns.add(field);
					}
				}
				else
				{
					columns = Collections.emptyList();
				}
			}
		}
		catch (Exception e)
		{
			throw new TableException("Failed to initialize the columns for Mapping Table" + e);
		}
	}

	private void getFeatures(Protocol protocol, List<ObservableFeature> features) throws DatabaseException
	{
		// store features
		features.addAll(protocol.getFeatures());

		for (Protocol subProtocol : protocol.getSubprotocols())
		{
			getFeatures(subProtocol, features);
		}
	}

	@Override
	public int getCount() throws TableException
	{
		if (numberOfRows == null)
		{
			try
			{
				numberOfRows = this.observationSets.size();
			}
			catch (Exception e)
			{
				new RuntimeException(e);
			}
		}
		return numberOfRows;
	}
}
