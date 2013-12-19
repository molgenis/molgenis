package org.molgenis.charts;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.molgenis.charts.data.ChartDataServiceImpl;

public enum MolgenisAxisType
{
	LINEAR, 
	LOGARITHMIC, 
	DATETIME,
	CATEGORY;
	
	private static final Logger logger = Logger.getLogger(MolgenisAxisType.class);
	
	private MolgenisAxisType() {}
	
	/**
	 * Get a Molgenis axis type based on an attributeJavaType
	 * 
	 * @param attributeJavaType Class<?>
	 * @return MolgenisAxisType
	 */
	public static MolgenisAxisType getType(Class<?> javaType)
	{	
		logger.info("javaType: " + javaType);
		if(Double.class == javaType)
		{
			return MolgenisAxisType.LINEAR;
		} 
		else if (Date.class == javaType) 
		{
			return MolgenisAxisType.DATETIME;
		}
		else if (String.class == javaType) 
		{
			return MolgenisAxisType.CATEGORY;
		} 
		else if (Timestamp.class == javaType) 
		{
			return MolgenisAxisType.DATETIME;
		}
		else {
			return MolgenisAxisType.CATEGORY;
		}
	}
}
