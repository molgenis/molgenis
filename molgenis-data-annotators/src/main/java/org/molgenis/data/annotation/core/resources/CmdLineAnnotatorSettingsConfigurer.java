package org.molgenis.data.annotation.core.resources;

/**
 * Each annotator must supply a concrete instance of this interface to configure the MolgenisSettings for the
 * CmdLineAnnotator
 */
public interface CmdLineAnnotatorSettingsConfigurer
{
	void addSettings(String annotationSourceFileName);
}
