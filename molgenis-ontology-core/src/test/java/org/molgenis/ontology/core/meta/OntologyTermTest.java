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
      OntologyTermMetadata.class,
      OntologyTermFactory.class,
      OntologyPackage.class,
      OntologyTestConfig.class
    })
public class OntologyTermTest extends AbstractSystemEntityTest {

  @Autowired OntologyTermMetadata metadata;
  @Autowired OntologyTermFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, OntologyTerm.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
