package org.molgenis.data.annotation.settings;

import org.molgenis.data.Entity;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.stereotype.Component;

@Component
public class AnnotationDbSettings extends DefaultSettingsEntity implements AnnotationSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "annotation";

	public AnnotationDbSettings()
	{
		super(ID);
	}

	@Override
	public String getCaddLocation()
	{
		return getString(Meta.CADD_LOCATION);
	}

	@Override
	public void setCaddLocation(String caddLocation)
	{
		set(Meta.CADD_LOCATION, caddLocation);
	}

	@Override
	public String getCgdLocation()
	{
		return getString(Meta.CGD_LOCATION);
	}

	@Override
	public void setCgdLocation(String cgdLocation)
	{
		set(Meta.CGD_LOCATION, cgdLocation);
	}

	@Override
	public String getClinVarLocation()
	{
		return getString(Meta.CLINVAR_LOCATION);
	}

	@Override
	public void setClinVarLocation(String clinVarLocation)
	{
		set(Meta.CLINVAR_LOCATION, clinVarLocation);
	}

	@Override
	public String getExacLocation()
	{
		return getString(Meta.EXAC_LOCATION);
	}

	@Override
	public void setExacLocation(String exacLocation)
	{
		set(Meta.EXAC_LOCATION, exacLocation);
	}

	@Override
	public String getGoNlLocation()
	{
		return getString(Meta.GONL_LOCATION);
	}

	@Override
	public void setGoNlLocation(String goNlLocation)
	{
		set(Meta.GONL_LOCATION, goNlLocation);
	}

	@Override
	public String getHgncLocation()
	{
		return getString(Meta.HGNC_LOCATION);
	}

	@Override
	public void setHgncLocation(String hgncLocation)
	{
		set(Meta.HGNC_LOCATION, hgncLocation);
	}

	@Override
	public String getHpoLocation()
	{
		return getString(Meta.HPO_LOCATION);
	}

	@Override
	public void setHpoLocation(String hpoLocation)
	{
		set(Meta.HPO_LOCATION, hpoLocation);
	}

	@Override
	public String getDbsnpLocationGene()
	{
		return getString(Meta.DBSNP_LOCATION_GENE);
	}

	@Override
	public void setDbsnpLocationGene(String dbsnpLocationGene)
	{
		set(Meta.DBSNP_LOCATION_GENE, dbsnpLocationGene);
	}

	@Override
	public String getDbsnpLocationVariant()
	{
		return getString(Meta.DBSNP_LOCATION_VARIANT);
	}

	@Override
	public void setDbsnpLocationVariant(String dbsnpLocationVariant)
	{
		set(Meta.DBSNP_LOCATION_VARIANT, dbsnpLocationVariant);
	}

	@Override
	public String getSnpEffLocation()
	{
		return getString(Meta.SNPEFF_LOCATION);
	}

	@Override
	public void setSnpEffLocation(String snpEffLocation)
	{
		set(Meta.SNPEFF_LOCATION, snpEffLocation);
	}

	@Override
	public String getPhenomizerLocation()
	{
		return getString(Meta.PHENOMIZER_LOCATION);
	}

	@Override
	public void setPhenomizerLocation(String phenomizerLocation)
	{
		set(Meta.PHENOMIZER_LOCATION, phenomizerLocation);
	}

	@Override
	public String get1000GLocation()
	{
		return getString(Meta.THOUSAND_G_LOCATION);
	}

	@Override
	public void set1000GLocation(String thousandGLocation)
	{
		set(Meta.THOUSAND_G_LOCATION, thousandGLocation);
	}

	@Override
	public String getKeggPathway()
	{
		return getString(Meta.KEGG_PATHWAY);
	}

	@Override
	public void setKeggPathway(String keggPathway)
	{
		set(Meta.KEGG_PATHWAY, keggPathway);
	}

	@Override
	public String getKeggHsa()
	{
		return getString(Meta.KEGG_HSA);
	}

	@Override
	public void setKeggHsa(String keggHsa)
	{
		set(Meta.KEGG_HSA, keggHsa);
	}

	@Override
	public String getKeggPathwayHsaLink()
	{
		return getString(Meta.DEFAULT_KEGG_PATHWAY_HSA_LINK);
	}

	@Override
	public void setKeggPathwayHsaLink(String keggPathwayHsaLink)
	{
		set(Meta.DEFAULT_KEGG_PATHWAY_HSA_LINK, keggPathwayHsaLink);
	}

	@Override
	public String getOmimMorbidMapLocation()
	{
		return getString(Meta.OMIM_MORBIDMAP_LOCATION);
	}

	@Override
	public void setOmimMorbidMapLocation(String omimMorbidMapLocation)
	{
		set(Meta.OMIM_MORBIDMAP_LOCATION, omimMorbidMapLocation);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String CADD_LOCATION = "cadd_location";
		public static final String CGD_LOCATION = "cgd_location";
		private static final String CLINVAR_LOCATION = "clinvar_loc";
		public static final String EXAC_LOCATION = "exac_location";
		private static final String GONL_LOCATION = "gonl_location";
		private static final String HGNC_LOCATION = "hgnc_location";
		private static final String HPO_LOCATION = "hpo_location";
		private static final String DBSNP_LOCATION_GENE = "dbsnp_gene_location";
		private static final String DBSNP_LOCATION_VARIANT = "dbsnp_variant_location";
		private static final String SNPEFF_LOCATION = "snpeff_location";
		private static final String PHENOMIZER_LOCATION = "snpeff_location";
		private static final String THOUSAND_G_LOCATION = "thousand_g_location";
		private static final String KEGG_PATHWAY = "kegg_pathway";
		private static final String KEGG_HSA = "kegg_hsa";
		private static final String KEGG_PATHWAY_HSA_LINK = "kegg_pathway_hsa_link";
		private static final String OMIM_MORBIDMAP_LOCATION = "omim_morbidmap_location";

		private static final String RESOURCES_HOME = System.getProperty("molgenis.home") + "/data/annotation_resources";
		private static final String DEFAULT_CADD_LOCATION = RESOURCES_HOME + "/CADD/1000G.vcf.gz";
		private static final String DEFAULT_CGD_LOCATION = RESOURCES_HOME + "/CGD/CGD.txt";
		private static final String DEFAULT_CLINVAR_LOCATION = RESOURCES_HOME + "/Clinvar/variant_summary.txt";
		private static final String DEFAULT_HGNC_LOCATION = "https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv";
		private static final String DEFAULT_HPO_LOCATION = "https://molgenis26.target.rug.nl/downloads/5gpm/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt";
		private static final String DEFAULT_DBSNP_LOCATION_GENE = RESOURCES_HOME + "/dbnsfp/dbNSFP2.3_gene";
		private static final String DEFAULT_DBSNP_LOCATION_VARIANT = RESOURCES_HOME + "/dbnsfp/dbNSFP2.3_variant.chr";
		private static final String DEFAULT_SNPEFF_LOCATION = RESOURCES_HOME + "/Applications/snpEff/snpEff.jar";
		private static final String DEFAULT_PHENOMIZER_LOCATION = "http://compbio.charite.de/phenomizer/phenomizer/PhenomizerServiceURI";
		private static final String DEFAULT_KEGG_PATHWAY = "http://rest.kegg.jp/list/pathway/hsa";
		private static final String DEFAULT_KEGG_HSA = "http://rest.kegg.jp/list/hsa";
		private static final String DEFAULT_KEGG_PATHWAY_HSA_LINK = "http://rest.kegg.jp/link/hsa/pathway";
		private static final String DEFAULT_OMIM_MORBIDMAP_LOCATION = "https://molgenis26.target.rug.nl/downloads/5gpm/morbidmap";

		public Meta()
		{
			super(ID);
			setLabel("Annotation settings");
			setDescription("Settings for various dataset annotators.");

			addAttribute(CADD_LOCATION).setNillable(false).setDefaultValue(DEFAULT_CADD_LOCATION)
					.setLabel("CADD location");
			addAttribute(CGD_LOCATION).setNillable(false).setDefaultValue(DEFAULT_CGD_LOCATION)
					.setLabel("CGD location");
			addAttribute(CLINVAR_LOCATION).setNillable(false).setDefaultValue(DEFAULT_CLINVAR_LOCATION)
					.setLabel("ClinVar location");
			addAttribute(EXAC_LOCATION).setLabel("ExAC location");
			addAttribute(GONL_LOCATION).setLabel("GoNL location");
			addAttribute(HGNC_LOCATION).setNillable(false).setDefaultValue(DEFAULT_HGNC_LOCATION)
					.setLabel("HGNC location");
			addAttribute(HPO_LOCATION).setNillable(false).setDefaultValue(DEFAULT_HPO_LOCATION)
					.setLabel("HPO location");
			addAttribute(DBSNP_LOCATION_GENE).setNillable(false).setDefaultValue(DEFAULT_DBSNP_LOCATION_GENE)
					.setLabel("dbSNP gene location");
			addAttribute(DBSNP_LOCATION_VARIANT).setNillable(false).setDefaultValue(DEFAULT_DBSNP_LOCATION_VARIANT)
					.setLabel("dbSNP variant location");
			addAttribute(SNPEFF_LOCATION).setNillable(false).setDefaultValue(DEFAULT_SNPEFF_LOCATION)
					.setLabel("SnpEff location");
			addAttribute(PHENOMIZER_LOCATION).setNillable(false).setDefaultValue(DEFAULT_PHENOMIZER_LOCATION)
					.setLabel("Phenomizer location");
			addAttribute(THOUSAND_G_LOCATION).setLabel("1000G location");
			addAttribute(KEGG_PATHWAY).setNillable(false).setDefaultValue(DEFAULT_KEGG_PATHWAY);
			addAttribute(KEGG_HSA).setNillable(false).setDefaultValue(DEFAULT_KEGG_HSA);
			addAttribute(KEGG_PATHWAY_HSA_LINK).setNillable(false).setDefaultValue(DEFAULT_KEGG_PATHWAY_HSA_LINK);
			addAttribute(OMIM_MORBIDMAP_LOCATION).setNillable(false).setDefaultValue(DEFAULT_OMIM_MORBIDMAP_LOCATION);
		}

		@Override
		protected Entity getDefaultSettings()
		{
			// FIXME workaround for https://github.com/molgenis/molgenis/issues/1810
			MapEntity defaultSettings = new MapEntity(this);
			defaultSettings.set(CADD_LOCATION, DEFAULT_CADD_LOCATION);
			defaultSettings.set(CGD_LOCATION, DEFAULT_CGD_LOCATION);
			defaultSettings.set(CLINVAR_LOCATION, DEFAULT_CLINVAR_LOCATION);
			defaultSettings.set(HGNC_LOCATION, DEFAULT_HGNC_LOCATION);
			defaultSettings.set(HPO_LOCATION, DEFAULT_HPO_LOCATION);
			defaultSettings.set(DBSNP_LOCATION_GENE, DEFAULT_DBSNP_LOCATION_GENE);
			defaultSettings.set(DBSNP_LOCATION_VARIANT, DEFAULT_DBSNP_LOCATION_VARIANT);
			defaultSettings.set(SNPEFF_LOCATION, DEFAULT_SNPEFF_LOCATION);
			defaultSettings.set(PHENOMIZER_LOCATION, DEFAULT_PHENOMIZER_LOCATION);
			defaultSettings.set(KEGG_PATHWAY, DEFAULT_KEGG_PATHWAY);
			defaultSettings.set(KEGG_HSA, DEFAULT_KEGG_HSA);
			defaultSettings.set(KEGG_PATHWAY_HSA_LINK, DEFAULT_KEGG_PATHWAY_HSA_LINK);
			defaultSettings.set(OMIM_MORBIDMAP_LOCATION, DEFAULT_OMIM_MORBIDMAP_LOCATION);
			return defaultSettings;
		}
	}
}
