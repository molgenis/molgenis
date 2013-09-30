package org.molgenis.ui.wizard;

public class WizardButton
{
	private final String title;
	private final boolean enabled;
	private final String targetUri;

	public WizardButton(String title, boolean enabled, String targetUri)
	{
		this.title = title;
		this.enabled = enabled;
		this.targetUri = targetUri;
	}

	public String getTitle()
	{
		return title;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public String getTargetUri()
	{
		return targetUri;
	}

}
