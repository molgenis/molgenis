package org.molgenis.data.mapper.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.mapper.service.impl.UnitResolverImpl.UNIT_ONTOLOGY_IRI;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

@ContextConfiguration(classes = UnitResolverImplTest.Config.class)
public class UnitResolverImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private UnitResolverImpl unitResolverImpl;

	private static OntologyTerm KG_ONTOLOGY_TERM;
	private static OntologyTerm CM_ONTOLOGY_TERM;

	@Test
	public void testConvertNumberToOntologyTermStyle()
	{
		Assert.assertEquals(unitResolverImpl.convertNumberToOntologyTermStyle("kg/m^2"), "kg\\/m\\^\\[2\\]");
		Assert.assertEquals(unitResolverImpl.convertNumberToOntologyTermStyle("kg/m^²"), "kg\\/m\\^\\[2\\]");
		Assert.assertEquals(unitResolverImpl.convertNumberToOntologyTermStyle("kg/m²"), "kg\\/m\\^\\[2\\]");
	}

	@Test
	public void testTokenize()
	{
		Set<String> tokenize = unitResolverImpl.tokenize("area density (kg/m^²)");
		Assert.assertTrue(Sets.newHashSet("area", "density", "kg/m^²").containsAll(tokenize));

		Set<String> tokenize1 = unitResolverImpl.tokenize("area density (kg/m^2)");
		Assert.assertTrue(Sets.newHashSet("area", "density", "kg/m^²").containsAll(tokenize1));

		Set<String> tokenize2 = unitResolverImpl.tokenize("area density (kg/m2)");
		Assert.assertTrue(Sets.newHashSet("area", "density", "kg/m²").containsAll(tokenize2));

		Set<String> tokenize3 = unitResolverImpl.tokenize("area 2 density2 (kg/m2)");
		Assert.assertTrue(Sets.newHashSet("area", "density²", "kg/m²").containsAll(tokenize3));

		Set<String> tokenize4 = unitResolverImpl.tokenize("area 2 density 2 (kg/m2)");
		Assert.assertEquals(tokenize4.size(), 3);
		Assert.assertFalse(tokenize4.containsAll(Sets.newHashSet("area", "density", "²", "kg/m²")));
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
		Assert.assertEquals(unitResolverImpl.replaceIllegalChars("area density (kg/m^2)"), "area density  kg/m^2 ");
		Assert.assertEquals(unitResolverImpl.replaceIllegalChars("area density (kg/m²)"), "area density  kg/m2 ");
	}

	@Test
	public void resolveUnitLabelNoUnit()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("weight").setDescription(null);
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertNull(unit);
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionNoUnit()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("weight").setDescription("weight");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertNull(unit);
	}

	@Test
	public void resolveUnitLabelWithUnit_directUnitMatch()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("weight (kg)").setDescription(null);
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatch()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("label").setDescription("height (cm)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("cm"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatchRaw_kgm2()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("label").setDescription(
				"area density (kg/m2)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg/m²"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_unitOntologyMatch_kgm2()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("label").setDescription(
				"area density (kg/m^2)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg/m²"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatch_kgm2_2()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("label").setDescription(
				"area density (kg/m²)");
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg/m²"));
	}

	@Test
	public void resolveUnitLabelWithUnit_unitOntologyMatch()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("weight (kilogram)")
				.setDescription(null);
		Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr, null);
		assertEquals(unit, Unit.valueOf("kg"));
	}

	@Test
	public void resolveUnitLabelNoUnitDescriptionWithUnit_unitOntologyMatch()
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("label").setDescription(
				"height (centimeter)");
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
			List<String> ontologyIds = Arrays.asList(ontologyId);

			Ontology ontology = Ontology.create(ontologyId, UNIT_ONTOLOGY_IRI, "unit ontology");
			KG_ONTOLOGY_TERM = OntologyTerm.create(UNIT_ONTOLOGY_IRI, kgTerm, Arrays.asList(kgTerm, "kg"));
			CM_ONTOLOGY_TERM = OntologyTerm.create(UNIT_ONTOLOGY_IRI, cmTerm, Arrays.asList(cmTerm, "cm"));

			OntologyService ontologyService = mock(OntologyService.class);
			when(ontologyService.getOntology(UNIT_ONTOLOGY_IRI)).thenReturn(ontology);

			when(
					ontologyService.findExcatOntologyTerms(ontologyIds,
							Sets.newLinkedHashSet(Arrays.asList("weight", "kilogram")), Integer.MAX_VALUE)).thenReturn(
					Arrays.asList(KG_ONTOLOGY_TERM));
			when(
					ontologyService.findExcatOntologyTerms(ontologyIds,
							Sets.newLinkedHashSet(Arrays.asList("label", "height", "centimeter")), Integer.MAX_VALUE))
					.thenReturn(Arrays.asList(CM_ONTOLOGY_TERM));

			when(
					ontologyService.findExcatOntologyTerms(ontologyIds, Sets.newHashSet(kgTerm, cmTerm),
							Integer.MAX_VALUE)).thenReturn(Arrays.asList(KG_ONTOLOGY_TERM, CM_ONTOLOGY_TERM));
			return ontologyService;
		}
	}
}
