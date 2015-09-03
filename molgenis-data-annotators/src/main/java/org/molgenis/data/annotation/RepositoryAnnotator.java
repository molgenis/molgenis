package org.molgenis.data.annotation;

import java.util.Iterator;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.entity.AnnotatorInfo;

/**
 * interface for annotators. annotators take an iterator and return an iterator with some information added or updated
 */

public interface RepositoryAnnotator
{
	final static String ANNOTATOR_PREFIX = "molgenis_annotated_";

	AnnotatorInfo getInfo();

	// add entityAnnotator
	Iterator<Entity> annotate(Iterable<Entity> source);

	// alternative constructor that allows seamless chaining
	Iterator<Entity> annotate(Iterator<Entity> source);

	/**
	 * returns an entityMetaData containing the attributes the annotator will add
	 * 
	 * @return ouputMetadata
	 */
	List<AttributeMetaData> getOutputMetaData();

	/**
	 * Returns a entityMetaData containing the attributes needed for the annotator to work
	 * 
	 * @return inputMetaData;
	 */
	List<AttributeMetaData> getInputMetaData();

	/**
	 * Returns null if the annotator will work for the given metadata, a reason if not so
	 * 
	 * @param inputMetaData
	 * @return canAnnotate
	 */
	String canAnnotate(EntityMetaData inputMetaData);

	/**
	 * Return the name of the annotator
	 * 
	 * @return name
	 */
	String getSimpleName();

	String getFullName();

	CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer();

	default String getDescription()
	{
		return getInfo() == null ? "no description" : getInfo().getDescription();
	}

}
