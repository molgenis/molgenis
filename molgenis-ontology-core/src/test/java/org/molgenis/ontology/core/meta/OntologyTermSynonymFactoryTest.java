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
      OntologyTermSynonymMetadata.class,
      OntologyTermSynonymFactory.class,
      OntologyPackage.class,
      OntologyTestConfig.class
    })
public class OntologyTermSynonymFactoryTest extends AbstractEntityFactoryTest {

  @Autowired OntologyTermSynonymFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, OntologyTermSynonym.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, OntologyTermSynonym.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, OntologyTermSynonym.class);
  }
}
