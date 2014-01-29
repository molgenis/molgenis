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
import org.molgenis.data.Entity;
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
	public Repository<? extends Entity> annotate (Repository<? extends Entity> source) {
		
		//Create a new Observable feature (column)
		ObservableFeature newFeature = new ObservableFeature();
		
		//Set the identifier and name of the new Observable feature
		//Name cannot be NULL
		newFeature.setIdentifier("qwert");
		newFeature.setName("testColumn");
		
		//Add this new Observable feature to the dataService
		dataService.add(ObservableFeature.ENTITY_NAME, newFeature);
		
		//Load the protocol (collection of column names) of the selected repository (a data set shown in the data explorer)
		Protocol repositoryProtocol = dataService.findOne(Protocol.ENTITY_NAME, 
				new QueryImpl().eq(Protocol.IDENTIFIER, "car_batch123_protocol"));
		
		//Add the new Observable feature to the protocol
		repositoryProtocol.getFeatures().add(newFeature);
		
		//Update the protocol that is stored in the data service with the expanded protocol
		dataService.update(Protocol.ENTITY_NAME, repositoryProtocol);
		
		//Select the data set (repository) 
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, 
				new QueryImpl().eq(DataSet.IDENTIFIER, "CAR_Batch123"));
		
		//Iterate over all the Observation sets (rows) that are part of the selected data set
		Iterable<ObservationSet> osSet = dataService.findAll(ObservationSet.ENTITY_NAME, 
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet));
		
		//For every observation set (row)
		for (ObservationSet os :osSet)
		{
			//Create a new string value and add it to the data service 
			//This is done to make it a known value, so an observable value can get this value
			StringValue sv = new StringValue();
			sv.setValue("Test");
			
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
