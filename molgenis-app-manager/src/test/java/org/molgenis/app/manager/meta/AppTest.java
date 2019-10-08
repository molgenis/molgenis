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
    assertEquals("id", app.getId());
    app.setId("id1");
    assertEquals("id1", app.getId());
  }

  @Test
  void testLabel() {
    assertNull(app.getLabel());
    app.setLabel("label");
    assertEquals("label", app.getLabel());
  }

  @Test
  void testDescription() {
    assertNull(app.getDescription());
    app.setDescription("description");
    assertEquals("description", app.getDescription());
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
    assertEquals("1.0.0", app.getAppVersion());
  }

  @Test
  void testDependency() {
    assertNull(app.getApiDependency());
    app.setApiDependency("7.9.1");
    assertEquals("7.9.1", app.getApiDependency());
  }

  @Test
  void testContent() {
    assertNull(app.getTemplateContent());
    app.setTemplateContent("<html></html>");
    assertEquals("<html></html>", app.getTemplateContent());
  }

  @Test
  void testResource() {
    assertNull(app.getResourceFolder());
    app.setResourceFolder("/blah");
    assertEquals("/blah", app.getResourceFolder());
  }

  @Test
  void testUri() {
    assertNull(app.getName());
    app.setName("app1");
    assertEquals("app1", app.getName());
  }

  @Test
  void testConfig() {
    assertNull(app.getAppConfig());
    app.setAppConfig("{key: value}");
    assertEquals("{key: value}", app.getAppConfig());
  }

  @Test
  void testFolder() {
    assertNull(app.getResourceFolder());
    app.setResourceFolder("/blah");
    assertEquals("/blah", app.getResourceFolder());
  }

  @Test
  void menuAndFooter() {
    assertFalse(app.includeMenuAndFooter());
    app.setIncludeMenuAndFooter(true);
    assertTrue(app.includeMenuAndFooter());
  }
}
