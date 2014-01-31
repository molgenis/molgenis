package org.molgenis.variome;

import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.omx.observ.value.StringValue;

/**
 * 
 * @author mdehaan
 * 
 * This class inserts a observable feature 
 * and observable values for each observable set into a existing data set
 * 
 * */
public class ExampleRepositoryAnnotator implements RepositoryAnnotator {

	@Autowired
	DataService dataService;
	
	@Override
	@Transactional
	public Repository annotate (Repository source) {
		
		//Create a new Observable feature (column)
		ObservableFeature newFeature = new ObservableFeature();
		
		//Set the identifier and name of the new Observable feature
		//Name cannot be NULL
		newFeature.setIdentifier("id");
		newFeature.setName("column_name");
		
		//Add this new Observable feature to the dataService
		dataService.add(ObservableFeature.ENTITY_NAME, newFeature);
		
		//Load the protocol (collection of column names) of the selected repository (a data set shown in the data explorer)
		Protocol repositoryProtocol = dataService.findOne(Protocol.ENTITY_NAME, 
				new QueryImpl().eq(Protocol.IDENTIFIER, "protocol_id"), Protocol.class);
		
		//Add the new Observable feature to the protocol
		repositoryProtocol.getFeatures().add(newFeature);
		
		//Update the protocol that is stored in the data service with the expanded protocol
		dataService.update(Protocol.ENTITY_NAME, repositoryProtocol);
		
		//Select the data set (repository) 
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, 
				new QueryImpl().eq(DataSet.IDENTIFIER, "dataset_id"), DataSet.class);
		
		//Iterate over all the Observation sets (rows) that are part of the selected data set
		Iterable<ObservationSet> osSet = dataService.findAll(ObservationSet.ENTITY_NAME, 
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);
		
		//For every observation set (row)
		for (ObservationSet os :osSet)
		{
			//Use a work around to acces the features for this observation set
			CrudRepository valueRepo = (CrudRepository) dataService.getRepositoryByEntityName(ObservedValue.ENTITY_NAME);
			ObservedValue value = valueRepo.findOne(
					new QueryImpl().eq(ObservedValue.OBSERVATIONSET, os).eq(ObservedValue.FEATURE, "column_name"), ObservedValue.class);
			
			//Create a new string value and add it to the data service 
			//This is done to make it a known value, so an observable value can get this value
			StringValue sv = new StringValue();
			sv.setValue("annotation_obtained_with_value(s)");
			dataService.add(StringValue.ENTITY_NAME, sv);
			
			// Create a new observation value
			ObservedValue ov = new ObservedValue();
			
			//Insert the new observed value into the new column
			ov.setFeature(newFeature);
			//Insert the new observed value into the current observed set (row) 
			ov.setObservationSet(os);
			
			//Give the new observed value a value
			ov.setValue(sv);

			//Add the observed value that is now in the correct column and row to the data service
			dataService.add(ObservedValue.ENTITY_NAME, ov);
		}
		
		return null;
	}
}
