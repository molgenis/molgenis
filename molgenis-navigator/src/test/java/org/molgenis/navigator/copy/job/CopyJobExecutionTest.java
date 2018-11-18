package org.molgenis.navigator.copy.job;

import static freemarker.template.utility.Collections12.singletonList;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.jobs.model.JobExecutionMetaData.TYPE;
import static org.molgenis.navigator.copy.job.CopyJobExecutionMetadata.COPY_JOB_TYPE;
import static org.molgenis.navigator.copy.job.CopyJobExecutionMetadata.RESOURCES;
import static org.molgenis.navigator.copy.job.CopyJobExecutionMetadata.TARGET_PACKAGE;
import static org.testng.Assert.assertEquals;

import com.google.gson.Gson;
import org.mockito.stubbing.Answer;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class CopyJobExecutionTest extends AbstractMockitoTest {

  @Test
  public void testConstructor() {
    Entity entity = mock(Entity.class);
    new CopyJobExecution(entity);
    verify(entity).set(TYPE, COPY_JOB_TYPE);
  }

  @Test
  public void testConstructor2() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttribute = mock(Attribute.class);
    when(entityType.getAttribute(TYPE)).thenReturn(typeAttribute);
    when(typeAttribute.getDataType()).thenReturn(AttributeType.STRING);
    CopyJobExecution copyJobExecution = new CopyJobExecution(entityType);
    assertEquals(copyJobExecution.getType(), COPY_JOB_TYPE);
  }

  @Test
  public void testConstructor3() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttribute(any(String.class)))
        .thenAnswer(
            (Answer)
                invocation -> {
                  Attribute attribute = mock(Attribute.class);
                  when(attribute.getDataType()).thenReturn(AttributeType.STRING);
                  return attribute;
                });

    CopyJobExecution copyJobExecution = new CopyJobExecution("", entityType);
    assertEquals(copyJobExecution.getType(), COPY_JOB_TYPE);
  }

  @Test
  public void testSetResources() {
    Entity entity = mock(Entity.class);
    CopyJobExecution copyJobExecution = new CopyJobExecution(entity);
    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "id1");
    ResourceIdentifier id2 = ResourceIdentifier.create(ResourceType.PACKAGE, "id2");
    copyJobExecution.setResources(asList(id1, id2));
    verify(entity).set(eq(RESOURCES), any(String.class));
  }

  @Test
  public void testGetResources() {
    Entity entity = mock(Entity.class);
    CopyJobExecution copyJobExecution = new CopyJobExecution(entity);
    ResourceIdentifier id1 = ResourceIdentifier.create(ResourceType.ENTITY_TYPE, "id1");
    when(entity.getString(RESOURCES)).thenReturn(new Gson().toJson(singletonList(id1)));
    copyJobExecution.getResources();
    verify(entity).getString(RESOURCES);
  }

  @Test
  public void testSetTargetPackage() {
    Entity entity = mock(Entity.class);
    CopyJobExecution copyJobExecution = new CopyJobExecution(entity);
    copyJobExecution.setTargetPackage("test");
    verify(entity).set(TARGET_PACKAGE, "test");
  }

  @Test
  public void testGetTargetPackage() {
    Entity entity = mock(Entity.class);
    CopyJobExecution copyJobExecution = new CopyJobExecution(entity);
    copyJobExecution.getTargetPackage();
    verify(entity).getString(TARGET_PACKAGE);
  }
}
