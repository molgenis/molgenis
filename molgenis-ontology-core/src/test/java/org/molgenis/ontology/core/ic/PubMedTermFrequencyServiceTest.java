package org.molgenis.ontology.core.ic;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PubMedTermFrequencyServiceTest
{
	PubMedTermFrequencyService termFrequencyService = new PubMedTermFrequencyService();

	@Test
	public void testRegexPattern()
	{
		assertEquals(termFrequencyService.parseResponse(
				"<eSearchResult><Count>4603</Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList>").getOccurrence(),
				4603);
		assertEquals(
				termFrequencyService.parseResponse("<eSearchResult><RetMax>20</RetMax><RetStart>0</RetStart><IdList>"),
				null);
		assertEquals(termFrequencyService.parseResponse(
				"<eSearchResult><Count></Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList>"), null);
	}
}
