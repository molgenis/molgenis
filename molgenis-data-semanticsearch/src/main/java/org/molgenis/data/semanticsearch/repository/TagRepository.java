package org.molgenis.data.semanticsearch.repository;

import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.TagMetaData;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;

/**
 * Helper class around the {@link TagMetaData} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */

public class TagRepository
{
	private final IdGenerator idGenerator;
	private final Repository<Entity> repository;

	public TagRepository(Repository<Entity> repository, IdGenerator idGenerator)
	{
		this.repository = repository;
		this.idGenerator = idGenerator;
	}

	/**
	 * Fetches a tag from the repository. Creates a new one if it does not yet exist.
	 *
	 * @param objectIRI     IRI of the object
	 * @param label         label of the object
	 * @param relation      {@link Relation} of the tag
	 * @param codeSystemIRI the IRI of the code system of the tag
	 * @return {@link Entity} of type {@link TagMetaData}
	 */
	public Entity getTagEntity(String objectIRI, String label, Relation relation, String codeSystemIRI)
	{
		Query<Entity> q = new QueryImpl<Entity>().eq(TagMetaData.OBJECT_IRI, objectIRI).and()
				.eq(TagMetaData.RELATION_IRI, relation.getIRI()).and().eq(TagMetaData.CODE_SYSTEM, codeSystemIRI);
		Entity result = repository.findOne(q);
		if (result == null)
		{
			Entity entity = new DynamicEntity(null); // FIXME pass entity meta data instead of null
			entity.set(TagMetaData.IDENTIFIER, idGenerator.generateId());
			entity.set(TagMetaData.OBJECT_IRI, objectIRI);
			entity.set(TagMetaData.LABEL, label);
			entity.set(TagMetaData.RELATION_IRI, relation.getIRI());
			entity.set(TagMetaData.RELATION_LABEL, relation.getLabel());
			entity.set(TagMetaData.CODE_SYSTEM, codeSystemIRI);
			repository.add(entity);
			result = entity;
		}
		return result;
	}
}
