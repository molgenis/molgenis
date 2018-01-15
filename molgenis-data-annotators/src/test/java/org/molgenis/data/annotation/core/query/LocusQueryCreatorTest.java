package org.molgenis.data.annotation.core.query;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { LocusQueryCreatorTest.Config.class })
public class LocusQueryCreatorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Test
	public void createQueryEntity()
	{
		Attribute idAttr = attributeFactory.create().setName("idAttribute").setAuto(true).setIdAttribute(true);
		EntityType emd = entityTypeFactory.create("testEntity");
		emd.addAttributes(Arrays.asList(idAttr, vcfAttributes.getChromAttribute(), vcfAttributes.getPosAttribute()));
		Entity entity = new DynamicEntity(emd);
		entity.set(VcfAttributes.CHROM, "3");
		entity.set(VcfAttributes.POS, 3276424);

		Query<Entity> q = QueryImpl.EQ(VcfAttributes.CHROM, "3").and().eq(VcfAttributes.POS, 3276424);
		assertEquals(q, new LocusQueryCreator(vcfAttributes).createQuery(entity));
	}

	@Test
	public void getRequiredAttributes()
	{
		Iterator<Attribute> requiredAttrs = new LocusQueryCreator(vcfAttributes).getRequiredAttributes().iterator();
		EntityUtils.equals(requiredAttrs.next(), vcfAttributes.getChromAttribute());
		EntityUtils.equals(requiredAttrs.next(), vcfAttributes.getPosAttribute());
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
	}
}
