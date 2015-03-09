package org.molgenis.ontology.beans;

import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.utils.OntologyServiceUtil;

/**
 * This function is used to parse the results from OntologyService
 * 
 * @author chaopang
 * 
 */
public class OntologyServiceResultImpl extends OntologyServiceResult
{
	public OntologyServiceResultImpl(String message)
	{
		super(message);
	}

	public OntologyServiceResultImpl(Map<String, Object> inputData, Iterable<? extends Entity> ontologyTerms,
			long totalHitCount)
	{
		super(inputData, OntologyServiceUtil.getEntityAsMap(ontologyTerms), totalHitCount);
	}
}