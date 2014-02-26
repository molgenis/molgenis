package org.molgenis.data.annotation.impl;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;

public class AlleleSpecificExpressionServiceAnnotator extends LocusAnnotator
{
	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";
	
	@Autowired
	AnnotationService annotatorService;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}
	
	@Override
	public String getName()
	{
		return "ASE";
	}
	
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
}
