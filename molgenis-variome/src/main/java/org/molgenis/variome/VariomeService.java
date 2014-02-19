package org.molgenis.variome;

import org.springframework.ui.Model;

/**
 * Interface that must be implemented by a VariomeService
 * 
 * @author mdehaan
 * 
 */
public interface VariomeService
{
	void tsvToOmxRepository(String file, Model model);
}
