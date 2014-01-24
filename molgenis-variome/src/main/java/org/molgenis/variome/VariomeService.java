package org.molgenis.variome;

import java.io.File;
import java.util.List;

import org.springframework.ui.Model;

/**
 * Interface that must be implemented by a VariomeService
 * 
 * @author Mark-de-Haan
 * 
 */
public interface VariomeService
{
	File[] vcfFile(List<File> listOfFiles, Model model);
}
