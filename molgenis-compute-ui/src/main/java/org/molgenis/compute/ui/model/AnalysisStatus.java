package org.molgenis.compute.ui.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum AnalysisStatus implements State<AnalysisStatus>
{
	CREATED
	{
		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public boolean isInitialState()
		{
			return true;
		}

		@Override
		public EnumSet<AnalysisStatus> getStateTransitions()
		{
			return EnumSet.of(RUNNING);
		}
	},
	RUNNING
	{
		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public EnumSet<AnalysisStatus> getStateTransitions()
		{
			return EnumSet.of(PAUSED, COMPLETED, FAILED, CANCELLED);
		}
	},
	PAUSED
	{
		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public EnumSet<AnalysisStatus> getStateTransitions()
		{
			return EnumSet.of(RUNNING);
		}
	},
	COMPLETED
	{
		@Override
		public boolean isFinalState()
		{
			return true;
		}

		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public EnumSet<AnalysisStatus> getStateTransitions()
		{
			return EnumSet.noneOf(AnalysisStatus.class);
		}
	},
	FAILED
	{
		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public EnumSet<AnalysisStatus> getStateTransitions()
		{
			return EnumSet.of(RUNNING);
		}
	},
	CANCELLED
	{
		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public EnumSet<AnalysisStatus> getStateTransitions()
		{
			return EnumSet.of(RUNNING);
		}
	};

	public static List<String> names()
	{
		AnalysisStatus[] values = values();
		List<String> names = new ArrayList<String>(values.length);
		for (AnalysisStatus analysisStatus : values)
			names.add(analysisStatus.toString());
		return names;
	}
}
