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

public class AnnotatorUpdateIT
{
	/**
	 * Input:
	 * 22	25599848	.	C	T,TA	.	.	CADD=1.0,2.0;CADD_SCALED=3.0,4.0;
	 * 22	25599849	.	G	AT	.	.	CADD=5.0;CADD_SCALED=6.0;
	 * 22	25599863	.	G	C,T	.	.	.
	 * 22	25601151	.	T	C,GA	.	.	CADD=.,7.0;CADD_SCALED=.,8.0;
	 * 22	25601188	.	A	G	.	.	CADD=9.0;CADD_SCALED=10.0;
	 * 
	 * Output, without updating:
	 * 22	25599848	.	C	T,TA	.	.	CADD=6.956883,.;CADD_SCALED=33.0,.;
	 * 22	25599849	.	G	AT	.	.	.
	 * 22	25599863	.	G	C,T	.	.	CADD=.,2.984226;CADD_SCALED=.,22.2;
	 * 22	25601151	.	T	C,GA	.	.	CADD=-0.283077,.;CADD_SCALED=0.725,.;
	 * 22	25601188	.	A	G	.	.	CADD=3.177096;CADD_SCALED=22.7;
	 * 
	 * Output, with updating (new functionality):
	 * 22	25599848	.	C	T,TA	.	.	CADD=6.956883,2.0;CADD_SCALED=33.0,4.0;
	 * 22	25599849	.	G	AT	.	.	CADD=5.0;CADD_SCALED=6.0;
	 * 22	25599863	.	G	C,T	.	.	CADD=.,2.984226;CADD_SCALED=.,22.2;
	 * 22	25601151	.	T	C,GA	.	.	CADD=-0.283077,7.0;CADD_SCALED=0.725,8.0;
	 * 22	25601188	.	A	G	.	.	CADD=3.177096;CADD_SCALED=22.7;
	 * 
	 * @throws IOException
	 */
	@Test
	public void update() throws IOException
	{
		File vcf = new File("src/test/resources/AnnotatorUpdateIT.vcf");

		try (VcfRepository repo = new VcfRepository(vcf, "vcf"))
		{
			try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
					"org.molgenis.data.annotation"))
			{
				Map<String, RepositoryAnnotator> annotators = ctx.getBeansOfType(RepositoryAnnotator.class);

				RepositoryAnnotator caddAnnotator = annotators.get("cadd");
				caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings("src/test/resources/cadd/AnnotatorUpdateIT_CADD_ESP6500SI_7lines.tsv.gz");

				Iterator<Entity> it = caddAnnotator.annotate(repo);
				assertNotNull(it);
				assertTrue(it.hasNext());

				Entity entity = it.next();
				System.out.println(entity.toString());
			
				//TODO

			}
		}
	}
}
