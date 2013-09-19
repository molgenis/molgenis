package org.molgenis.omx.harmonization.mesh;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			File file = new File("/Users/chaopang/Desktop/Ontologies/desc2014.xml");
			// File file = new File("/Users/chaopang/Desktop/example-test.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(DescriptorRecordSet.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			DescriptorRecordSet descriptorRecordSet = (DescriptorRecordSet) jaxbUnmarshaller.unmarshal(file);
			int count = 0;
			int conceptCounts = 0;
			for (DescriptorRecord descriptor : descriptorRecordSet.getDescriptorRecords())
			{
				count++;
				String descriptorName = descriptor.getDescriptorName().getName().trim();
				String descriptorUI = descriptor.getDescriptorUI().trim();

				// Set<String> uniqueSynonyms = new HashSet<String>();
				// System.out.println(descriptorName + " : " + descriptorUI);
				for (Concept concept : descriptor.getConceptList().getConcepts())
				{
					conceptCounts++;
					System.out.println(concept.getConceptUI() + " : " + concept.getConceptName().getName().trim());

					// for (Term term : concept.getTermList().getTerms())
					// {
					// String ternName = term.getName().trim();
					// String termUI = term.getTermUI().trim();
					// }
				}

				// for (String synonym : uniqueSynonyms)
				// {
				// System.out.println(synonym);
				// }
			}
			System.out.println(count + " : " + conceptCounts);
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
		}

	}
}
