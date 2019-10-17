package org.molgenis.ontology.core.meta;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OntologyMetadata.class,
      OntologyFactory.class,
      OntologyPackage.class,
      OntologyTestConfig.class
    })
public class OntologyTest extends AbstractSystemEntityTest {

  @Autowired OntologyMetadata metadata;
  @Autowired OntologyFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, Ontology.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
