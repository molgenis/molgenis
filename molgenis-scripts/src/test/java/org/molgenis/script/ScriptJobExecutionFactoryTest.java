package org.molgenis.script;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ScriptJobExecutionMetadata.class,
      ScriptJobExecutionFactory.class,
      JobExecutionMetaData.class,
      JobPackage.class
    })
public class ScriptJobExecutionFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ScriptJobExecutionFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ScriptJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ScriptJobExecution.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ScriptJobExecution.class);
  }
}
