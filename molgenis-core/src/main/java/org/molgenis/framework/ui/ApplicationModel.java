package org.molgenis.framework.ui;

public class ApplicationModel extends SimpleScreenModel
{
	private static final long serialVersionUID = 1L;

	/** The version used to generate this MOLGENIS */
	private String version;
	/** Show, if whole app or only target should be shown */
	private String show = "root";
	/** Target, for dialogs */
	private ScreenModel target = null;

	public ApplicationModel(ScreenController<?> controller)
	{
		super(controller);
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getShow()
	{
		return show;
	}

	public void setShow(String show)
	{
		this.show = show;
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return this.getController().getCustomHtmlHeaders();
	}

	@Override
	public String getCustomHtmlBodyOnLoad()
	{
		return this.getController().getCustomHtmlBodyOnLoad();
	}

	public void setTarget(ScreenController<?> target)
	{
		/*
		 * BUG: This can be resolved to a NULL pointer (When the underlying
		 * caller doesn't have required fields filled in ?) To replicate: press
		 * the add new btn next to an entity which has required fields not
		 * filled out
		 * 
		 * NullPointerException at
		 * org.molgenis.framework.ui.ApplicationModel.setTarget
		 * (ApplicationModel.java:64) at
		 * org.molgenis.framework.server.AbstractMolgenisServlet
		 * .handleGUIrequest(AbstractMolgenisServlet.java:607) at
		 * org.molgenis.framework
		 * .server.AbstractMolgenisServlet.service(AbstractMolgenisServlet
		 * .java:242) at
		 * org.molgenis.framework.server.AbstractMolgenisServlet.doPost
		 * (AbstractMolgenisServlet.java:178)
		 */
		this.target = target.getModel();
	}

	public ScreenModel getTarget()
	{
		return this.target;
	}
}
