package org.molgenis.ui.wizard;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Wizard implements Serializable
{
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
		String title = lastPage ? "Finish" : "Next &rarr;";
		String uri = lastPage ? "/restart" : "/next";

		return new WizardButton(title, true, uri);
	}

	public WizardButton getPreviousButton()
	{
		return new WizardButton("&larr; Previous", !isFirstPage(), "/previous");
	}
}
