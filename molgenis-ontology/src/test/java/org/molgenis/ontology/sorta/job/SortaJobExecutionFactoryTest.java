package org.molgenis.ontology.sorta.job;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      SortaJobExecutionMetadata.class,
      SortaJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class SortaJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired SortaJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, SortaJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, SortaJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, SortaJobExecution.class);
  }
}
