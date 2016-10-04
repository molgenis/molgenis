package org.molgenis.ontology.repository.model;

import org.molgenis.ontology.core.model.CombinedOntologyTermImpl;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OntologyTermTest
{
	@Test
	public void testAnd()
	{
		OntologyTermImpl term1 = OntologyTermImpl.create("1", "iri1", "label 1");
		OntologyTermImpl term2 = OntologyTermImpl.create("2", "iri2", "label 2");
		OntologyTermImpl term3 = OntologyTermImpl.create("3", "iri3", "label 3");

		Assert.assertEquals(CombinedOntologyTermImpl.create("iri1,iri2", "(label 1 and label 2)"),
				CombinedOntologyTermImpl.and(term1, term2));
		Assert.assertEquals(CombinedOntologyTermImpl.create("iri1,iri2,iri3", "(label 1 and label 2 and label 3)"),
				CombinedOntologyTermImpl.and(term1, term2, term3));
	}
}
