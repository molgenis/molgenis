package org.molgenis.gavin.job;

import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.CmdLineAnnotatorUtils;
import org.molgenis.data.annotation.core.utils.EffectStructureConverter;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.emptyList;

@Component
public class AnnotatorRunner
{
	private final VcfAttributes vcfAttributes;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attributeFactory;
	private final EffectStructureConverter effectStructureConverter;

	public AnnotatorRunner(VcfAttributes vcfAttributes, EntityTypeFactory entityTypeFactory,
			AttributeFactory attributeFactory, EffectStructureConverter effectStructureConverter)
	{
		this.vcfAttributes = vcfAttributes;
		this.entityTypeFactory = entityTypeFactory;
		this.attributeFactory = attributeFactory;
		this.effectStructureConverter = effectStructureConverter;
	}

	public void runAnnotator(RepositoryAnnotator annotator, File inputFile, File outputFile, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{
		CmdLineAnnotatorUtils.annotate(annotator, vcfAttributes, entityTypeFactory, attributeFactory,
				effectStructureConverter, inputFile, outputFile, emptyList(), update);
	}
}
