package org.molgenis.annotators.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for the Annotation UI service
 * 
 * @author mdehaan
 * 
 */

@Service
public class AnnotatorsUIServiceImpl implements AnnotatorsUIService
{
	@Autowired
	DataService dataService;

	@Autowired
	SearchService searchService;

	@Autowired
	FileStore fileStore;

	@Autowired
	DataSetsIndexer indexer;

	@Autowired
	EntityValidator entityValidator;

	@Override
	public void tsvToOmxRepository(String file, String submittedDataSetName)
	{
		// define the data set
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, submittedDataSetName + "_ID"), DataSet.class);

		if (dataSet == null)
		{
			dataSet = createNewDataSet(submittedDataSetName);
		}

		// DefaultAttributeMetaData object for all observable features
		DefaultEntityMetaData metaData = new DefaultEntityMetaData(submittedDataSetName + "_METADATA", MapEntity.class);

		try
		{
			Iterator<Entity> entityIterator = retrieveValuesFromFile(dataSet, metaData, file, submittedDataSetName);

			while (entityIterator.hasNext())
			{
				Entity entity = entityIterator.next();

				// create a new set for every feature value object
				ObservationSet os = new ObservationSet();

				// give it a random id
				os.setIdentifier(UUID.randomUUID().toString());

				// tell this ObservationSet its part of the current data set
				os.setPartOfDataSet(dataSet);

				// add this set set to the DataService
				dataService.add(ObservationSet.ENTITY_NAME, os);

				// for every feature
				for (String columnName : getMetadataNamesAsList(metaData))
				{
					// add value in this row for this column
					addValue(entity, os, columnName, submittedDataSetName);
				}
			}

			// Index the newly created dataSet, this way its visible in the data explorer
			indexResultDataSet(dataSet);
			dataService.addRepository(new OmxRepository(dataService, searchService, dataSet.getIdentifier(),
					entityValidator));
		}
		catch (ValueConverterException e)
		{
			throw new RuntimeException(e);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Iterator<Entity> retrieveValuesFromFile(DataSet dataSet, DefaultEntityMetaData metaData, String file,
			String submittedDataSetName) throws IOException
	{
		List<Entity> results = new ArrayList<Entity>();

		// get file from fileStore
		File uploadedFile = fileStore.getFile(file);

		FileReader reader = new FileReader(uploadedFile);
		BufferedReader bufferedReader = new BufferedReader(reader);

		try
		{
			boolean reachedData = false;
			boolean readHeader = false;
			String[] features = null;

			// start reading file
			while (bufferedReader.ready())
			{
				String line = bufferedReader.readLine();

				// first lines in cartagenia export are descriptive lines
				if (!line.startsWith("#"))
				{
					reachedData = true;
				}

				if (reachedData)
				{
					if (!readHeader)
					{
						// put the column headers from the file in the DefaultAttributeMetaData object
						features = line.split("\t");
						for (String feature : features)
						{
							if (feature.equalsIgnoreCase("POS"))
							{
								metaData.addAttributeMetaData(new DefaultAttributeMetaData(feature, FieldTypeEnum.LONG));
							}
							else
							{
								metaData.addAttributeMetaData(new DefaultAttributeMetaData(feature,
										FieldTypeEnum.STRING));
							}
						}

						// Create a new rootProtocol for this data set
						Protocol newRootProtocol = dataService.findOne(Protocol.ENTITY_NAME,
								new QueryImpl().eq(Protocol.IDENTIFIER, submittedDataSetName + "_PROTOCOL_ID"),
								Protocol.class);

						// if the protocol does not exist
						if (newRootProtocol == null)
						{
							newRootProtocol = createAnnotationResultProtocol(dataSet, metaData.getAttributes(),
									submittedDataSetName);
						}

						// set which protocol this dataSet should use
						dataSet.setProtocolUsed(newRootProtocol);

						// add the dataSet to the dataService
						dataService.add(DataSet.ENTITY_NAME, dataSet);

						// the header is now a list of features in the protocol
						readHeader = true;
					}
					else
					{
						int count = 0;

						// -1 makes sure the split function does not trim empty strings
						// makes sure the number of values equals the number of features
						String[] values = line.split("\t", -1);

						HashMap<String, Object> resultMap = new HashMap<String, Object>();

						// for every feature
						for (String columnName : getMetadataNamesAsList(metaData))
						{
							// the resultMap has a feature + its value
							resultMap.put(columnName, values[count]);
							count = count + 1;
						}

						// put all the objects with feature-value pairs in the results<Entity> list
						results.add(new MapEntity(resultMap));
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			bufferedReader.close();
		}

		return results.iterator();
	}

	private DataSet createNewDataSet(String submittedDataSetName)
	{
		DataSet dataSet = new DataSet();
		dataSet.setIdentifier(submittedDataSetName + "_ID");
		dataSet.setName(submittedDataSetName);

		return dataSet;
	}

	private Protocol createAnnotationResultProtocol(DataSet dataSet, Iterable<AttributeMetaData> outputMetadataNames,
			String submittedDataSetName)
	{
		// Creater new protocol with an identifier and name, and add it to the DataService
		Protocol newRootProtocol = new Protocol();
		newRootProtocol.setIdentifier(submittedDataSetName + "_PROTOCOL_ID");
		newRootProtocol.setName(submittedDataSetName + "_PROTOCOL_NAME");
		dataService.add(Protocol.ENTITY_NAME, newRootProtocol);

		// Add features to the protocol
		addOutputFeatures(newRootProtocol, outputMetadataNames, submittedDataSetName);

		return newRootProtocol;
	}

	private List<String> getMetadataNamesAsList(EntityMetaData metadata)
	{
		Iterator<AttributeMetaData> metadataIterator = metadata.getAttributes().iterator();

		List<String> inputFeatureNames = new ArrayList<String>();
		while (metadataIterator.hasNext())
		{
			AttributeMetaData attributeMetaData = metadataIterator.next();
			inputFeatureNames.add(attributeMetaData.getName());
		}
		return inputFeatureNames;
	}

	private void addOutputFeatures(Protocol newRootProtocol, Iterable<AttributeMetaData> metaData, String prefix)
	{
		for (AttributeMetaData attributeMetaData : metaData)
		{
			ObservableFeature newFeature = new ObservableFeature();
			if (dataService.findOne(ObservableFeature.ENTITY_NAME,
					new QueryImpl().eq(ObservableFeature.IDENTIFIER, attributeMetaData.getName())) == null)
			{
				newFeature.setIdentifier(attributeMetaData.getName());
				newFeature.setName(attributeMetaData.getLabel());
				newFeature.setDataType(attributeMetaData.getDataType().toString());
				dataService.add(ObservableFeature.ENTITY_NAME, newFeature);

				newRootProtocol.getFeatures().add(newFeature);
			}
			dataService.update(Protocol.ENTITY_NAME, newRootProtocol);
		}
	}

	private void addValue(Entity entity, ObservationSet os, String columnName, String prefix)
			throws ValueConverterException
	{
		ObservableFeature thisFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, columnName), ObservableFeature.class);
		ValueConverter valueConverter = new ValueConverter(dataService);
		Value value = valueConverter.fromEntity(entity, columnName, thisFeature);
		dataService.add(Value.ENTITY_NAME, value);

		ObservedValue observedValue = new ObservedValue();
		observedValue.setFeature(thisFeature);
		observedValue.setObservationSet(os);
		observedValue.setValue(value);
		dataService.add(ObservedValue.ENTITY_NAME, observedValue);
	}

	private void indexResultDataSet(DataSet dataSet)
	{
		ArrayList<Integer> datasetIds = new ArrayList<Integer>();
		datasetIds.add(dataSet.getId());
		indexer.indexDataSets(datasetIds);
	}
}