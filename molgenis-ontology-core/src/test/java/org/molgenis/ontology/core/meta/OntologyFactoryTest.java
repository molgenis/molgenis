package org.molgenis.ontology.core.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OntologyMetadata.class,
      OntologyFactory.class,
      OntologyPackage.class,
      OntologyTestConfig.class
    })
public class OntologyFactoryTest extends AbstractEntityFactoryTest {

  @Autowired OntologyFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Ontology.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Ontology.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Ontology.class);
  }
}
