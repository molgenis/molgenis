package org.molgenis.data.jpa;

import java.util.List;

import org.molgenis.data.Entity;

/**
 * Entity interface t o be implemented by jpa entities (jpa enties are generated)
 */
public interface JpaEntity extends Entity
{
	List<String> getLabelAttributeNames();

	void set(Entity entity, boolean strict);
}
