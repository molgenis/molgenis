package org.molgenis.vortext;

import java.util.List;

public interface PublicationService
{
	boolean exists(String publicationId);

	void addAnnotationGroups(String publicationId, List<AnnotationGroup> annotationGroups);
}
