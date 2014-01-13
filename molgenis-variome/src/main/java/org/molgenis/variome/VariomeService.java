package org.molgenis.variome;

import java.io.File;

import org.springframework.ui.Model;

/**
 * Interface that must be implemented by a VariomeService
 * 
 * @author Mark-de-Haan
 * 
 */
public interface VariomeService
{
	File vcfFile(File vcfFile, Model model);
}
