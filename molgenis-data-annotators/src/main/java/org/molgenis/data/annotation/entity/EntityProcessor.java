package org.molgenis.data.annotation.entity;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;

import java.util.Collection;

/**
 * A step to use in the Annotator. Each step has requirements on the annotated entity's metadata.
 * 
 * @author fkelpin
 *
 */
public interface EntityProcessor
{
	/**
	 * @return {@link Collection} of atomic {@link AttributeMetaData} that must be present in the annotated
	 *         {@link Entity}.
	 */
	Collection<AttributeMetaData> getRequiredAttributes();
}
