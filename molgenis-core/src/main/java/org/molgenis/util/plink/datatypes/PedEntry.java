package org.molgenis.util.plink.datatypes;

import java.util.List;

import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class PedEntry extends FamEntry
{

	// list iterates SNP's, so 1 list per individual
	List<Biallele> bialleles;

	public PedEntry(String family, String individual, String father, String mother, byte sex, double phenotype,
			List<Biallele> bialleles)
	{
		super(family, individual, father, mother, sex, phenotype);
		this.bialleles = bialleles;
	}

	public List<Biallele> getBialleles()
	{
		return bialleles;
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
