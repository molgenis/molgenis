package org.molgenis.core.ui.wizard;

public class WizardButton
{
	private final String id;
	private final String title;
	private final boolean enabled;
	private final String targetUri;

	public WizardButton(String id, String title, boolean enabled, String targetUri)
	{
		this.id = id;
		this.title = title;
		this.enabled = enabled;
		this.targetUri = targetUri;
	}

	public String getId()
	{
		return id;
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
