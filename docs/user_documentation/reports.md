#### <a name="entity-report"></a> Entity Report
If you are plotting your own data, a nice trick is to define a FreemarkerTemplate with an Entity Report for the entity you're plotting.

For instance, if you have an entity with a SNP_ID attribute, it's as easy as adding `<img src="https://molgenis09.target.rug.nl/scripts/plot-ase/run?snp_id=${entity.getString("SNP_ID")}"/>` into the FreemarkerTemplate for that entity.

This will allow you to generate one or more plots for entities you select in the Data Explorer. See the documentation for Entity Report.
