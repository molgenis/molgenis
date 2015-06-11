package org.molgenis.data;

import java.util.Iterator;
import java.util.List;

public interface RepositoryAnnotator
{
	Iterator<Entity> annotate(Iterator<Entity> source);

	List<AttributeMetaData> getOutputMetaData();

	List<AttributeMetaData> getInputMetaData();

	Boolean canAnnotate(EntityMetaData sourceMetaData);

	String getName();
}
