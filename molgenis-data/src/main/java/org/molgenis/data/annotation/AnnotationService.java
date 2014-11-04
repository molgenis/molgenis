package org.molgenis.data.annotation;

import java.util.List;

import org.molgenis.data.EntityMetaData;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 19/02/14 Time: 12:50 To change this template use File | Settings |
 * File Templates.
 */
public interface AnnotationService
{

	void addAnnotator(RepositoryAnnotator newAnnotator);

	RepositoryAnnotator getAnnotatorByName(String annotatorName);

	List<RepositoryAnnotator> getAllAnnotators();

	List<RepositoryAnnotator> getAnnotatorsByMetaData(EntityMetaData metaData);
	
}
