package org.molgenis.ontology.core.ic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PubMedTermFrequencyServiceTest {
  PubMedTermFrequencyService termFrequencyService = new PubMedTermFrequencyService();

  @Test
  void testRegexPattern() {
    assertEquals(
        termFrequencyService
            .parseResponse(
                "<eSearchResult><Count>4603</Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList>")
            .getOccurrence(),
        4603);
    assertEquals(
        termFrequencyService.parseResponse(
            "<eSearchResult><RetMax>20</RetMax><RetStart>0</RetStart><IdList>"),
        null);
    assertEquals(
        termFrequencyService.parseResponse(
            "<eSearchResult><Count></Count><RetMax>20</RetMax><RetStart>0</RetStart><IdList>"),
        null);
  }
}
