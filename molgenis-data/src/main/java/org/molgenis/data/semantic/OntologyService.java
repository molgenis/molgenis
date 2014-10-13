package org.molgenis.data.semantic;

/**
 * Makes Ontology trees manageable
 * 
 * @author chaopang
 * 
 */
public interface OntologyService
{
	void importOntology();

	OntologyTerm getOntologyTerm(String ontologyTermIRI, String codeSystemIRI);

	/**
	 * 
	 * @param queryTerm
	 * @param codeSystemIRI
	 * @return a list of ontology terms sorted based on the relevance
	 */
	Iterable<OntologyTerm> findOntologyTerms(String queryTerm, String codeSystemIRI);
}
