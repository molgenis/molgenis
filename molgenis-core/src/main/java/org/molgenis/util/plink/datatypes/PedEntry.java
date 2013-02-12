package org.molgenis.util.plink.datatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class PedEntry extends FamEntry implements Iterable<Biallele>
{

	// list iterates SNP's, so 1 list per individual
	private final Iterator<Biallele> bialleles;

	public PedEntry(String family, String individual, String father, String mother, byte sex, double phenotype,
			Iterator<Biallele> bialleles)
	{
		super(family, individual, father, mother, sex, phenotype);
		this.bialleles = bialleles;
	}

	@Override
	public Iterator<Biallele> iterator()
	{
		return bialleles;
	}

	public List<Biallele> getBialleles()
	{
		List<Biallele> bialleleList = new ArrayList<Biallele>();
		Iterator<Biallele> it = iterator();
		while (it.hasNext())
		{
			bialleleList.add(it.next());
		}

		return bialleleList;
	}

	public static String[] pedHeader()
	{
		return new String[]
		{ "fam", "ind", "fa", "mo", "sex", "phen", "bial" };
	}

	public static Tuple pedToTuple(PedEntry ped)
	{
		WritableTuple tuple = new KeyValueTuple();
		tuple.set("fam", ped.getFamily());
		tuple.set("ind", ped.getIndividual());
		tuple.set("fa", ped.getFather());
		tuple.set("mo", ped.getMother());
		tuple.set("sex", ped.getSex());
		tuple.set("phen", ped.getPhenotype());
		tuple.set("bial", ped.getBialleles());
		return tuple;
	}
}
