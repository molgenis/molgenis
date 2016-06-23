package org.molgenis.data.semanticsearch.repository;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.TagMetaData.CODE_SYSTEM;
import static org.molgenis.data.meta.model.TagMetaData.OBJECT_IRI;
import static org.molgenis.data.meta.model.TagMetaData.RELATION_IRI;

import org.molgenis.data.IdGenerator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetaData;
import org.molgenis.data.semantic.Relation;

/**
 * Helper class around the {@link TagMetaData} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */

public class TagRepository
{
	private final Repository<Tag> repository;
	private final IdGenerator idGenerator;
	private final TagFactory tagFactory;

	public TagRepository(Repository<Tag> repository, IdGenerator idGenerator, TagFactory tagFactory)
	{
		this.repository = requireNonNull(repository);
		this.idGenerator = requireNonNull(idGenerator);
		this.tagFactory = requireNonNull(tagFactory);
	}

	/**
	 * Fetches a tag from the repository. Creates a new one if it does not yet exist.
	 *
	 * @param objectIRI     IRI of the object
	 * @param label         label of the object
	 * @param relation      {@link Relation} of the tag
	 * @param codeSystemIRI the IRI of the code system of the tag
	 * @return {@link Tag} of type {@link TagMetaData}
	 */
	public Tag getTagEntity(String objectIRI, String label, Relation relation, String codeSystemIRI)
	{
		Tag tag = repository.query().eq(OBJECT_IRI, objectIRI).and().eq(RELATION_IRI, relation.getIRI()).and()
				.eq(CODE_SYSTEM, codeSystemIRI).findOne();
		if (tag == null)
		{
			tag = tagFactory.create();
			tag.setIdentifier(idGenerator.generateId());
			tag.setObjectIri(objectIRI);
			tag.setLabel(label);
			tag.setRelationIri(relation.getIRI());
			tag.setRelationLabel(relation.getLabel());
			tag.setCodeSystem(codeSystemIRI);
			repository.add(tag);
		}
		return tag;
	}
}
