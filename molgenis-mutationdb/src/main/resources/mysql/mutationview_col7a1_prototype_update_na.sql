UPDATE import_mutationsview 
SET `Other Mutation ID`='N/A', `Mutation ID`='+ N/A' 
WHERE (`Other Mutation ID` = 'unknown' OR `Mutation ID` = '+ unknown') 
AND Phenotype LIKE 'DDEB%';