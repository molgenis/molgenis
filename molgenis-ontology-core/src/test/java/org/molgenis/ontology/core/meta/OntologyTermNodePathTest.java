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
      OntologyTermNodePathMetadata.class,
      OntologyTermNodePathFactory.class,
      OntologyPackage.class,
      OntologyTestConfig.class
    })
public class OntologyTermNodePathTest extends AbstractSystemEntityTest {

  @Autowired OntologyTermNodePathMetadata metadata;
  @Autowired OntologyTermNodePathFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        OntologyTermNodePath.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
