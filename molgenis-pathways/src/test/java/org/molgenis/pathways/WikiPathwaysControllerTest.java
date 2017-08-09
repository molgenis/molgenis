package org.molgenis.pathways;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.pathways.model.Impact;
import org.molgenis.pathways.model.Pathway;
import org.molgenis.pathways.service.WikiPathwaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { WikiPathwaysControllerTest.Config.class })
public class WikiPathwaysControllerTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public WikiPathwaysService serviceMock()
		{
			return mock(WikiPathwaysService.class);
		}

		@Bean
		public WikiPathwaysController controller()
		{
			return new WikiPathwaysController(serviceMock());
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}
	}

	@Autowired
	private WikiPathwaysController controller;
	@Autowired
	private WikiPathwaysService serviceMock;
	@Autowired
	private DataService dataService;
	private EntityType vcf;

	@BeforeTest
	public void init()
	{
		vcf = mock(EntityType.class);
		when(vcf.getId()).thenReturn("VCF");
		Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		Attribute effAttr = when(mock(Attribute.class).getName()).thenReturn("EFF").getMock();
		when(effAttr.getDataType()).thenReturn(STRING);
		when(vcf.getIdAttribute()).thenReturn(idAttr);
		when(vcf.getAtomicAttributes()).thenReturn(asList(idAttr, effAttr));
		when(vcf.getAttribute("id")).thenReturn(idAttr);
		when(vcf.getAttribute("EFF")).thenReturn(effAttr);
	}

	@Test
	public void testGetGeneSymbol()
	{
		assertEquals(controller.getGeneSymbol("TUSC2 / Fus1 , Fusion"), "TUSC2");
		assertEquals(controller.getGeneSymbol("MIR9-1"), "MIR9-1");
		assertEquals(controller.getGeneSymbol("GENE[cytosol]"), "GENE");
		assertEquals(controller.getGeneSymbol("TUSC2abc/adsf"), "TUSC2abc");

	}

	@Test
	public void testAnalyzeGPML() throws ParserConfigurationException, SAXException, IOException
	{
		String gpml = "<gpml>  "
				+ "<DataNode TextLabel='TUSC2 / Fus1 , Fusion' GraphId = 'cf7548' Type='GeneProduct' GroupRef='bced7'>"
				+ "<Graphics CenterX='688.6583271016858' CenterY='681.6145075824545' Width='80.0' Height='20.0' ZOrder='32768' FontSize='10' Valign='Middle' />"
				+ "<Xref Database='Ensembl' ID='ENSG00000197081' />" + "</DataNode>"
				+ "<DataNode TextLabel='IPO4' GraphId='d9af5' Type='GeneProduct' GroupRef='bced7'>"
				+ "<Graphics CenterX='688.6583271016858' CenterY='701.6145075824545' Width='80.0' Height='20.0' ZOrder='32768' FontSize='10' Valign='Middle' />"
				+ "<Xref Database='Ensembl' ID='ENSG00000196497' />" + "</DataNode></gpml>";

		assertEquals(controller.analyzeGPML(gpml),
				ImmutableMultimap.of("TUSC2", "cf7548", "IPO4", "d9af5"));
	}

	@Test
	public void testInit() throws RemoteException
	{
		when(dataService.getEntityTypeIds()).thenReturn(Stream.of("NonVCF", "VCF"));
		EntityType nonVcf = when(mock(EntityType.class).getId()).thenReturn("NonVCF").getMock();
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn("id");
		when(nonVcf.getIdAttribute()).thenReturn(idAttr);
		when(nonVcf.getAtomicAttributes()).thenReturn(singletonList(idAttr));

		when(dataService.getEntityType("NonVCF")).thenReturn(nonVcf);
		when(dataService.getEntityType("VCF")).thenReturn(vcf);

		ExtendedModelMap model = new ExtendedModelMap();
		assertEquals(controller.init(model), "view-pathways");
		assertEquals(model.get("entitiesMeta"), ImmutableList.of(vcf));
	}

	@Test
	public void testGetAllPathways() throws ExecutionException
	{
		List<Pathway> allPathways = asList(Pathway.create("WP1234", "Pathway 1"), Pathway.create("WP12", "Pathway 2"));
		when(serviceMock.getAllPathways("Homo sapiens")).thenReturn(allPathways);
		assertEquals(controller.getAllPathways(), allPathways);
	}

	@Test
	public void testGetColoredPathway()
			throws ParserConfigurationException, SAXException, IOException, ExecutionException
	{
		// {TUSC2=[cf7548], IPO4=[d9af5]}
		when(serviceMock.getPathwayGPML("WP1234")).thenReturn("<gpml>  "
				+ "<DataNode TextLabel='TUSC2 / Fus1 , Fusion' GraphId = 'cf7548' Type='GeneProduct' GroupRef='bced7'>"
				+ "<Graphics CenterX='688.6583271016858' CenterY='681.6145075824545' Width='80.0' Height='20.0' ZOrder='32768' FontSize='10' Valign='Middle' />"
				+ "<Xref Database='Ensembl' ID='ENSG00000197081' />" + "</DataNode>"
				+ "<DataNode TextLabel='&amp;quot;IPO4&amp;quot;' GraphId='d9af5' Type='GeneProduct' GroupRef='bced7'>"
				+ "<Graphics CenterX='688.6583271016858' CenterY='701.6145075824545' Width='80.0' Height='20.0' ZOrder='32768' FontSize='10' Valign='Middle' />"
				+ "<Xref Database='Ensembl' ID='ENSG00000196497' />" + "</DataNode></gpml>");
		@SuppressWarnings("unchecked")
		Repository<Entity> vcfRepo = mock(Repository.class);
		Entity row1 = new DynamicEntity(vcf);
		row1.set("EFF", "INTRON(LOW||||1417|TUSC2|protein_coding|CODING|NM_000057.3|7|1)	GT	1|0");
		Entity row2 = new DynamicEntity(vcf);
		row2.set("EFF", "INTRON(LOW||||1417|IPO4|protein_coding|CODING|NM_000057.3|8|1)	GT	1|0");
		Entity row3 = new DynamicEntity(vcf);
		row2.set("EFF", "INTRON(MODERATE||||1417|IPO4|protein_coding|CODING|NM_000057.3|8|1)	GT	1|0");
		when(vcfRepo.spliterator()).thenReturn(asList(row1, row2, row3).spliterator());
		when(dataService.getRepository("VCF")).thenReturn(vcfRepo);

		when(serviceMock.getColoredPathwayImage("WP1234",
				ImmutableMap.of("cf7548", Impact.LOW, "d9af5", Impact.MODERATE))).thenReturn(
				"<svg>WP1234</svg>");
		assertEquals(controller.getColoredPathway("VCF", "WP1234"), "<svg>WP1234</svg>");
	}

	@Test
	public void testGetColoredPathwayNoGraphIds()
			throws ParserConfigurationException, SAXException, IOException, ExecutionException
	{
		when(serviceMock.getPathwayGPML("WP1234")).thenReturn(
				"<gpml>  " + "<DataNode TextLabel='TUSC2 / Fus1 , Fusion' Type='GeneProduct' GroupRef='bced7'>"
						+ "<Graphics CenterX='688.6583271016858' CenterY='681.6145075824545' Width='80.0' Height='20.0' ZOrder='32768' FontSize='10' Valign='Middle' />"
						+ "<Xref Database='Ensembl' ID='ENSG00000197081' />" + "</DataNode>"
						+ "<DataNode TextLabel='IPO4' Type='GeneProduct' GroupRef='bced7'>"
						+ "<Graphics CenterX='688.6583271016858' CenterY='701.6145075824545' Width='80.0' Height='20.0' ZOrder='32768' FontSize='10' Valign='Middle' />"
						+ "<Xref Database='Ensembl' ID='ENSG00000196497' />" + "</DataNode></gpml>");
		@SuppressWarnings("unchecked")
		Repository<Entity> vcfRepo = mock(Repository.class);
		Entity row1 = new DynamicEntity(vcf);
		row1.set("EFF", "INTRON(LOW||||1417|TUSC2|protein_coding|CODING|NM_000057.3|7|1)	GT	1|0");
		Entity row2 = new DynamicEntity(vcf);
		row2.set("EFF", "INTRON(MODERATE||||1417|IPO4|protein_coding|CODING|NM_000057.3|8|1)	GT	1|0");
		when(vcfRepo.spliterator()).thenReturn(asList(row1, row2).spliterator());
		when(dataService.getRepository("VCF")).thenReturn(vcfRepo);
		when(serviceMock.getUncoloredPathwayImage("WP1234")).thenReturn("<svg>WP1234</svg>");
		assertEquals(controller.getColoredPathway("VCF", "WP1234"), "<svg>WP1234</svg>");
	}

	@Test
	public void testGetPathwaysByGenes() throws ExecutionException
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> vcfRepo = mock(Repository.class);
		Entity row1 = new DynamicEntity(vcf);
		row1.set("EFF", "INTRON(LOW||||1417|TUSC2|protein_coding|CODING|NM_000057.3|7|1)	GT	1|0");
		Entity row2 = new DynamicEntity(vcf);
		row2.set("EFF", "INTRON(MODERATE||||1417|IPO4|protein_coding|CODING|NM_000057.3|8|1)	GT	1|0");
		when(vcfRepo.spliterator()).thenReturn(asList(row1, row2).spliterator());

		when(dataService.getRepository("VCF")).thenReturn(vcfRepo);

		when(serviceMock.getPathwaysForGene("TUSC2", "Homo sapiens")).thenReturn(
				asList(Pathway.create("WP1", "Pathway 1"), Pathway.create("WP2", "Pathway 2")));
		when(serviceMock.getPathwaysForGene("IPO4", "Homo sapiens")).thenReturn(
				asList(Pathway.create("WP3", "Pathway 3"), Pathway.create("WP4", "Pathway 4")));

		assertEquals(controller.getListOfPathwayNamesByGenes("VCF"),
				asList(Pathway.create("WP1", "Pathway 1"), Pathway.create("WP2", "Pathway 2"),
						Pathway.create("WP3", "Pathway 3"), Pathway.create("WP4", "Pathway 4")));
	}

}
