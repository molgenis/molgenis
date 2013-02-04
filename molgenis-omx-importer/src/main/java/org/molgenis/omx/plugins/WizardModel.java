package org.molgenis.omx.plugins;

public abstract class WizardModel
{
	private final int nrPages;
	private int page = 0;
	private boolean disableBack;
	private boolean disableNext;

	public WizardModel(int nrPages)
	{
		if (nrPages <= 0) throw new IllegalArgumentException();
		this.nrPages = nrPages;
	}

	public int getNrPages()
	{
		return nrPages;
	}

	public int getPage()
	{
		return page;
	}

	public void incrementPage()
	{
		if (this.page < nrPages - 1) this.page++;
	}

	public void decrementPage()
	{
		if (this.page > 0) this.page--;
	}

	public void setPage(int page)
	{
		if (page >= 0 && page < nrPages) this.page = page;
	}

	public boolean isLastPage()
	{
		return this.page == nrPages - 1;
	}

	public boolean isFirstPage()
	{
		return this.page == 0;
	}

	public boolean isDisableBack()
	{
		return disableBack;
	}

	public void setDisableBack(boolean disableBack)
	{
		this.disableBack = disableBack;
	}

	public boolean isDisableNext()
	{
		return disableNext;
	}

	public void setDisableNext(boolean disableNext)
	{
		this.disableNext = disableNext;
	}
}
