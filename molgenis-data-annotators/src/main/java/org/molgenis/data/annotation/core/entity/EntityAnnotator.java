package org.molgenis.data.annotation.core.entity;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;

import java.util.List;

/**
 * Simple interface to implement for fine-grained annotators.
 *
 * @author fkelpin
 */
public interface EntityAnnotator extends Annotator
{
	/**
	 * Annotates a single entity.
	 *
	 * @param sourceEntity the entity to annotate
	 * @param updateMode   boolean indicating if existing annotations should be updated
	 * @return {@link Iterable} for the annotated data
	 */
	Iterable<Entity> annotateEntity(Entity sourceEntity, boolean updateMode);

	/**
	 * @return the {@link List< Attribute >} that must be present in the source entity.
	 */
	List<Attribute> getRequiredAttributes();

	/**
	 * @return Indication if the required source repository is present in the dataService
	 */
	boolean sourceExists();

	CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer();
}
