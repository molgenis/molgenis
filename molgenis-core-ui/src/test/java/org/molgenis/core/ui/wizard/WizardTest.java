package org.molgenis.core.ui.wizard;

import org.springframework.validation.BindingResult;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.testng.Assert.*;

public class WizardTest
{

	@Test
	public void addPage()
	{
		Wizard wizard = new Wizard();
		wizard.addPage(new TestWizardPage());
		assertEquals(wizard.getPages().size(), 1);
	}

	@Test
	public void getCurrentPage()
	{
		Wizard wizard = new Wizard();
		WizardPage page = new TestWizardPage();
		wizard.addPage(page);
		assertEquals(wizard.getCurrentPage(), page);
	}

	@Test
	public void getNextButton()
	{
		Wizard wizard = new Wizard();
		wizard.addPage(new TestWizardPage());
		wizard.addPage(new TestWizardPage());

		WizardButton nextButton = wizard.getNextButton();
		assertNotNull(nextButton);
		assertTrue(nextButton.isEnabled());
		assertEquals(nextButton.getTitle(), "Next");
		assertEquals(nextButton.getTargetUri(), "/next");

		wizard.next();
		nextButton = wizard.getNextButton();
		assertTrue(nextButton.isEnabled());
		assertEquals(nextButton.getTitle(), "Finish");
		assertEquals(nextButton.getTargetUri(), "/restart");
	}

	@Test
	public void getPages()
	{
		Wizard wizard = new Wizard();
		assertNotNull(wizard.getPages());
		assertEquals(wizard.getPages().size(), 0);

		wizard.addPage(new TestWizardPage());
		assertEquals(wizard.getPages().size(), 1);

		wizard.addPage(new TestWizardPage());
		assertEquals(wizard.getPages().size(), 2);
	}

	@Test
	public void getPreviousButton()
	{
		Wizard wizard = new Wizard();
		wizard.addPage(new TestWizardPage());
		WizardButton prevButton = wizard.getPreviousButton();
		assertNotNull(prevButton);
		assertFalse(prevButton.isEnabled());
		assertEquals(prevButton.getTitle(), "Previous");
	}

	@Test
	public void isFirstPage()
	{
		Wizard wizard = new Wizard();
		wizard.addPage(new TestWizardPage());
		wizard.addPage(new TestWizardPage());
		assertTrue(wizard.isFirstPage());
		wizard.next();
		assertFalse(wizard.isFirstPage());
	}

	@Test
	public void isLastPage()
	{
		Wizard wizard = new Wizard();
		wizard.addPage(new TestWizardPage());
		wizard.addPage(new TestWizardPage());
		assertFalse(wizard.isLastPage());
		wizard.next();
		assertTrue(wizard.isLastPage());
	}

	@Test
	public void next()
	{
		Wizard wizard = new Wizard();
		WizardPage p1 = new TestWizardPage();
		wizard.addPage(p1);
		WizardPage p2 = new TestWizardPage();
		wizard.addPage(p2);

		assertEquals(wizard.getCurrentPage(), p1);
		wizard.next();
		assertEquals(wizard.getCurrentPage(), p2);
	}

	@Test
	public void previous()
	{
		Wizard wizard = new Wizard();
		WizardPage p1 = new TestWizardPage();
		wizard.addPage(p1);
		WizardPage p2 = new TestWizardPage();
		wizard.addPage(p2);
		wizard.next();
		assertEquals(wizard.getCurrentPage(), p2);
		wizard.previous();
		assertEquals(wizard.getCurrentPage(), p1);
	}

	private static class TestWizardPage extends AbstractWizardPage
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle()
		{
			return "test";
		}

		@Override
		public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
		{
			return null;
		}

	}
}
