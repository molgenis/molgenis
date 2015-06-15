package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.rmi.dgc.DGC;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.HPOFilterDataProvider;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.provider.HpoDataProvider;
import org.molgenis.data.annotation.provider.UrlPinger;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.utils.HgncLocationsUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * exclusive and common genes per phenotype / hpo term
 * 
 */

@Component("HPOFilterService")
class HPOFilterAnnotator
{
	public static final String CHROM_LABEL = "#CHROM";
	public static final String POS_LABEL = "POS";
	public static final String PASS_LABEL = "Pass";
	private static final String NAME = "HPOFilter";
	private static final String DESC = "The HPOFilter annotator filteres variants by HPO terms associated with variant genes.\n\n";
	private HPOFilterDataProvider hpoFilterData;
	private HgncLocationsProvider hgncLocationsProvider;
	private MolgenisSettings molgenisSettings;
	private UrlPinger pinger;
	// time out for waiting for the HPOFilterDataProvider to complete is 10 seconds
	Long TIME_OUT = 10000L;
	
	String hpo = "HP:0000006";
	
	@Autowired
	HPOFilterAnnotator(HPOFilterDataProvider hpoFilterData, MolgenisSettings molgenisSettings, 
			HgncLocationsProvider hgncLocationsProvider)
	{
		this.hpoFilterData = hpoFilterData;
		this.molgenisSettings = molgenisSettings;
		this.hgncLocationsProvider = hgncLocationsProvider;
	}
	
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		/*metadata.addAttributeMetaData(new DefaultAttributeMetaData(PASS_LABEL, MolgenisFieldTypes.FieldTypeEnum.BOOL)
		.setLabel(PASS_LABEL).setDescription("True if variant passed filter, false if not."));*/
		return metadata;
	}

	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROM_LABEL, MolgenisFieldTypes.FieldTypeEnum.STRING)
		.setLabel(CHROM_LABEL).setDescription("Chromosome on which the variant is located"));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POS_LABEL, MolgenisFieldTypes.FieldTypeEnum.LONG)
		.setLabel(POS_LABEL).setDescription("Nucleotide on the chromosome on which the variant is located"));
		return metadata;
	}

	public String getDescription()
	{
		return DESC;
	}

	public String getSimpleName() 
	{
		return NAME;
	}

	protected boolean annotationDataExists() {
		return true;
	}
	
	//public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	public void annotateEntity(Entity entity) throws IOException, InterruptedException 
	{
		//HashMap<String,Object> resultMap = new HashMap<String,Object>();	
		List<String> genes;
		String c = entity.getString(CHROM_LABEL);
		Long pos = entity.getLong(POS_LABEL);
		Locus l = new Locus(c, pos);
		
		genes = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(), l);
		System.out.println(genes.size()+"genes");
		//resultMap.put(PASS_LABEL, false);
		
		for (String gene : genes) {
			if (HPOContainsGene(hpo, gene, true))
				hpoFilterData.addEntityToResult(entity);
				//resultMap.put(PASS_LABEL, true);
		}
		
	}
	
	private boolean HPOContainsGene(String hpo, String gene, boolean recursive) {
		if (hpoFilterData.getAssocData().containsKey(hpo))
			if (hpoFilterData.getAssocData().get(hpo).contains(gene))
				return true;
			if (recursive)
				for (String child : hpoFilterData.getHPOData().get(hpo))
					if (HPOContainsGene(child, gene, true))
						return true;
		return false;
	}
}