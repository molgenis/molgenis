package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.AnalysisTargetMetaData;
import org.molgenis.data.support.MapEntity;

public class AnalysisTarget extends MapEntity
{
	private static final long serialVersionUID = 406768955023996782L;

	public AnalysisTarget()
	{
		super(AnalysisTargetMetaData.IDENTIFIER);
	}

	public AnalysisTarget(String identifier, String targetId, Analysis analysis)
	{
		this();
		setIdentifier(identifier);
		setTargetId(targetId);
		setAnalysis(analysis);
	}

	private void setIdentifier(String identifier)
	{
		set(AnalysisTargetMetaData.IDENTIFIER, identifier);
	}

	public String getIdentifier()
	{
		return getString(AnalysisTargetMetaData.IDENTIFIER);
	}

	public Analysis getAnalysis()
	{
		return getEntity(AnalysisTargetMetaData.ANALYSIS, Analysis.class);
	}

	public void setAnalysis(Analysis analysis)
	{
		set(AnalysisTargetMetaData.ANALYSIS, analysis);
	}

	public String getTargetId()
	{
		return getString(AnalysisTargetMetaData.TARGET_ID);
	}

	public void setTargetId(String targetId)
	{
		set(AnalysisTargetMetaData.TARGET_ID, targetId);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdentifier().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getIdentifier().equals(((AnalysisTarget) obj).getIdentifier());
	}
}
