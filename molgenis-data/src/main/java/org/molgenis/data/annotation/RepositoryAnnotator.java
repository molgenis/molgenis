package org.molgenis.data.annotation;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import java.util.Iterator;

public interface RepositoryAnnotator {

	Iterator<Entity> annotate (Iterator<Entity> source);
	EntityMetaData getOutputMetaData ();
	EntityMetaData getInputMetaData ();
	boolean canAnnotate(EntityMetaData inputMetaData);
	String getName();
	
	
}
