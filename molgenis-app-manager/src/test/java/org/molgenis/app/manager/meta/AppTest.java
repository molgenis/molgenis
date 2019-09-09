package org.molgenis.app.manager.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {AppFactory.class, AppMetadata.class, AbstractMolgenisSpringTest.Config.class})
class AppTest extends AbstractMolgenisSpringTest {
  @Autowired private AppFactory appFactory;
  private App app;

  @BeforeEach
  void setup() {
    app = appFactory.create("id");
  }

  @Test
  void testConstructorParams() {
    app = appFactory.create();
    assertNotNull(app.getId());
  }

  @Test
  void testId() {
    assertEquals(app.getId(), "id");
    app.setId("id1");
    assertEquals(app.getId(), "id1");
  }

  @Test
  void testLabel() {
    assertNull(app.getLabel());
    app.setLabel("label");
    assertEquals(app.getLabel(), "label");
  }

  @Test
  void testDescription() {
    assertNull(app.getDescription());
    app.setDescription("description");
    assertEquals(app.getDescription(), "description");
  }

  @Test
  void testActive() {
    assertFalse(app.isActive());
    app.setActive(true);
    assertTrue(app.isActive());
  }

  @Test
  void testVersion() {
    assertNull(app.getAppVersion());
    app.setAppVersion("1.0.0");
    assertEquals(app.getAppVersion(), "1.0.0");
  }

  @Test
  void testDependency() {
    assertNull(app.getApiDependency());
    app.setApiDependency("7.9.1");
    assertEquals(app.getApiDependency(), "7.9.1");
  }

  @Test
  void testContent() {
    assertNull(app.getTemplateContent());
    app.setTemplateContent("<html></html>");
    assertEquals(app.getTemplateContent(), "<html></html>");
  }

  @Test
  void testResource() {
    assertNull(app.getResourceFolder());
    app.setResourceFolder("/blah");
    assertEquals(app.getResourceFolder(), "/blah");
  }

  @Test
  void testUri() {
    assertNull(app.getName());
    app.setName("app1");
    assertEquals(app.getName(), "app1");
  }

  @Test
  void testConfig() {
    assertNull(app.getAppConfig());
    app.setAppConfig("{key: value}");
    assertEquals(app.getAppConfig(), "{key: value}");
  }

  @Test
  void testFolder() {
    assertNull(app.getResourceFolder());
    app.setResourceFolder("/blah");
    assertEquals(app.getResourceFolder(), "/blah");
  }

  @Test
  void menuAndFooter() {
    assertFalse(app.includeMenuAndFooter());
    app.setIncludeMenuAndFooter(true);
    assertTrue(app.includeMenuAndFooter());
  }
}
