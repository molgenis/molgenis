package org.molgenis.gavin.job;

import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final VcfUtils vcfUtils;

	@Autowired
	public AnnotatorRunner(VcfAttributes vcfAttributes, EntityTypeFactory entityTypeFactory,
			AttributeFactory attributeFactory, VcfUtils vcfUtils)
	{
		this.vcfAttributes = vcfAttributes;
		this.entityTypeFactory = entityTypeFactory;
		this.attributeFactory = attributeFactory;
		this.vcfUtils = vcfUtils;
	}

	public void runAnnotator(RepositoryAnnotator annotator, File inputFile, File outputFile, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{
		AnnotatorUtils.annotate(annotator, vcfAttributes, entityTypeFactory, attributeFactory, vcfUtils, inputFile,
				outputFile, emptyList(), update);
	}
}
