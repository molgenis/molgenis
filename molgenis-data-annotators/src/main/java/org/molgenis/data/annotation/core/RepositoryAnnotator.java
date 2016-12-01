package org.molgenis.data.annotation.core;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.List;

/**
 * interface for annotators. annotators take an iterator and return an iterator with some information added or updated
 */

public interface RepositoryAnnotator
{
	String ANNOTATOR_PREFIX = "mgs_ann_";

	AnnotatorInfo getInfo();

	// add entityAnnotator
	default Iterator<Entity> annotate(Iterable<Entity> source, boolean updateMode)
	{
		if (updateMode == true)
		{
			throw new MolgenisDataException("This annotator/filter does not support updating of values");
		}
		return this.annotate(source);
	}

	Iterator<Entity> annotate(Iterable<Entity> source);

	/**
	 * Checks if folder and files that were set with a runtime property actually exist, or if a webservice can be
	 * reached
	 *
	 * @return boolean
	 */
	boolean annotationDataExists();

	// alternative constructor that allows seamless chaining
	Iterator<Entity> annotate(Iterator<Entity> source);

	/**
	 * returns an EntityType containing the attributes the annotator will add
	 *
	 * @return ouputMetadata
	 */
	List<Attribute> getOutputAttributes();

	/**
	 * Returns a EntityType containing the attributes needed for the annotator to work
	 *
	 * @return inputMetaData;
	 */
	List<Attribute> getRequiredAttributes();

	/**
	 * Returns null if the annotator will work for the given metadata, a reason if not so
	 *
	 * @param inputMetaData
	 * @return canAnnotate
	 */
	String canAnnotate(EntityType inputMetaData);

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

	List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory);
}
