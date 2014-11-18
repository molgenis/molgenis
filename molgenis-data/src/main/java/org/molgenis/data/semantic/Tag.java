package org.molgenis.data.semantic;


public interface Tag<SubjectType, ObjectType, CodeSystemType>
{
	Relation getRelation();

	SubjectType getSubject();

	ObjectType getObject();

	CodeSystemType getCodeSystem();

	String getIdentifier();
}
