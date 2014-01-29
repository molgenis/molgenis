package org.molgenis.toolAnnotators;

import org.molgenis.annotators.VariantAnnotator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

/**
 * 
 * @author mdehaan
 * 
 * This class calls the dbNSFP commandline tool to run it
 * 
 * */
public class DbnsfpAnnotator implements VariantAnnotator {

	@Override
	public Repository<? extends Entity> annotate(
			Repository<? extends Entity> source) {
		
		// TODO: go through repository row by row
		// For every row call dbnsfp????
		// Create VCF and then call dbnsfp????
		
		return null;
	}

}
