package org.molgenis.semanticsearch.repository;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.Relation;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.TagMetadata.*;

/**
 * Helper class around the {@link TagMetadata} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */

public class TagRepository
{
	private final DataService dataService;
	private final IdGenerator idGenerator;
	private final TagFactory tagFactory;

	public TagRepository(DataService dataService, IdGenerator idGenerator, TagFactory tagFactory)
	{
		this.dataService = requireNonNull(dataService);
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
	 * @return {@link Tag} of type {@link TagMetadata}
	 */
	public Tag getTagEntity(String objectIRI, String label, Relation relation, String codeSystemIRI)
	{
		Tag tag = dataService.query(TAG, Tag.class)
							 .eq(OBJECT_IRI, objectIRI)
							 .and()
							 .eq(RELATION_IRI, relation.getIRI())
							 .and()
							 .eq(CODE_SYSTEM, codeSystemIRI)
							 .findOne();
		if (tag == null)
		{
			tag = tagFactory.create();
			tag.setId(idGenerator.generateId());
			tag.setObjectIri(objectIRI);
			tag.setLabel(label);
			tag.setRelationIri(relation.getIRI());
			tag.setRelationLabel(relation.getLabel());
			tag.setCodeSystem(codeSystemIRI);
			dataService.add(TAG, tag);
		}
		return tag;
	}
}
