package org.molgenis.ontology.repository.model;

import org.molgenis.ontology.core.model.CombinedOntologyTerm;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OntologyTagObjectTest
{
	@Test
	public void testAnd()
	{
		OntologyTerm term1 = OntologyTerm.create("1", "iri1", "label 1");
		OntologyTerm term2 = OntologyTerm.create("2", "iri2", "label 2");
		OntologyTerm term3 = OntologyTerm.create("3", "iri3", "label 3");

		Assert.assertEquals(CombinedOntologyTerm.create("iri1,iri2", "(label 1 and label 2)"),
				CombinedOntologyTerm.and(term1, term2));
		Assert.assertEquals(CombinedOntologyTerm.create("iri1,iri2,iri3", "(label 1 and label 2 and label 3)"),
				CombinedOntologyTerm.and(term1, term2, term3));
	}
}
