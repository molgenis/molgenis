package org.molgenis.omx.harmonization.mesh;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class MeshLoader
{
	private MeshTerm topNode = new MeshTerm("Thing", null);

	public MeshLoader(File meshFile) throws JAXBException
	{
		Map<Integer, Set<DescriptorRecord>> map = new HashMap<Integer, Set<DescriptorRecord>>();

		JAXBContext jaxbContext = JAXBContext.newInstance(DescriptorRecordSet.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		DescriptorRecordSet descriptorRecordSet = (DescriptorRecordSet) jaxbUnmarshaller.unmarshal(meshFile);
		for (DescriptorRecord descriptor : descriptorRecordSet.getDescriptorRecords())
		{
			for (String path : descriptor.getTreeNumberList().getTreeNumbers())
			{
				String[] nodes = path.split("\\.");
				if (!map.containsKey(nodes.length)) map.put(nodes.length, new HashSet<DescriptorRecord>());
				map.get(nodes.length).add(descriptor);
			}
		}

		Set<String> paths = new HashSet<String>();
		for (Entry<Integer, Set<DescriptorRecord>> entry : map.entrySet())
		{
			for (DescriptorRecord descriptor : entry.getValue())
			{
				String descriptorLabel = descriptor.getDescriptorName().getName();
				for (String path : descriptor.getTreeNumberList().getTreeNumbers())
				{
					Set<String> synonyms = new HashSet<String>();
					for (Concept concept : descriptor.getConceptList().getConcepts())
					{
						for (Term term : concept.getTermList().getTerms())
						{
							synonyms.add(term.getName());
						}
					}
					synonyms.remove(descriptorLabel);
					MeshTerm parentNode = null;
					String[] nodes = path.split("\\.");
					if (nodes.length == 1)
					{
						if (!paths.contains(path)) parentNode = topNode;
					}
					else if (nodes.length == entry.getKey())
					{
						StringBuilder oldPath = new StringBuilder();
						for (int i = 0; i < nodes.length - 1; i++)
						{
							oldPath.append(nodes[i]).append('.');
						}
						oldPath.delete(oldPath.length() - 1, oldPath.length());

						if (paths.contains(oldPath.toString())) parentNode = topNode.get(oldPath.toString());
						else parentNode = topNode;
					}
					MeshTerm subTerm = new MeshTerm(path, descriptorLabel, parentNode);
					subTerm.setDefinition(descriptor.getAnnotation());
					subTerm.setSynonyms(synonyms);
					paths.add(path);
				}
			}
		}
	}

	public MeshTerm getTopNode()
	{
		return topNode;
	}
}