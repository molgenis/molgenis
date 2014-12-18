package org.molgenis.compute.ui.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum JobStatus implements State<JobStatus>
{
	CREATED
	{
		@Override
		public boolean isInitialState()
		{
			return true;
		}

		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public EnumSet<JobStatus> getStateTransitions()
		{
			return EnumSet.of(SCHEDULED, RUNNING);
		}
	},
	SCHEDULED
	{
		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public EnumSet<JobStatus> getStateTransitions()
		{
			return EnumSet.of(RUNNING);
		}
	},
	RUNNING
	{
		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public EnumSet<JobStatus> getStateTransitions()
		{
			return EnumSet.of(COMPLETED, FAILED, CANCELLED);
		}
	},
	COMPLETED
	{
		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public boolean isFinalState()
		{
			return true;
		}

		@Override
		public EnumSet<JobStatus> getStateTransitions()
		{
			return EnumSet.noneOf(JobStatus.class);
		}
	},
	FAILED
	{
		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public EnumSet<JobStatus> getStateTransitions()
		{
			return EnumSet.of(SCHEDULED, RUNNING);
		}
	},
	CANCELLED
	{
		@Override
		public boolean isInitialState()
		{
			return false;
		}

		@Override
		public boolean isFinalState()
		{
			return false;
		}

		@Override
		public EnumSet<JobStatus> getStateTransitions()
		{
			return EnumSet.of(SCHEDULED, RUNNING);
		}
	};

	// public abstract boolean isInitialState();
	//
	// public abstract boolean isFinalState();
	//
	// public abstract EnumSet<JobStatus> getStateTransitions();

	public static List<String> names()
	{
		JobStatus[] values = values();
		List<String> names = new ArrayList<String>(values.length);
		for (JobStatus analysisStatus : values)
			names.add(analysisStatus.toString());
		return names;
	}
}
