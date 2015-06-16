package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.rmi.dgc.DGC;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import net.sf.samtools.util.RuntimeEOFException;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.LocusAnnotator;
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

import ch.qos.logback.core.boolex.Matcher;

/**
 * exclusive and common genes per phenotype / hpo term
 * 
 */

@Component("HPOFilterService")
class HPOFilterAnnotator extends LocusAnnotator
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
	
	// TODO These parameters need to be set by the user!
	String hpo = "12345";
	boolean recursive = true;
	
	@Autowired
	HPOFilterAnnotator(HPOFilterDataProvider hpoFilterData, MolgenisSettings molgenisSettings, 
			HgncLocationsProvider hgncLocationsProvider)
	{
		this.hpoFilterData = hpoFilterData;
		this.molgenisSettings = molgenisSettings;
		this.hgncLocationsProvider = hgncLocationsProvider;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PASS_LABEL, MolgenisFieldTypes.FieldTypeEnum.BOOL)
		.setLabel(PASS_LABEL).setDescription("True if variant passed filter, false if not."));
		return metadata;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROM_LABEL, MolgenisFieldTypes.FieldTypeEnum.STRING)
		.setLabel(CHROM_LABEL).setDescription("Chromosome on which the variant is located"));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POS_LABEL, MolgenisFieldTypes.FieldTypeEnum.LONG)
		.setLabel(POS_LABEL).setDescription("Nucleotide on the chromosome on which the variant is located"));
		return metadata;
	}

	@Override
	public String getDescription()
	{
		return DESC;
	}

	@Override
	public String getSimpleName() 
	{
		return NAME;
	}

	protected boolean annotationDataExists() {
		return true;
	}
	
	
	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException 
	{
		HashMap<String,Object> resultMap = new HashMap<String,Object>();	
		List<String> genes;
		String c = entity.getString(CHROM_LABEL);
		Long pos = entity.getLong(POS_LABEL);
		Locus l = new Locus(c, pos);
		List<Entity> results = new ArrayList<>();
		
		if (validateHpo(hpo) == 2)
			hpo = convertNumberToHPO(hpo);
		
		// Throw an error if the HPO term specified by the user does not exist
		if (!hpoFilterData.getHPOData().containsKey(hpo))
			throw new RuntimeException(hpo+" does not exist!");
		
		genes = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(), l);
		resultMap.put(PASS_LABEL, false);
		
		for (String gene : genes) {
			if (HPOContainsGene(hpo, gene, recursive))
				resultMap.put(PASS_LABEL, true);
		}
		results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
		
		return results;
	}
	
	/**
	 * Checks if a specified hpo contains a specified gene. If 
	 * recursive = true, it will also check the specified HPO's 
	 * children.
	 * @param hpo the filter HPO term
	 * @param gene the variants' gene
	 * @param recursive true if searching children, false if not.
	 * @return true if HPO contains gene, false if hpo does not contain gene
	 */
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
	
	/**
	 * validates the HPO term as input by the user
	 * @param input HPO term to validate
	 * @return 1 if exact term, 2 if just numbers, 0 if invalid
	 */
	private int validateHpo (String input) {
		if (input.matches("(HP:[0-9]{7})")) 
			return 1;
		if (input.matches("([0-9]{1,7})"))
			return 2;
		return 0;
	}
	
	/**
	 * converts any number of 1-7 digits as a valid HPO term. Returns null if
	 * 1 > length > 8.<br>
	 * <br>
	 * Example: 123 -> HP:0000123
	 * @param num
	 * @return a valid hpo number
	 */
	public String convertNumberToHPO(String num) {
		for (String number : num.split("([^\\d]+)")) {
			StringBuilder sb = new StringBuilder("HP:");
			if (number.length() < 8) {
				for (int i = number.length(); i < 7; i++) {
					sb.append('0');
				}
				sb.append(number);
				return sb.toString();
			}else{
				return null;
			}
		}
		return null;
	}
}