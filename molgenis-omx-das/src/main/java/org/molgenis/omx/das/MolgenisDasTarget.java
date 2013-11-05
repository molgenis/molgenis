package org.molgenis.omx.das;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasTarget;

public class MolgenisDasTarget extends DasTarget
{
	//class used to override  the equals method so it can be used in unittests
	private static final long serialVersionUID = 1L;

	public MolgenisDasTarget(String targetId, int startCoordinate, int stopCoordinate, String targetName)
			throws DataSourceException
	{
		super(targetId, startCoordinate, stopCoordinate, targetName);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MolgenisDasTarget other = (MolgenisDasTarget) obj;
		if (getStartCoordinate() != other.getStartCoordinate()) return false;
		if (getStopCoordinate() != other.getStopCoordinate()) return false;
		if (getTargetId() == null)
		{
			if (other.getTargetId() != null) return false;
		}
		else if (!getTargetId().equals(other.getTargetId())) return false;
		if (getTargetId() == null)
		{
			if (other.getTargetId() != null) return false;
		}
		else if (!getTargetName().equals(other.getTargetName())) return false;
		return true;
	}

	public String getTargetId()
	{
		return super.getTargetId();
	}


	public int getStartCoordinate()
	{
		return super.getStartCoordinate();
	}


	public int getStopCoordinate()
	{
		return super.getStopCoordinate();
	}


	public String getTargetName()
	{
		return super.getTargetName();
	}

}
