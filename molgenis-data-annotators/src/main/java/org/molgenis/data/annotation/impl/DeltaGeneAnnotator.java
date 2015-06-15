package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.provider.DeltaGeneDataProvider;
import org.molgenis.data.annotation.provider.UrlPinger;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
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

@Component("DeltaGeneService")
class DeltaGeneAnnotator extends AbstractRepositoryAnnotator
{
	public static final String DG_A_LABEL = "Input_A";
	public static final String DG_B_LABEL = "Input_B";
	public static final String UNIQUE_GENES_A_LABEL = "Input_A_Unique_Genes";
	public static final String UNIQUE_GENES_B_LABEL = "Input_B_Unique_Genes";
	public static final String COMMON_GENES_LABEL = "Common_Genes";
	private static final String NAME = "DeltaGene";
	private static final String DESC = "The DeltaGene annotator compares the genes of two inputs, and displays genes shared by both inputs as well as genes unique for each input.\n"
			+ "Inputs can be either a comma-seperated list of genes or a comma-seperated list of HPO terms.\n\n"
			+ "HPO terms are searched recusively (HPO terms with sub-terms will have their childrens' genes included in the comparison).";
	private DeltaGeneDataProvider DGData;
	private MolgenisSettings molgenisSettings;
	private UrlPinger pinger;
	// time out for waiting for the DeltaGeneDataProvider to complete is 10 seconds
	Long TIME_OUT = 10000L;
	
	@Autowired
	DeltaGeneAnnotator(DeltaGeneDataProvider DGData, MolgenisSettings molgenisSettings)
	{
		this.DGData = DGData;
		this.molgenisSettings = molgenisSettings;
	}
	
	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("comp_test", MolgenisFieldTypes.FieldTypeEnum.COMPOUND)
		.setLabel("comp_test").setDescription("Compound result data."));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData("comp_A", MolgenisFieldTypes.FieldTypeEnum.INT)
		.setLabel("comp_A"));
			metadata.addAttributeMetaData(new DefaultAttributeMetaData("comp_B", MolgenisFieldTypes.FieldTypeEnum.INT)
		.setLabel("comp_B"));
			metadata.addAttributeMetaData(new DefaultAttributeMetaData("comp_C", MolgenisFieldTypes.FieldTypeEnum.INT)
		.setLabel("comp_C"));
		return metadata;
	}
	
	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(DG_A_LABEL, MolgenisFieldTypes.FieldTypeEnum.TEXT)
		.setLabel(DG_A_LABEL).setDescription("A comma-seperated list of genes or HPO terms to be compared."));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(DG_B_LABEL, MolgenisFieldTypes.FieldTypeEnum.TEXT)
		.setLabel(DG_B_LABEL).setDescription("A comma-seperated list of genes or HPO terms to be compared."));
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

	@Override
	protected boolean annotationDataExists() {
		return true;
	}
	
	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException 
	{
		List <Entity> results = new ArrayList<>();
		HashMap<String,Object> resultMap = new HashMap<String,Object>();		
		String A = entity.getString(DG_A_LABEL);
		String B = entity.getString(DG_B_LABEL);
		Stack<String> AStack;
		Stack<String> BStack;
		StringBuilder commonString = new StringBuilder();
		StringBuilder uniqueAString = new StringBuilder();
		StringBuilder uniqueBString = new StringBuilder();
		Enumeration<String> e;
		String gene;

		
		
		for (int i = 0; i < 10; i++) {
			
		}
		
		/*AStack = getInputStack(A);
		BStack = getInputStack(B);
		
		e = AStack.elements();
		while(e.hasMoreElements()) {
			if (BStack.contains(gene = e.nextElement())) {
				commonString.append(gene+",");
			}else{
				uniqueAString.append(gene+",");
			}
		}
		
		e = BStack.elements();
		while(e.hasMoreElements()) {
			if (!AStack.contains(gene = e.nextElement()))
				uniqueBString.append(gene+",");
		}
		resultMap.put(COMMON_GENES_LABEL, commonString.toString());
		
		resultMap.put(UNIQUE_GENES_A_LABEL, uniqueAString.toString());
		
		resultMap.put(UNIQUE_GENES_B_LABEL, uniqueBString.toString());*/
		
		results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
		return results;
	}
	
	private Stack<String> getInputStack(String input) 
	{
		Stack<String> out = new Stack<String>();
		switch (getInputType(input)) {
		case 0:
			return null;
		case 1:
			DGData.getHPOGeneStack(input, out);
			return out;
		case 2:
			for (String hpo : input.split("[,\\n\\t]"))
				DGData.getHPOGeneStack(hpo, out);
			return out;
		case 3:
			for (String gene : input.split(","))
				out.add(gene);
			return out;
		default:
			return null;
		}
	}
	
	private int getInputType(String in)
	{
		if (in.contains("HP:")) 
			if (in.substring(in.indexOf("HP:")+1).contains("HP:"))
				return 2;
			else
				return 1;
		if (in.matches("[^A-Z0-9,\\-\\n\\t]")) 
			return 0;
		if (in.matches("(?:[A-Z0-9]+,?)+")) 
			return 3;
		return 0;
	}
}