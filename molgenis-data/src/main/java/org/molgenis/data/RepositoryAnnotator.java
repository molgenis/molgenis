package org.molgenis.data;

import java.util.Iterator;

public interface RepositoryAnnotator {

	Iterator<Entity> annotate (Iterator<Entity> source);  
	EntityMetaData getOutputMetaData ();
	EntityMetaData getInputMetaData ();
	Boolean canAnnotate ();
	String getName();
	
}
