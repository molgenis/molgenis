package org.molgenis.data.annotation.impl;

import org.testng.annotations.BeforeMethod;

public class KeggAnnotatorServiceTest
{
	@BeforeMethod
	public void beforeMethod(){
		
	}
}

//public static void main(String[] args) throws Exception
//{
//	// includes a gene without HGNC symbol, and a gene not related to OMIM/HPO terms
//	List<Locus> loci = new ArrayList<Locus>(Arrays.asList(new Locus("2", 58453844l), new Locus("2", 71892329l),
//			new Locus("2", 73679116l), new Locus("10", 112360316l), new Locus("11", 2017661l), new Locus("1",
//					18151726l), new Locus("1", -1l), new Locus("11", 6637740l)));
//
//	List<Entity> inputs = new ArrayList<Entity>();
//	for (Locus l : loci)
//	{
//		HashMap<String, Object> inputMap = new HashMap<String, Object>();
//		inputMap.put(CHROMOSOME, l.getChrom());
//		inputMap.put(POSITION, l.getPos());
//		inputs.add(new MapEntity(inputMap));
//	}
//
//	Iterator<Entity> res = new KeggAnnotator().annotate(inputs.iterator());
//	while (res.hasNext())
//	{
//		System.out.println(res.next().toString());
//	}
//
//}