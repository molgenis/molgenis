package org.molgenis.data.security.auth;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.security.config.GroupTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {EntityBaseTestConfig.class, GroupMetadata.class,
    GroupFactory.class, SecurityPackage.class, GroupTestConfig.class})
public class GroupFactoryTest extends AbstractEntityFactoryTest {

  @Autowired
  GroupFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Group.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Group.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Group.class);
  }
}