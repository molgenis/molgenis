package org.molgenis.data.annotation.impl;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.LocusAnnotator;

public class AlleleSpecificExpressionServiceAnnotator extends LocusAnnotator
{
	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";
	
	@Override
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		return "ASE";
	}

}
