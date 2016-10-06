package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.mapper.service.impl.UnitResolverImpl.UNIT_ONTOLOGY_IRI;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = UnitResolverImplTest.Config.class)
public class UnitResolverImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private UnitResolverImpl unitResolverImpl;

	@Test
	public void testConvertNumberToOntologyTermStyle()
	{
		assertEquals(unitResolverImpl.convertNumberToOntologyTermStyle("kg/m^2"), "kg\\/m\\^\\[2\\]");
		assertEquals(unitResolverImpl.convertNumberToOntologyTermStyle("kg/m^²"), "kg\\/m\\^\\[2\\]");
		assertEquals(unitResolverImpl.convertNumberToOntologyTermStyle("kg/m²"), "kg\\/m\\^\\[2\\]");
	}

	@Test
	public void testTokenize()
	{
		Set<String> tokenize = unitResolverImpl.tokenize("area density (kg/m^²)");
		Assert.assertTrue(newHashSet("area", "density", "kg/m^²").containsAll(tokenize));

		Set<String> tokenize1 = unitResolverImpl.tokenize("area density (kg/m^2)");
		Assert.assertTrue(newHashSet("area", "density", "kg/m^²").containsAll(tokenize1));

		Set<String> tokenize2 = unitResolverImpl.tokenize("area density (kg/m2)");
		Assert.assertTrue(newHashSet("area", "density", "kg/m²").containsAll(tokenize2));

		Set<String> tokenize3 = unitResolverImpl.tokenize("area 2 density2 (kg/m2)");
		Assert.assertTrue(newHashSet("area", "density²", "kg/m²").containsAll(tokenize3));

		Set<String> tokenize4 = unitResolverImpl.tokenize("area 2 density 2 (kg/m2)");
		assertEquals(tokenize4.size(), 3);
		Assert.assertFalse(tokenize4.containsAll(newHashSet("area", "density", "²", "kg/m²")));
	}

	@Test
	public void testIsUnitEmpty()
	{
		Unit<?> unit = Unit.valueOf("");
		Unit<?> unit1 = Unit.valueOf("¹");
		Unit<?> unitKg = Unit.valueOf("kg");
		Assert.assertTrue(unitResolverImpl.isUnitEmpty(null));
		Assert.assertTrue(unitResolverImpl.isUnitEmpty(unit));
		Assert.assertTrue(unitResolverImpl.isUnitEmpty(unit1));
		Assert.assertFalse(unitResolverImpl.isUnitEmpty(unitKg));
	}

	@Test
	public void testReplaceIllegalChars()
	{
		assertEquals(unitResolverImpl.replaceIllegalChars("area density (kg/m^2)"), "area density  kg/m^2 ");
		assertEquals(unitResolverImpl.replaceIllegalChars("area density (kg/m²)"), "area density  kg/m2 ");
	}

	@Test
	public void resolveUnitLabelNoUnit()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("weight").setDescription(null);
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertNull(unit);
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionNoUnit()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("weight").setDescription("weight");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertNull(unit);
	}

	@Test
	public void resolveUnitLabelWithUnit_directUnitMatch()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("weight (kg)").setDescription(null);
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatch()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("label")
				.setDescription("height (cm)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("cm"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatchRaw_kgm2()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("label")
				.setDescription("area density (kg/m2)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg/m²"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_unitOntologyMatch_kgm2()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("label")
				.setDescription("area density (kg/m^2)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg/m²"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatch_kgm2_2()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("label")
				.setDescription("area density (kg/m²)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg/m²"));
	}

	@Test
	public void resolveUnitLabelWithUnit_unitOntologyMatch()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("weight (kilogram)")
				.setDescription(null);
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_unitOntologyMatch()
	{
		AttributeMetaData attr = attrMetaFactory.create().setName("attr").setLabel("label")
				.setDescription("height (centimeter)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("cm"));
	}

	@Configuration
	static class Config
	{
		@Bean
		public UnitResolverImpl unitResolverImpl()
		{
			return new UnitResolverImpl(ontologyService());
		}

		@Bean
		public OntologyService ontologyService()
		{
			String ontologyId = "id";
			String kgTerm = "kilogram";
			String cmTerm = "centimeter";
			List<String> ontologyIds = singletonList(ontologyId);

			Ontology ontology = Ontology.create(ontologyId, UNIT_ONTOLOGY_IRI, "unit ontology");
			OntologyTermImpl KG_ONTOLOGY_TERM = OntologyTermImpl
					.create(UNIT_ONTOLOGY_IRI, kgTerm, asList(kgTerm, "kg"));
			OntologyTermImpl CM_ONTOLOGY_TERM = OntologyTermImpl
					.create(UNIT_ONTOLOGY_IRI, cmTerm, asList(cmTerm, "cm"));

			OntologyService ontologyService = mock(OntologyService.class);
			when(ontologyService.getOntology(UNIT_ONTOLOGY_IRI)).thenReturn(ontology);

			when(ontologyService
					.findExcatOntologyTerms(ontologyIds, Sets.newLinkedHashSet(asList("weight", "kilogram")),
							Integer.MAX_VALUE)).thenReturn(singletonList(KG_ONTOLOGY_TERM));
			when(ontologyService
					.findExcatOntologyTerms(ontologyIds, Sets.newLinkedHashSet(asList("label", "height", "centimeter")),
							Integer.MAX_VALUE)).thenReturn(singletonList(CM_ONTOLOGY_TERM));

			when(ontologyService.findExcatOntologyTerms(ontologyIds, newHashSet(kgTerm, cmTerm), Integer.MAX_VALUE))
					.thenReturn(asList(KG_ONTOLOGY_TERM, CM_ONTOLOGY_TERM));
			return ontologyService;
		}
	}
}
