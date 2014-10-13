package org.molgenis.data.semantic;

public interface Tag<SubjectType, ObjectType>
{

	String getRelationIRI();

	String getRelationLabel();

	SubjectType getSubject();

	ObjectType getObject();
}
