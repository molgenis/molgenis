package org.molgenis.data.semantic;

public class TagImpl<SubjectType, ObjectType, CodeSystemType> implements Tag<SubjectType, ObjectType, CodeSystemType>
{
	private SubjectType subject;
	private Relation relation;
	private ObjectType object;
	private CodeSystemType codeSystem;

	public TagImpl(SubjectType subject, Relation relation, ObjectType object, CodeSystemType codeSystem)
	{
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codeSystem == null) ? 0 : codeSystem.hashCode());
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
		TagImpl<?, ?, ?> other = (TagImpl<?, ?, ?>) obj;
		if (codeSystem == null)
		{
			if (other.codeSystem != null) return false;
		}
		else if (!codeSystem.equals(other.codeSystem)) return false;
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
