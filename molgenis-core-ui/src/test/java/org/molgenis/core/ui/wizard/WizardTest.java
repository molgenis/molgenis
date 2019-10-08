package org.molgenis.core.ui.wizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;

class WizardTest {

  @Test
  void addPage() {
    Wizard wizard = new Wizard();
    wizard.addPage(new TestWizardPage());
    assertEquals(1, wizard.getPages().size());
  }

  @Test
  void getCurrentPage() {
    Wizard wizard = new Wizard();
    WizardPage page = new TestWizardPage();
    wizard.addPage(page);
    assertEquals(page, wizard.getCurrentPage());
  }

  @Test
  void getNextButton() {
    Wizard wizard = new Wizard();
    wizard.addPage(new TestWizardPage());
    wizard.addPage(new TestWizardPage());

    WizardButton nextButton = wizard.getNextButton();
    assertNotNull(nextButton);
    assertTrue(nextButton.isEnabled());
    assertEquals("Next", nextButton.getTitle());
    assertEquals("/next", nextButton.getTargetUri());

    wizard.next();
    nextButton = wizard.getNextButton();
    assertTrue(nextButton.isEnabled());
    assertEquals("Finish", nextButton.getTitle());
    assertEquals("/restart", nextButton.getTargetUri());
  }

  @Test
  void getPages() {
    Wizard wizard = new Wizard();
    assertNotNull(wizard.getPages());
    assertEquals(0, wizard.getPages().size());

    wizard.addPage(new TestWizardPage());
    assertEquals(1, wizard.getPages().size());

    wizard.addPage(new TestWizardPage());
    assertEquals(2, wizard.getPages().size());
  }

  @Test
  void getPreviousButton() {
    Wizard wizard = new Wizard();
    wizard.addPage(new TestWizardPage());
    WizardButton prevButton = wizard.getPreviousButton();
    assertNotNull(prevButton);
    assertFalse(prevButton.isEnabled());
    assertEquals("Previous", prevButton.getTitle());
  }

  @Test
  void isFirstPage() {
    Wizard wizard = new Wizard();
    wizard.addPage(new TestWizardPage());
    wizard.addPage(new TestWizardPage());
    assertTrue(wizard.isFirstPage());
    wizard.next();
    assertFalse(wizard.isFirstPage());
  }

  @Test
  void isLastPage() {
    Wizard wizard = new Wizard();
    wizard.addPage(new TestWizardPage());
    wizard.addPage(new TestWizardPage());
    assertFalse(wizard.isLastPage());
    wizard.next();
    assertTrue(wizard.isLastPage());
  }

  @Test
  void next() {
    Wizard wizard = new Wizard();
    WizardPage p1 = new TestWizardPage();
    wizard.addPage(p1);
    WizardPage p2 = new TestWizardPage();
    wizard.addPage(p2);

    assertEquals(p1, wizard.getCurrentPage());
    wizard.next();
    assertEquals(p2, wizard.getCurrentPage());
  }

  @Test
  void previous() {
    Wizard wizard = new Wizard();
    WizardPage p1 = new TestWizardPage();
    wizard.addPage(p1);
    WizardPage p2 = new TestWizardPage();
    wizard.addPage(p2);
    wizard.next();
    assertEquals(p2, wizard.getCurrentPage());
    wizard.previous();
    assertEquals(p1, wizard.getCurrentPage());
  }

  private static class TestWizardPage extends AbstractWizardPage {
    private static final long serialVersionUID = 1L;

    @Override
    public String getTitle() {
      return "test";
    }

    @Override
    public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard) {
      return null;
    }
  }
}
