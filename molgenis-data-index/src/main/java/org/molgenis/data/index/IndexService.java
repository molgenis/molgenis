package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

public interface IndexService
{
	void createIndex(EntityType entityType);

	boolean hasIndex(EntityType entityType);

	void deleteIndex(EntityType entityType);

	void rebuildIndex(Repository<? extends Entity> repository);

	void refreshIndex();

	void index(EntityType entityType, Entity entity);

	long index(EntityType entityType, Stream<? extends Entity> entities);

	void delete(EntityType entityType, Entity entity);

	void delete(EntityType entityType, Stream<? extends Entity> entities);

	void deleteById(EntityType entityType, Object entityId);

	void deleteAll(EntityType entityType, Stream<Object> entityIds);
}
