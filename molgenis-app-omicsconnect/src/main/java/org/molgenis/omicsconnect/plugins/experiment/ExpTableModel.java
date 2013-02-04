package org.molgenis.omicsconnect.plugins.experiment;

import java.util.List;

import org.molgenis.omx.observ.target.Panel;
import org.molgenis.omx.organization.Study;

//mac shift o 

public class ExpTableModel
{
	private List<Study> studies;

	private List<Panel> panels;

	public List<Study> getStudies()
	{
		return studies;
	}

	public void setStudies(List<Study> studies)
	{
		this.studies = studies;
	}

	public List<Panel> getPanels()
	{
		return panels;
	}

	public void setPanels(List<Panel> panels)
	{
		this.panels = panels;
	}

}
