package org.molgenis.ontology.utils;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY_TERMS;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.COMBINED_SCORE;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.SCORE;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.meta.Ontology;
import org.molgenis.ontology.core.meta.OntologyFactory;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTerm;
import org.molgenis.ontology.core.meta.OntologyTermFactory;
import org.molgenis.ontology.core.meta.OntologyTermSynonym;
import org.molgenis.ontology.core.meta.OntologyTermSynonymFactory;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData;
import org.molgenis.ontology.utils.SortaServiceUtilTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {Config.class})
public class SortaServiceUtilTest extends AbstractMolgenisSpringTest {
  private static final String ONTOLOGY_IRI = "http://www.molgenis.org/";

  @Autowired private OntologyFactory ontologyFactory;

  @Autowired private OntologyTermFactory ontologyTermFactory;

  @Autowired private OntologyTermSynonymFactory ontologyTermSynonymFactory;

  private OntologyTerm term;

  @BeforeMethod
  public void setUp() {
    OntologyTermSynonym synonym = ontologyTermSynonymFactory.create();
    synonym.setId("1");
    synonym.setOntologyTermSynonym("synonym");
    synonym.set(SCORE, 10.0);
    synonym.set(COMBINED_SCORE, 20.0);
    Ontology ontology = ontologyFactory.create();
    ontology.setOntologyIri(ONTOLOGY_IRI);
    ontology.setId("1");
    ontology.setOntologyName("testOntology");
    term = ontologyTermFactory.create();
    term.setOntology(ontology);
    term.setOntologyTermName("testTerm");
    term.setOntologyTermIri("iri");
    term.setOntologyTermSynonyms(singletonList(synonym));
    ontology.set(ONTOLOGY_TERMS, singletonList(term));
  }

  @Test
  public void testGetEntityAsMap() {
    HashMap<String, Object> expected = newHashMap();
    expected.put(ID, null);
    expected.put(ONTOLOGY_TERM_IRI, "iri");
    expected.put(ONTOLOGY_TERM_NAME, "testTerm");
    expected.put(
        ONTOLOGY_TERM_SYNONYM,
        singletonList(
            ImmutableMap.of(
                OntologyTermSynonymMetaData.ID,
                "1",
                ONTOLOGY_TERM_SYNONYM_ATTR,
                "synonym",
                SCORE,
                10.0,
                COMBINED_SCORE,
                20.0)));
    expected.put(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, null);
    expected.put(ONTOLOGY_TERM_NODE_PATH, null);
    expected.put(
        ONTOLOGY,
        ImmutableMap.of(
            OntologyMetaData.ID,
            "1",
            OntologyMetaData.ONTOLOGY_IRI,
            ONTOLOGY_IRI,
            OntologyMetaData.ONTOLOGY_NAME,
            "testOntology"));

    assertEquals(SortaServiceUtil.getEntityAsMap(singletonList(term)), singletonList(expected));
  }

  @Configuration
  @Import({OntologyTestConfig.class, OntologyTermHitMetaData.class})
  public static class Config {}
}
