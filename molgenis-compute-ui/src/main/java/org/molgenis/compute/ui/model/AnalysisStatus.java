package org.molgenis.compute.ui.model;

import java.util.ArrayList;
import java.util.List;

public enum AnalysisStatus
{
	CREATED
	{
		@Override
		public boolean isEndPoint()
		{
			return false;
		}
	},
	RUNNING
	{
		@Override
		public boolean isEndPoint()
		{
			return false;
		}
	},
	PAUSED
	{
		@Override
		public boolean isEndPoint()
		{
			return false;
		}
	},
	COMPLETED
	{
		@Override
		public boolean isEndPoint()
		{
			return true;
		}
	},
	FAILED
	{
		@Override
		public boolean isEndPoint()
		{
			return true;
		}
	},
	CANCELLED
	{
		@Override
		public boolean isEndPoint()
		{
			return true;
		}
	};

	public abstract boolean isEndPoint();

	public static List<String> names()
	{
		AnalysisStatus[] values = values();
		List<String> names = new ArrayList<String>(values.length);
		for (AnalysisStatus analysisStatus : values)
			names.add(analysisStatus.toString());
		return names;
	}
}
