package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class AnnotatorUpdateIT
{
	/**
	 *
	 * TODO: also test GoNL because it has custom QueryAnnotatorImpl !
	 *
	 * Input: 22 25599848 . C T,TA . . CADD=1.0,2.0;CADD_SCALED=3.0,4.0; 22 25599849 . G AT . .
	 * CADD=5.0;CADD_SCALED=6.0; 22 25599863 . G C,T . . . 22 25601151 . T C,GA . . CADD=.,7.0;CADD_SCALED=.,8.0; 22
	 * 25601188 . A G . . CADD=9.0;CADD_SCALED=10.0;
	 *
	 * Output, without updating: 22 25599848 . C T,TA . . CADD=6.956883,.;CADD_SCALED=33.0,.; 22 25599849 . G AT . . .
	 * 22 25599863 . G C,T . . CADD=.,2.984226;CADD_SCALED=.,22.2; 22 25601151 . T C,GA . .
	 * CADD=-0.283077,.;CADD_SCALED=0.725,.; 22 25601188 . A G . . CADD=3.177096;CADD_SCALED=22.7;
	 *
	 * Output, with updating (new functionality): 22 25599848 . C T,TA . . CADD=6.956883,2.0;CADD_SCALED=33.0,4.0; 22
	 * 25599849 . G AT . . CADD=5.0;CADD_SCALED=6.0; 22 25599863 . G C,T . . CADD=.,2.984226;CADD_SCALED=.,22.2; 22
	 * 25601151 . T C,GA . . CADD=-0.283077,7.0;CADD_SCALED=0.725,8.0; 22 25601188 . A G . .
	 * CADD=3.177096;CADD_SCALED=22.7;
	 *
	 * @throws IOException
	 */
	@Test
	public void update() throws IOException
	{
		File vcf = new File("src/test/resources/annotatorUpdateIT.vcf");

		try (VcfRepository repo = new VcfRepository(vcf, "vcf"))
		{
			try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
					"org.molgenis.data.annotation"))
			{
				Map<String, RepositoryAnnotator> annotators = ctx.getBeansOfType(RepositoryAnnotator.class);

				RepositoryAnnotator caddAnnotator = annotators.get("cadd");
				caddAnnotator.getCmdLineAnnotatorSettingsConfigurer()
						.addSettings("src/test/resources/cadd/AnnotatorUpdateIT_CADD_ESP6500SI_7lines.tsv.gz");

				// first without updating
				Iterator<Entity> noUpdateIt = caddAnnotator.annotate(repo, false);
				assertNotNull(noUpdateIt);
				assertTrue(noUpdateIt.hasNext());

				Entity entity = noUpdateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS).toString(), "6.956883,.");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED).toString(), "33.0,.");

				entity = noUpdateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS), null);
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED), null);

				entity = noUpdateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS).toString(), ".,2.984226");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED).toString(), ".,22.2");

				entity = noUpdateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS).toString(), "-0.283077,.");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED).toString(), "0.725,.");

				entity = noUpdateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS).toString(), "3.177096");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED).toString(), "22.7");

				// and now with updating
				RepositoryAnnotator caddAnnotatorUpdating = annotators.get("cadd");
				caddAnnotatorUpdating.getCmdLineAnnotatorSettingsConfigurer()
						.addSettings("src/test/resources/cadd/AnnotatorUpdateIT_CADD_ESP6500SI_7lines.tsv.gz");

				Iterator<Entity> updateIt = caddAnnotatorUpdating.annotate(repo, true);
				assertNotNull(updateIt);
				assertTrue(updateIt.hasNext());

				entity = updateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS).toString(), "6.956883,2.0");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED).toString(), "33.0,4.0");

				entity = updateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS), "5.0");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED), "6.0");

				entity = updateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS), ".,2.984226");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED), ".,22.2");

				entity = updateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS), "-0.283077,7.0");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED), "0.725,8.0");

				entity = updateIt.next();
				assertEquals(entity.get(CaddAnnotator.CADD_ABS), "3.177096");
				assertEquals(entity.get(CaddAnnotator.CADD_SCALED), "22.7");

			}
		}
	}
}