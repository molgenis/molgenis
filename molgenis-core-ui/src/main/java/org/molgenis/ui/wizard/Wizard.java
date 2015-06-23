package org.molgenis.ui.wizard;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Wizard implements Serializable
{
	private static final String PREVIOUS_BUTTON_ID = "wizard-previous-button";
	private static final String FINISH_BUTTON_ID = "wizard-finish-button";
	private static final String NEXT_BUTTON_ID = "wizard-next-button";
	private static final long serialVersionUID = 1L;
	private final List<WizardPage> pages = new LinkedList<WizardPage>();
	private int currentPageIndex = 0;

	public void addPage(WizardPage page)
	{
		pages.add(page);
	}

	public List<WizardPage> getPages()
	{
		return Collections.unmodifiableList(pages);
	}

	public WizardPage getCurrentPage()
	{
		if (pages.isEmpty())
		{
			throw new IllegalStateException("No wizard pages defined");
		}

		return pages.get(currentPageIndex);
	}

	public boolean isLastPage()
	{
		if (pages.isEmpty())
		{
			throw new IllegalStateException("No wizard pages defined");
		}

		return currentPageIndex == pages.size() - 1;
	}

	public boolean isFirstPage()
	{
		if (pages.isEmpty())
		{
			throw new IllegalStateException("No wizard pages defined");
		}

		return currentPageIndex == 0;
	}

	public void next()
	{
		if (isLastPage())
		{
			throw new IllegalStateException("There is no next page");
		}

		currentPageIndex++;
	}

	public void previous()
	{
		if (isFirstPage())
		{
			throw new IllegalStateException("There is no previous page");
		}

		currentPageIndex--;
	}

	public WizardButton getNextButton()
	{
		boolean lastPage = isLastPage();
		String id = lastPage ? FINISH_BUTTON_ID : NEXT_BUTTON_ID;
		String title = lastPage ? "Finish" : "Next";
		String uri = lastPage ? "/restart" : "/next";

		return new WizardButton(id, title, true, uri);
	}

	public WizardButton getPreviousButton()
	{
		return new WizardButton(PREVIOUS_BUTTON_ID, "Previous", !isFirstPage(), "/previous");
	}
}
