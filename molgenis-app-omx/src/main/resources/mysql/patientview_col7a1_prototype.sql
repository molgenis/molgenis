INSERT import_patientsview (patient_id, identifier_mutation, mutationPosition, patient_reference, codonchange, Mutation)
Select patient_id, identifier_mutation, mutationPosition, patient_reference, codonchange, Mutation
from import_patients
left join 
(select patient_id, identifier_mutation, mutationPosition, patient_reference, codonchange, "First mutation" AS "Mutation"
from import_patients
INNER JOIN (
	(select patient_id, identifier_mutation, reference AS patient_reference, mutationPosition, codonchange  from import_patients
		INNER JOIN import_mutations 
		on import_patients.first_mutation = import_mutations.identifier_mutation) 
		AS first_join) USING(patient_id)

UNION ALL

select patient_id, identifier_mutation, mutationPosition, patient_reference, codonchange, "Second mutation" AS "Mutaion"
from import_patients
INNER JOIN (
	(select patient_id, identifier_mutation, reference AS patient_reference, mutationPosition, codonchange from import_patients
		INNER JOIN import_mutations 
		on import_patients.second_mutation = import_mutations.identifier_mutation) 
		AS second_join) USING(patient_id)) AS uTable

USING(patient_id)
Order by patient_id, mutation;