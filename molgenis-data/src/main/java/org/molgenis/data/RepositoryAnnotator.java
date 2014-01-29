package org.molgenis.data;

public interface RepositoryAnnotator {

	Repository<? extends Entity> annotate (Repository<? extends Entity> source);  
		
	
}
