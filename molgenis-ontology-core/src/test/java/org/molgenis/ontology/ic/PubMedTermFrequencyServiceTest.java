package org.molgenis.ontology.ic;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class PubMedTermFrequencyServiceTest
{
	PubMedTermFrequencyService termFrequencyService = new PubMedTermFrequencyService();

	@Test
	public void testRegexPattern()
	{
		assertEquals(termFrequencyService
				.parseResponse("<eSearchResult><Count>46030</Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList>")
				.getOccurrence(), 46030);
		assertEquals(
				termFrequencyService.parseResponse("<eSearchResult><RetMax>20</RetMax><RetStart>0</RetStart><IdList>"),
				null);
		assertEquals(termFrequencyService.parseResponse(
				"<eSearchResult><Count></Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList>"), null);
	}
}
