package org.molgenis.data.annotation.entity.impl;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.Test;

public class AnnotatorChainingIT
{
	@Test
	public void chain() throws IOException
	{
		File vcf = new File("src/test/resources/gonl/test_gonl_and_1000g.vcf");

		try (VcfRepository repo = new VcfRepository(vcf, "vcf"))
		{
			try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
					"org.molgenis.data.annotation"))
			{
				Map<String, RepositoryAnnotator> annotators = ctx.getBeansOfType(RepositoryAnnotator.class);

				RepositoryAnnotator gonlAnnotator = annotators.get("gonl");
				gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings("src/test/resources/gonl");

				RepositoryAnnotator tgAnnotator = annotators.get("thousandGenomes");
				tgAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings("src/test/resources/1000g");

				Iterator<Entity> it = gonlAnnotator.annotate(repo);
				assertNotNull(it);
				assertTrue(it.hasNext());

				it = tgAnnotator.annotate(it);
				assertNotNull(it);
				assertTrue(it.hasNext());

				Entity entity = it.next();
				assertNotNull(entity.get(GoNLAnnotator.GONL_GENOME_AF));
				assertNotNull(entity.get(GoNLAnnotator.GONL_GENOME_GTC));
				assertNotNull(entity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));

				EntityMetaData meta = entity.getEntityMetaData();
				assertNotNull(meta);
				assertNotNull(meta.getAttribute(GoNLAnnotator.GONL_GENOME_AF));
				assertNotNull(meta.getAttribute(GoNLAnnotator.GONL_GENOME_GTC));
				assertNotNull(meta.getAttribute(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));
			}
		}
	}
}
