package org.molgenis.variome;

import org.molgenis.genotype.tabix.*;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service implementation for the variome service
 * 
 * @author Mark-de-Haan
 * 
 */

@Service
public class VariomeServiceImpl implements VariomeService
{
	@Autowired
	DataService dataService;
	private Iterable<Entity> data;
	
	@Override
	public void dataSetAnnotater() {
		
		File cadd = new File("/Users/mdehaan/Downloads/1000G.tsv.gz");
		TabixIndex caddIndex = null;
		
		TabixRawLineQuery query = new TabixRawLineQuery(cadd, caddIndex);
		System.out.println(query.executeQuery("G", 10583));
		
		//Find data set CAR_Batch123, for demo purposes already uploaded
		data = dataService.findAll("CAR_Batch123");

		// Loop through the data set rows
		for (Entity entity : data) {
			System.out.println(entity.get("chromosome") + ":" + entity.get("start_nucleotide") 
					+ "-" + entity.get("start_nucleotide"));
		}
	}
}