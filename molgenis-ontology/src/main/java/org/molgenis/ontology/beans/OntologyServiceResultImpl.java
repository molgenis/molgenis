package org.molgenis.ontology.beans;

import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.semantic.OntologyServiceResult;
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

	public OntologyServiceResultImpl(Map<String, Object> inputData, Iterable<Entity> ontologyTerms, long totalHitCount)
	{
		super(inputData, ontologyTerms, totalHitCount);
		this.ontologyTerms = OntologyServiceUtil.getEntityAsMap(ontologyTerms);
	}
}