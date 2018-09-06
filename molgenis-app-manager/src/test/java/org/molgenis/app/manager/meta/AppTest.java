package org.molgenis.app.manager.meta;

import static org.testng.Assert.*;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {AppFactory.class, AppMetadata.class, AbstractMolgenisSpringTest.Config.class})
public class AppTest extends AbstractMolgenisSpringTest {
  @Autowired private AppFactory appFactory;
  private App app;

  @BeforeMethod
  public void setup() {
    app = appFactory.create("id");
  }

  @Test
  public void testConstructorParams() {
    app = appFactory.create();
    assertNotNull(app.getId());
  }

  @Test
  public void testId() {
    assertEquals(app.getId(), "id");
    app.setId("id1");
    assertEquals(app.getId(), "id1");
  }

  @Test
  public void testLabel() {
    assertNull(app.getLabel());
    app.setLabel("label");
    assertEquals(app.getLabel(), "label");
  }

  @Test
  public void testDescription() {
    assertNull(app.getDescription());
    app.setDescription("description");
    assertEquals(app.getDescription(), "description");
  }

  @Test
  public void testActive() {
    assertFalse(app.isActive());
    app.setActive(true);
    assertTrue(app.isActive());
  }

  @Test
  public void testVersion() {
    assertNull(app.getAppVersion());
    app.setAppVersion("1.0.0");
    assertEquals(app.getAppVersion(), "1.0.0");
  }

  @Test
  public void testDependency() {
    assertNull(app.getApiDependency());
    app.setApiDependency("7.9.1");
    assertEquals(app.getApiDependency(), "7.9.1");
  }

  @Test
  public void testContent() {
    assertNull(app.getTemplateContent());
    app.setTemplateContent("<html></html>");
    assertEquals(app.getTemplateContent(), "<html></html>");
  }

  @Test
  public void testResource() {
    assertNull(app.getResourceFolder());
    app.setResourceFolder("/blah");
    assertEquals(app.getResourceFolder(), "/blah");
  }

  @Test
  public void testUri() {
    assertNull(app.getName());
    app.setName("app1");
    assertEquals(app.getName(), "app1");
  }

  @Test
  public void testConfig() {
    assertNull(app.getAppConfig());
    app.setAppConfig("{key: value}");
    assertEquals(app.getAppConfig(), "{key: value}");
  }

  @Test
  public void testFolder() {
    assertNull(app.getResourceFolder());
    app.setResourceFolder("/blah");
    assertEquals(app.getResourceFolder(), "/blah");
  }

  @Test
  public void menuAndFooter() {
    assertFalse(app.includeMenuAndFooter());
    app.setIncludeMenuAndFooter(true);
    assertTrue(app.includeMenuAndFooter());
  }
}
