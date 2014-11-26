package org.molgenis.data.semantic;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.util.IdGenerator;

/**
 * Helper class around the {@link TagMetaData} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */
public class TagRepository
{
	public static final TagMetaData META_DATA = new TagMetaData();
	private IdGenerator idGenerator;
	private CrudRepository repository;

	public TagRepository(CrudRepository repository, IdGenerator idGenerator)
	{
		this.repository = repository;
		this.idGenerator = idGenerator;
	}

	/**
	 * Fetches a tag from the repository. Creates a new one if it does not yet exist.
	 * 
	 * @param objectIri
	 *            IRI of the object
	 * @param label
	 *            label of the object
	 * @param relation
	 *            {@link Relation} of the tag
	 * @param codeSystemIRI
	 *            the IRI of the code system of the tag
	 * @return {@link Entity} of type {@link TagMetaData}
	 */
	public Entity getTagEntity(String objectIRI, String label, Relation relation, String codeSystemIRI)
	{
		Query q = new QueryImpl().eq(TagMetaData.OBJECT_IRI, objectIRI).and()
				.eq(TagMetaData.RELATION_IRI, relation.getIRI()).and().eq(TagMetaData.CODE_SYSTEM, codeSystemIRI);
		Entity result = repository.findOne(q);
		if (result == null)
		{
			MapEntity mapEntity = new MapEntity(TagMetaData.ENTITY_NAME);
			mapEntity.set(TagMetaData.IDENTIFIER, idGenerator.generateId().toString());
			mapEntity.set(TagMetaData.OBJECT_IRI, objectIRI);
			mapEntity.set(TagMetaData.LABEL, label);
			mapEntity.set(TagMetaData.RELATION_IRI, relation.getIRI());
			mapEntity.set(TagMetaData.RELATION_LABEL, relation.getLabel());
			mapEntity.set(TagMetaData.CODE_SYSTEM, codeSystemIRI);
			repository.add(mapEntity);
			result = mapEntity;
		}
		return result;
	}
}
