package org.molgenis.hpofilter;

public class HpoFilterInput {
	
	// inputs are initially in the format group-id-recursive-hpo
	int group;
	int id;
	boolean recursive;
	String hpo;
	
	public HpoFilterInput(int group, int id, boolean recursive, String hpo) {
		this.group = group;
		this.id = id;
		this.recursive = recursive;
		this.hpo = hpo;
	}
	
	public int group() {
		return group;
	}
	
	public int id() {
		return id;
	}
	
	public boolean recursive () {
		return recursive;
	}
	
	public String hpo() {
		return hpo;
	}
}