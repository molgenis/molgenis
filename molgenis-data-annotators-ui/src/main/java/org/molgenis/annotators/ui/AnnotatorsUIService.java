package org.molgenis.annotators.ui;

import org.springframework.ui.Model;

/**
 * Interface that must be implemented by a Annotation UI
 * 
 * @author mdehaan
 * 
 */
public interface AnnotatorsUIService
{
	void tsvToOmxRepository(String file, Model model, String submittedDataSetName);
}
