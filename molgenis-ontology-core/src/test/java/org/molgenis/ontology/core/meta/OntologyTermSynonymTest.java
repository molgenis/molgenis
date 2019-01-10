package org.molgenis.ontology.core.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OntologyTermSynonymMetadata.class,
      OntologyTermSynonymFactory.class,
      OntologyPackage.class,
      OntologyTestConfig.class
    })
public class OntologyTermSynonymTest extends AbstractSystemEntityTest {

  @Autowired OntologyTermSynonymMetadata metadata;
  @Autowired OntologyTermSynonymFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        OntologyTermSynonym.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
