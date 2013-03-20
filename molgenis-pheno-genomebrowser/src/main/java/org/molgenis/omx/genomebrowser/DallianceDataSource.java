package org.molgenis.omx.genomebrowser;

public class DallianceDataSource
{
	/*{name:                 'Genome',      
        uri:                  'http://www.derkholm.net:8080/das/hg18comp/',        
        tier_type:            'sequence',
        provides_entrypoints: true},
       {name:                 'Genes',     
        desc:                 'Gene structures from Ensembl 54',
        uri:                  'http://www.derkholm.net:8080/das/hsa_54_36p/',      
        collapseSuperGroups:  true,
        provides_karyotype:   true,
        provides_search:      true},
       {name:                 'Repeats',     
        uri:                  'http://www.derkholm.net:8080/das/hsa_54_36p/',      
        stylesheet_uri:       'http://www.derkholm.net/dalliance-test/stylesheets/ens-repeats.xml'},
       {name:                 'MeDIP raw',
        uri:                  'http://www.derkholm.net:8080/das/medipseq_reads'},
       {name:                 'MeDIP-seq',
        uri: 
                         'http://www.ebi.ac.uk/das-srv/genomicdas/das/batman_seq_SP/'}**/
	private final String name;
	private final String uri;
	private String tierType = null;
    private Boolean collapseSuperGroups = null;
    private Boolean provides_karyotype = null;
    private Boolean provides_search = null;
    private Boolean provides_entrypoints = null;
    
	public DallianceDataSource(String name, String uri, String tierType, boolean collapseSuperGroups,
			boolean provides_karyotype, boolean provides_search, boolean provides_entrypoints)
	{
		super();
		this.name = name;
		this.uri = uri;
		this.tierType = tierType;
		this.collapseSuperGroups = collapseSuperGroups;
		this.provides_karyotype = provides_karyotype;
		this.provides_search = provides_search;
		this.provides_entrypoints = provides_entrypoints;
	}
	
	public DallianceDataSource(String name, String uri)
	{
		super();
		this.name = name;
		this.uri = uri;
	}

	@Override
	public String toString()
	{
		StringBuilder string = new StringBuilder();
		string.append("{name:'" + name + "'");
		string.append(",uri:'" + uri + "'");
		if(tierType != null) string.append(", tierType:'" + tierType + "'");
		if(collapseSuperGroups != null) string.append(", collapseSuperGroups:" + collapseSuperGroups);
		if(provides_karyotype != null) string.append(", provides_karyotype:" + provides_karyotype);
		if(provides_search != null) string.append(", provides_search:" + provides_search);
		if(provides_entrypoints != null) string.append(", provides_entrypoints:" + provides_entrypoints);
		string.append("}");
		
		System.out.println("DATASOURCE String: "+string);
		return string.toString();
	}

	public String getName()
	{
		return name;
	}

	public String getUri()
	{
		return uri;
	}

	public String getTierType()
	{
		return tierType;
	}

	public boolean isCollapseSuperGroups()
	{
		return collapseSuperGroups;
	}

	public boolean isProvides_karyotype()
	{
		return provides_karyotype;
	}

	public boolean isProvides_search()
	{
		return provides_search;
	}

	public boolean isProvides_entrypoints()
	{
		return provides_entrypoints;
	}
	
	

}