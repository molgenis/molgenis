package org.molgenis.data.mapper.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.mapper.service.impl.UnitResolverImpl.UNIT_ONTOLOGY_IRI;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

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
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setLabel("label")
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
			List<String> ontologyIds = Arrays.asList(ontologyId);

			Ontology ontology = Ontology.create(ontologyId, UNIT_ONTOLOGY_IRI, "unit ontology");
			KG_ONTOLOGY_TERM = OntologyTerm.create(UNIT_ONTOLOGY_IRI, kgTerm, Arrays.asList(kgTerm, "kg"));
			CM_ONTOLOGY_TERM = OntologyTerm.create(UNIT_ONTOLOGY_IRI, cmTerm, Arrays.asList(cmTerm, "cm"));

			OntologyService ontologyService = mock(OntologyService.class);
			when(ontologyService.getOntology(UNIT_ONTOLOGY_IRI)).thenReturn(ontology);
			when(ontologyService.findOntologyTerms(ontologyIds, Sets.newHashSet(kgTerm), Integer.MAX_VALUE))
					.thenReturn(Arrays.asList(KG_ONTOLOGY_TERM));
			when(ontologyService.findOntologyTerms(ontologyIds, Sets.newHashSet(cmTerm), Integer.MAX_VALUE))
					.thenReturn(Arrays.asList(CM_ONTOLOGY_TERM));
			when(ontologyService.findOntologyTerms(ontologyIds, Sets.newHashSet(kgTerm, cmTerm), Integer.MAX_VALUE))
					.thenReturn(Arrays.asList(KG_ONTOLOGY_TERM, CM_ONTOLOGY_TERM));
			return ontologyService;
		}
	}
}
