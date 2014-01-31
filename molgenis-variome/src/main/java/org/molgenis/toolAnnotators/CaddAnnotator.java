package org.molgenis.toolAnnotators;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import org.molgenis.annotators.VariantAnnotator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

public class CaddAnnotator implements VariantAnnotator {

	@Autowired
	DataService dataService;
	
	@Override
	@Transactional
	public Repository annotate(Repository source) {

		// The CADD file should be read with Tabix, because it has an index file complementing it.
		// TODO: Edit the Tabix reader from Genotype I/O to read CADD
		HashMap<Integer, String> caddMutations = new HashMap<Integer, String>();
		HashMap<Integer, String> caddScores = new HashMap<Integer, String>();
		
		try{
			File cadd = new File("/Users/mdehaan/Downloads/1000G.tsv");
			Scanner s = new Scanner(cadd);
			String[] split = null;
			s.nextLine();
			int count = 0;
			System.out.println("Reading cadd file into memory...");
			
			while(s.hasNext()){
				count = count + 1;
				String line = s.nextLine();
				if(line.startsWith("#")){continue;}
				split = line.split("\t");
				caddMutations.put(Integer.parseInt(split[1]), split[3]);
				caddScores.put(Integer.parseInt(split[1]), split[5]);
				if(count == 20000){
					break;
				}
			}
			
		} catch (Exception e) {
			System.out.println("The following went wrong: " + e.getMessage());
		}
			
			System.out.println("Done Reading");
			
			System.out.println(source.getName());
			
			//Select the data set (repository) 
			DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, 
					new QueryImpl().eq(DataSet.IDENTIFIER, source.getName()), DataSet.class);
			
			//Create a new Observable feature (column)
			ObservableFeature newFeature = new ObservableFeature();
			
			//Set the identifier and name of the new Observable feature
			//Name cannot be NULL
			newFeature.setIdentifier("cadd_score");
			newFeature.setName("CADD Score");
			
			//Add this new Observable feature to the dataService
			dataService.add(ObservableFeature.ENTITY_NAME, newFeature);
			
			System.out.println(dataSet.getProtocolUsed().get("name"));
			
			//Load the protocol (collection of column names) of the selected repository (a data set shown in the data explorer)
			Protocol repositoryProtocol = dataService.findOne(Protocol.ENTITY_NAME, 
					new QueryImpl().eq(Protocol.IDENTIFIER, dataSet.getProtocolUsed().get("name")), Protocol.class);
			
			//Add the new Observable feature to the protocol
			repositoryProtocol.getFeatures().add(newFeature);
			
			//Update the protocol that is stored in the data service with the expanded protocol
			dataService.update(Protocol.ENTITY_NAME, repositoryProtocol);
			
			//Iterate over all the Observation sets (rows) that are part of the selected data set
			Iterable<ObservationSet> osSet = dataService.findAll(ObservationSet.ENTITY_NAME, 
					new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);
			
			//For every observation set (row)
			for (ObservationSet os : osSet)
			{
				System.out.println(os);
				
				//Create a new string value and add it to the data service 
				//This is done to make it a known value, so an observable value can get this value
				StringValue sv = new StringValue();
				
				System.out.println(os.get("start_nucleotide"));
				System.out.println(os.get("ALT"));
				
				Integer location = Integer.parseInt(os.get("start_nucleotide").toString());
				String alt = os.get("ALT").toString();
				
				if(caddMutations.containsKey(location)) {
					System.out.println("location in set: " + location);
					if(alt.equals(caddMutations.get(location))) {
						System.out.println("alt in set: " + alt + " alt in hash: " + caddMutations.get(location));
						sv.setValue(caddScores.get(location));
					}
				} else {
					sv.setValue("Unknown");
				}

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
		
		// TODO: Go through repository row by row
		// For every row call the CADD file and add the returned information
		// as observable value to the data set
	
		return null;
	}

}
