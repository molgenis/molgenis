package org.molgenis.data.semantic;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.TagMetaData;

public class TagImpl<SubjectType, ObjectType, CodeSystemType> implements Tag<SubjectType, ObjectType, CodeSystemType>
{
	private final SubjectType subject;
	private final Relation relation;
	private final ObjectType object;
	private final CodeSystemType codeSystem;
	private final String identifier;

	public static <SubjectType> TagImpl<SubjectType, LabeledResource, LabeledResource> asTag(SubjectType subjectType,
			Entity tagEntity)
	{
		String identifier = tagEntity.getString(TagMetaData.IDENTIFIER);
		String relationIri = tagEntity.getString(TagMetaData.RELATION_IRI);
		Relation relation = Relation.forIRI(relationIri);
		if (relation == null)
		{
			throw new IllegalArgumentException("Unknown relation iri [" + relationIri + "]");
		}

		LabeledResource codeSystem = null;
		if (tagEntity.getString(TagMetaData.CODE_SYSTEM) != null)
		{
			codeSystem = new LabeledResource(tagEntity.getString(TagMetaData.CODE_SYSTEM),
					tagEntity.getString(TagMetaData.CODE_SYSTEM));
		}

		LabeledResource objectResource = new LabeledResource(tagEntity.getString(TagMetaData.OBJECT_IRI),
				tagEntity.getString(TagMetaData.LABEL));

		return new TagImpl<SubjectType, LabeledResource, LabeledResource>(identifier, subjectType, relation,
				objectResource, codeSystem);
	}

	public TagImpl(String identifier, SubjectType subject, Relation relation, ObjectType object,
			CodeSystemType codeSystem)
	{
		this.identifier = identifier;
		this.subject = subject;
		this.relation = relation;
		this.object = object;
		this.codeSystem = codeSystem;
	}

	@Override
	public SubjectType getSubject()
	{
		return subject;
	}

	@Override
	public Relation getRelation()
	{
		return relation;
	}

	@Override
	public ObjectType getObject()
	{
		return object;
	}

	@Override
	public CodeSystemType getCodeSystem()
	{
		return codeSystem;
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codeSystem == null) ? 0 : codeSystem.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TagImpl other = (TagImpl) obj;
		if (codeSystem == null)
		{
			if (other.codeSystem != null) return false;
		}
		else if (!codeSystem.equals(other.codeSystem)) return false;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		if (object == null)
		{
			if (other.object != null) return false;
		}
		else if (!object.equals(other.object)) return false;
		if (relation != other.relation) return false;
		if (subject == null)
		{
			if (other.subject != null) return false;
		}
		else if (!subject.equals(other.subject)) return false;
		return true;
	}

}
