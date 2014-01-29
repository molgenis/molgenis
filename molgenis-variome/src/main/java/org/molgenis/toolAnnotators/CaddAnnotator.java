package org.molgenis.toolAnnotators;

import org.molgenis.annotators.VariantAnnotator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

public class CaddAnnotator implements VariantAnnotator {

	@Override
	public Repository<? extends Entity> annotate(
			Repository<? extends Entity> source) {

		// TODO: Go through repository row by row
		// For every row call the CADD file and add the returned information
		// as observable value

		return null;
	}

}
