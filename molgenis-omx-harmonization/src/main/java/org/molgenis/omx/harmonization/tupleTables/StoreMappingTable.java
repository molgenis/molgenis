package org.molgenis.omx.harmonization.tupleTables;

import java.util.ArrayList;
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
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

public class StoreMappingTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{

	private Database db;
	private final String dataSetIdentifier;
	private static final String OBSERVATION_SET = "observation_set";
	private final ValueConverter valueConverter;
	private Integer numberOfRows = null;
	private DataSet dataSet;
	private List<Field> columns;

	public StoreMappingTable(String dataSetIdentifier, Database db) throws DatabaseException
	{
		this.dataSet = db.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, dataSetIdentifier))
				.get(0);
		this.dataSetIdentifier = dataSetIdentifier;
		this.valueConverter = new ValueConverter(db);
		setDb(db);
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();
		try
		{
			List<Integer> observationSetIds = new ArrayList<Integer>();
			Map<Integer, KeyValueTuple> storeMapping = new HashMap<Integer, KeyValueTuple>();

			for (ObservationSet observation : db.find(ObservationSet.class, new QueryRule(
					ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.EQUALS, this.dataSetIdentifier)))
			{
				observationSetIds.add(observation.getId());
			}

			for (ObservedValue ov : db.find(ObservedValue.class, new QueryRule(ObservedValue.OBSERVATIONSET_ID,
					Operator.IN, observationSetIds)))
			{
				KeyValueTuple tuple = null;
				Integer observationId = ov.getObservationSet_Id();
				if (storeMapping.containsKey(observationId)) tuple = storeMapping.get(observationId);
				else tuple = new KeyValueTuple();
				System.out.println();
				tuple.set(ov.getFeature_Identifier(), valueConverter.toCell(ov.getValue()));
				if (tuple.get(OBSERVATION_SET) == null) tuple.set(OBSERVATION_SET, observationId);
				storeMapping.put(observationId, tuple);
			}

			for (KeyValueTuple tuple : storeMapping.values())
				tuples.add(tuple);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			throw new TableException(e);
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
				List<ObservationSet> observationSets = db.find(ObservationSet.class, new QueryRule(
						ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.EQUALS, this.dataSetIdentifier));
				numberOfRows = observationSets.size();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return numberOfRows;
	}
}
