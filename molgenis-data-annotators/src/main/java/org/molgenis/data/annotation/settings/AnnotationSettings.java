package org.molgenis.data.annotation.settings;

/**
 * Annotation settings: See {@link org.molgenis.ui.settings.AppDbSettings.Meta} for setting descriptions and allowed
 * return values.
 */
public interface AnnotationSettings
{
	String getCaddLocation();

	void setCaddLocation(String caddLocation);

	String getCgdLocation();

	void setCgdLocation(String cgdLocation);

	String getClinVarLocation();

	void setClinVarLocation(String clinVarLocation);

	String getExacLocation();

	void setExacLocation(String exacLocation);

	String getGoNlLocation();

	void setGoNlLocation(String goNlLocation);

	String getHgncLocation();

	void setHgncLocation(String hgncLocation);

	String getHpoLocation();

	void setHpoLocation(String hpoMappingUrl);

	String getDbsnpLocationGene();

	void setDbsnpLocationGene(String dbsnpLocationGene);

	String getDbsnpLocationVariant();

	void setDbsnpLocationVariant(String dbsnpLocationVariant);

	String getSnpEffLocation();

	void setSnpEffLocation(String snpEffLocation);

	String getPhenomizerLocation();

	void setPhenomizerLocation(String phenomizerLocation);

	String get1000GLocation();

	void set1000GLocation(String thousandGLocation);

	void setKeggPathway(String keggPathway);

	String getKeggPathway();

	String getKeggHsa();

	void setKeggHsa(String keggHsa);

	String getKeggPathwayHsaLink();

	void setKeggPathwayHsaLink(String keggPathwayHsaLink);

	String getOmimMorbidMapLocation();

	void setOmimMorbidMapLocation(String omimMorbidMapLocation);

	/**
	 * @deprecated Use specific getters
	 * @param fileProperty
	 * @return
	 */
	@Deprecated
	String getString(String fileProperty);
}