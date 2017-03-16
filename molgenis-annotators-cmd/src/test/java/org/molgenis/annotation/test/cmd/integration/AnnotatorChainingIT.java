package org.molgenis.annotation.test.cmd.integration;

import org.molgenis.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.annotation.cmd.utils.VcfValidator;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.impl.GoNLAnnotator;
import org.molgenis.data.annotation.core.entity.impl.ThousandGenomesAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { CommandLineAnnotatorConfig.class })
public class AnnotatorChainingIT extends AbstractTestNGSpringContextTests
{
	@Autowired
	CommandLineAnnotatorConfig commandLineAnnotatorConfig;

	@Autowired
	VcfValidator vcfValidator;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	VcfUtils vcfUtils;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	AttributeFactory attributeFactory;

	@Test
	public void chain() throws IOException
	{
		File vcf = ResourceUtils.getFile(getClass(), "/gonl/test_gonl_and_1000g.vcf");
		try (VcfRepository repo = new VcfRepository(vcf, "vcf", vcfAttributes, entityTypeFactory,
				attributeFactory))
		{
			try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
					"org.molgenis.data.annotation.core", "org.molgenis.annotation.cmd"))
			{
				ctx.register(CommandLineAnnotatorConfig.class);
				Map<String, AnnotatorConfig> annotatorMap = ctx.getBeansOfType(AnnotatorConfig.class);
				annotatorMap.values().forEach(AnnotatorConfig::init);
				Map<String, RepositoryAnnotator> annotators = ctx.getBeansOfType(RepositoryAnnotator.class);

				RepositoryAnnotator gonlAnnotator = annotators.get("gonl");
				gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer()
						.addSettings(ResourceUtils.getFile(getClass(), "/gonl").getPath());

				RepositoryAnnotator tgAnnotator = annotators.get("thousandGenomes");
				tgAnnotator.getCmdLineAnnotatorSettingsConfigurer()
						.addSettings(ResourceUtils.getFile(getClass(), "/1000g").getPath());
				AnnotatorUtils.addAnnotatorMetaDataToRepositories(repo.getEntityType(), attributeFactory,
						gonlAnnotator);

				Iterator<Entity> it = gonlAnnotator.annotate(repo);
				assertNotNull(it);
				assertTrue(it.hasNext());

				AnnotatorUtils.addAnnotatorMetaDataToRepositories(repo.getEntityType(), attributeFactory,
						tgAnnotator);
				it = tgAnnotator.annotate(it);
				assertNotNull(it);
				assertTrue(it.hasNext());

				Entity entity = it.next();
				assertNotNull(entity.get(GoNLAnnotator.GONL_GENOME_AF));
				assertNotNull(entity.get(GoNLAnnotator.GONL_GENOME_GTC));
				assertNotNull(entity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));

				EntityType meta = entity.getEntityType();
				assertNotNull(meta);
				assertNotNull(meta.getAttribute(GoNLAnnotator.GONL_GENOME_AF));
				assertNotNull(meta.getAttribute(GoNLAnnotator.GONL_GENOME_GTC));
				assertNotNull(meta.getAttribute(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));
			}
		}
	}
}
