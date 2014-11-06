package org.molgenis.data.annotation;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import org.molgenis.data.Entity;

public class VcfUtils {
	
	public static String convertToVCF(Entity e)
	{
		
		return null;
	}
	
	public static boolean checkInput(File inputVcfFile, File outputVCFFile, Scanner inputVcfFileScanner, PrintWriter outputVCFWriter, List<String> infoFields, String checkAnnotatedBeforeValue) throws Exception
	{
		boolean annotatedBefore = false;
		
		System.out.println("Detecting VCF column header...");

        String line = inputVcfFileScanner.nextLine();
        
        //if first line does not start with ##, we dont trust this file as VCF
        if(line.startsWith("##"))
        {
        	while(inputVcfFileScanner.hasNextLine())
        	{
        		//detect existing annotations of the same info field
        		if(line.contains("##INFO=<ID="+checkAnnotatedBeforeValue) && !annotatedBefore)
        		{
        			System.out.println("\nThis file has already been annotated with '"+checkAnnotatedBeforeValue+"' data before it seems. Skipping any further annotation of variants that already contain this field.");
        			annotatedBefore = true;
        		}
        		
        		//read and print to output until we find the header
        		outputVCFWriter.println(line);
        		line = inputVcfFileScanner.nextLine();
        		if(!line.startsWith("##"))
        		{
        			break;
        		}
        		System.out.print(".");
        	}
        	System.out.println("\nHeader line found:\n" + line);
        	
        	//check the header line
        	if(!line.startsWith("#CHROM"))
        	{
        		outputVCFWriter.close();
        		throw new Exception("Header does not start with #CHROM, are you sure it is a VCF file?");
        	}
        	
        	//print INFO lines for stuff to be annotated
        	if(!annotatedBefore)
        	{
        		for(String infoField : infoFields)
        		{
        			outputVCFWriter.println(infoField);
        		}
         	}
        	
        	//now print header
        	outputVCFWriter.println(line);
        }
        else
        {
        	outputVCFWriter.close();
        	throw new Exception("Did not find ## on the first line, are you sure it is a VCF file?");
        }
        
        System.out.println("Now starting to process the data.");
        return annotatedBefore;
	}

}
