INSERT 
	import_patientsview (`Patient ID`, `Phenotype`, `Mutation`, `cDNA change`, `Protein change`, `Exon`, `Consequence`, `Reference`)
SELECT *
FROM
	(SELECT 
		import_patients.`Patient ID`, 
		import_patients.Pheno AS `Phenotype`, 
		"First mutation" AS `Mutation`, 
		IFNULL(import_mutations.`cdna_notation`, 'null') AS `cDNA change`, 
		IFNULL(import_mutations.`aa_notation`, 'null') AS `Protein change`, 
		IFNULL(import_mutations.`exon`, 'null') AS `Exon`, 
		IFNULL(import_mutations.`Consequence`, 'null') AS `Consequence`,
		import_patients.`Reference` AS `Reference`
	FROM import_patients
	LEFT JOIN import_mutations 
	ON import_patients.`cDNA change 1` = import_mutations.cdna_notation

	UNION ALL

	SELECT 
		import_patients.`Patient ID`, 
		import_patients.Pheno AS `Phenotype`, 
		"Second mutation" AS "Mutation", 
		IFNULL(import_mutations.`cdna_notation`, 'null') AS `cDNA change`, 
		IFNULL(import_mutations.`aa_notation`, 'null') AS `Protein change`, 
		IFNULL(import_mutations.`exon`, 'null') AS `Exon`, 
		IFNULL(import_mutations.`Consequence`, 'null') AS `Consequence`,
		import_patients.`Reference` AS `Reference`
	FROM import_patients
	LEFT JOIN import_mutations 
	ON import_patients.`cDNA change 2` = import_mutations.cdna_notation) 
	AS uTable
ORDER BY `Patient ID`