INSERT 
	import_mutationsview 
		(`first_identifier_mutation`,
		`second_identifier_mutation`,	
		`Mutation ID`,
		`cDNA change`,
		`Protein change`,
		`Exon/Intron`,
		`Consequence`,
		`Inheritance`,
		`Patient ID`,
		`Phenotype`)
SELECT 
	first_identifier_mutation,
	second_identifier_mutation,
    `Mutation ID` AS `Mutation ID`,
    cdna_notation AS `cDNA change`,
    aa_notation AS `Protein change`,
    exon AS `Exon/Intron`,
    consequence AS `Consequence`,
    inheritance AS `Inheritance`,
    `Patient ID` AS `Patient ID`,
    `Pheno` AS `Phenotype`
FROM
    (
		(SELECT 
        import_mutations.`identifier_mutation` AS 'first_identifier_mutation',
		'' AS 'second_identifier_mutation',
		import_mutations.`identifier_mutation` AS 'Mutation ID',
		import_mutations.`cdna_notation`,
		import_mutations.`aa_notation`,
		import_mutations.`exon`,
		import_mutations.`consequence`,
		import_mutations.`inheritance`,
		'' AS 'Patient ID',
		'' AS 'Pheno'
		FROM import_mutations) 

		UNION ALL 

		(SELECT
				IFNULL(import_mutations.`identifier_mutation`, "unknown") AS 'first_identifier_mutation',
				IFNULL(sub_mutationview.`Mutation ID`, "unknown") AS 'second_identifier_mutation',
				CONCAT('+ ', IFNULL(sub_mutationview.`Mutation ID`, "unknown")) AS 'Mutation ID',
				sub_mutationview.`cdna_notation`,
				sub_mutationview.`aa_notation`,
				sub_mutationview.`exon`,
				sub_mutationview.`consequence`,
				sub_mutationview.`inheritance`,
				sub_mutationview.`Patient ID`,
				sub_mutationview.`Pheno`
			FROM
				(SELECT 
					import_patients.`cDNA change 1` AS 'cDNA_change_1',
					import_mutations.`identifier_mutation` AS 'Mutation ID',
					import_mutations.`cdna_notation`,
					import_mutations.`aa_notation`,
					import_mutations.`exon`,
					import_mutations.`consequence`,
					import_mutations.`inheritance`,
					import_patients.`Patient ID`,
					import_patients.`Pheno`
				FROM import_patients
					LEFT JOIN import_mutations 
					ON import_patients.`cDNA change 2` = import_mutations.`cdna_notation`) 
				AS sub_mutationview
			LEFT JOIN import_mutations
			ON sub_mutationview.`cDNA_change_1` = import_mutations.`cdna_notation`)

		UNION ALL 
		
			(SELECT
				IFNULL(import_mutations.`identifier_mutation`, "unknown") AS 'first_identifier_mutation',
				IFNULL(sub_mutationview.`Mutation ID`, "unknown") AS 'second_identifier_mutation',
				CONCAT('+ ', IFNULL(sub_mutationview.`Mutation ID`, "unknown"))  AS 'Mutation ID',
				sub_mutationview.`cdna_notation`,
				sub_mutationview.`aa_notation`,
				sub_mutationview.`exon`,
				sub_mutationview.`consequence`,
				sub_mutationview.`inheritance`,
				sub_mutationview.`Patient ID`,
				sub_mutationview.`Pheno`
			FROM
				(SELECT 
					import_patients.`cDNA change 2` AS 'cDNA_change_2',
					import_mutations.`identifier_mutation` AS 'Mutation ID',
					import_mutations.`cdna_notation`,
					import_mutations.`aa_notation`,
					import_mutations.`exon`,
					import_mutations.`consequence`,
					import_mutations.`inheritance`,
					import_patients.`Patient ID`,
					import_patients.`Pheno`
				FROM import_patients
					LEFT JOIN import_mutations
					ON import_patients.`cDNA change 1` = import_mutations.`cdna_notation`) 
				AS sub_mutationview
			LEFT JOIN import_mutations
			ON sub_mutationview.`cDNA_change_2` = import_mutations.`cdna_notation`)
		) AS uTable
GROUP BY
	uTable.first_identifier_mutation,
	uTable.second_identifier_mutation,	
	uTable.`Mutation ID`,
    uTable.`cdna_notation`,
    uTable.`aa_notation`,
    uTable.`exon`,
    uTable.`consequence`,
    uTable.`inheritance`,
    uTable.`Patient ID`,
    uTable.`Pheno`
ORDER BY uTable.first_identifier_mutation, uTable.second_identifier_mutation;