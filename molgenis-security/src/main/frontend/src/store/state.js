// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  me: {
    username: 'admin'
  },
  selectedSids: [],
  sids: [{authority: 'USER'}, {authority: 'SU'}],
  selectedEntityTypeId: null,
  permissions: ['ADMIN', 'WRITE', 'READ', 'COUNT'],
  filter: null,
  acls: [
    {
      entityId: 'home',
      entityLabel: 'Home',
      aces: [{
        permissions: ['READ', 'WRITE'],
        granted: true
      }],
      owner: {
        username: 'SYSTEM'
      }
    }, {
      entityId: 'dataexplorer',
      entityLabel: 'Data Explorer',
      aces: [{
        permissions: ['ADMIN'],
        granted: false
      }],
      owner: {
        username: 'SYSTEM'
      }
    }, {
      entityId: 'permissions',
      entityLabel: 'Permissions',
      aces: [{
        permissions: ['READ', 'WRITE', 'COUNT'],
        granted: true
      }],
      owner: {
        username: 'SYSTEM'
      }
    }],
  entityTypes: [
    {
      '_href': '/api/v2/sys_md_EntityType/sys_idx_IndexActionGroup',
      'id': 'sys_idx_IndexActionGroup',
      'label': 'Index action group',
      'description': 'This entity is used to group the index actions.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_StaticContent',
      'id': 'sys_StaticContent',
      'label': 'Static content'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_L10nString',
      'id': 'sys_L10nString',
      'label': 'Localization',
      'description': 'Translated language strings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_scr_ScriptType',
      'id': 'sys_scr_ScriptType',
      'label': 'Script type'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_JobExecution',
      'id': 'sys_job_JobExecution',
      'label': 'Job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_Ontology',
      'id': 'sys_ont_Ontology',
      'label': 'Ontology'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_scr_ScriptParameter',
      'id': 'sys_scr_ScriptParameter',
      'label': 'Script parameter'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_TermFrequency',
      'id': 'sys_ont_TermFrequency',
      'label': 'Term frequency'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_Tag',
      'id': 'sys_md_Tag',
      'label': 'Tag',
      'description': 'Semantic tags that can be applied to entities, attributes and other data'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_OntologyTermNodePath',
      'id': 'sys_ont_OntologyTermNodePath',
      'label': 'Ontology term node path'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_settings',
      'id': 'sys_set_settings',
      'label': 'Settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_OntologyTermHit',
      'id': 'sys_ont_OntologyTermHit',
      'label': 'Ontology term hit'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_OntologyTermSynonym',
      'id': 'sys_ont_OntologyTermSynonym',
      'label': 'Ontology term synonym'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_MatchingTaskContent',
      'id': 'sys_ont_MatchingTaskContent',
      'label': 'Matching task content'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_Owned',
      'id': 'sys_sec_Owned',
      'label': 'Owned'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_User',
      'id': 'sys_sec_User',
      'label': 'User',
      'description': 'Anyone who can login'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_FreemarkerTemplate',
      'id': 'sys_FreemarkerTemplate',
      'label': 'Freemarker template'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_authority',
      'id': 'sys_sec_authority',
      'label': 'Authority'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_OntologyTermDynamicAnnotation',
      'id': 'sys_ont_OntologyTermDynamicAnnotation',
      'label': 'Ontology term dynamic annotation'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_Language',
      'id': 'sys_Language',
      'label': 'Language',
      'description': 'Web application languages'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_Property',
      'id': 'sys_md_Property',
      'label': 'Property',
      'description': 'Abstract class for key/value properties.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_map_AttributeMapping',
      'id': 'sys_map_AttributeMapping',
      'label': 'Attribute mapping'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_CGD',
      'id': 'sys_set_CGD',
      'label': 'CGD annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_SortaJobExecution',
      'id': 'sys_job_SortaJobExecution',
      'label': 'SORTA job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_ScriptJobExecution',
      'id': 'sys_job_ScriptJobExecution',
      'label': 'Script job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_genomicdata',
      'id': 'sys_set_genomicdata',
      'label': 'Genomic data settings',
      'description': 'Settings for genomic data sets.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_hpo',
      'id': 'sys_set_hpo',
      'label': 'HPO annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_clinvar',
      'id': 'sys_set_clinvar',
      'label': 'Clinvar annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_Questionnaire',
      'id': 'sys_Questionnaire',
      'label': 'Questionnaire'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_dataexplorer',
      'id': 'sys_set_dataexplorer',
      'label': 'Data explorer settings',
      'description': 'Settings for the data explorer plugin.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_exac',
      'id': 'sys_set_exac',
      'label': 'Exac annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_Token',
      'id': 'sys_sec_Token',
      'label': 'Token'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_idx_IndexAction',
      'id': 'sys_idx_IndexAction',
      'label': 'Index action'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_UserAuthority',
      'id': 'sys_sec_UserAuthority',
      'label': 'User authority'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_gonl',
      'id': 'sys_set_gonl',
      'label': 'GoNL annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_Gavin',
      'id': 'sys_set_Gavin',
      'label': 'Gavin annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_FileMeta',
      'id': 'sys_FileMeta',
      'label': 'File metadata'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_fitcon',
      'id': 'sys_set_fitcon',
      'label': 'Fitcon annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ImportRun',
      'id': 'sys_ImportRun',
      'label': 'Import',
      'description': 'Data import reports'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_IndexJobExecution',
      'id': 'sys_job_IndexJobExecution',
      'label': 'Index job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_app',
      'id': 'sys_set_app',
      'label': 'Application settings',
      'description': 'General application settings.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_thousandgenomes',
      'id': 'sys_set_thousandgenomes',
      'label': '1000 Genomes annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_dann',
      'id': 'sys_set_dann',
      'label': 'Dann annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ont_OntologyTerm',
      'id': 'sys_ont_OntologyTerm',
      'label': 'Ontology term'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_AnnotationJobExecution',
      'id': 'sys_job_AnnotationJobExecution',
      'label': 'Annotation job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_Group',
      'id': 'sys_sec_Group',
      'label': 'Group'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_GavinJobExecution',
      'id': 'sys_job_GavinJobExecution',
      'label': 'Gavin job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_OMIM',
      'id': 'sys_set_OMIM',
      'label': 'OMIM annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_scr_Script',
      'id': 'sys_scr_Script',
      'label': 'Script'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_cadd',
      'id': 'sys_set_cadd',
      'label': 'Cadd annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_snpEff',
      'id': 'sys_set_snpEff',
      'label': 'SnpEff annotator settings'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_map_EntityMapping',
      'id': 'sys_map_EntityMapping',
      'label': 'Entity mapping'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_MappingJobExecution',
      'id': 'sys_job_MappingJobExecution',
      'label': 'Mapping Job Execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_mail_JavaMailProperty',
      'id': 'sys_mail_JavaMailProperty',
      'label': 'Mail sender properties.',
      'description': 'See https://javamail.java.net/nonav/docs/api/ for a description of the properties you can use.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_GroupAuthority',
      'id': 'sys_sec_GroupAuthority',
      'label': 'Group authority'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_map_MappingTarget',
      'id': 'sys_map_MappingTarget',
      'label': 'Mapping target'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_GroupMember',
      'id': 'sys_sec_GroupMember',
      'label': 'Group member'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_FileIngestJobExecution',
      'id': 'sys_job_FileIngestJobExecution',
      'label': 'File ingest job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_AmazonBucketJobExecution',
      'id': 'sys_job_AmazonBucketJobExecution',
      'label': 'Amazon Bucket file ingest job execution'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_App',
      'id': 'sys_App',
      'label': 'App'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_map_MappingProject',
      'id': 'sys_map_MappingProject',
      'label': 'Mapping project'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_ScheduledJobType',
      'id': 'sys_job_ScheduledJobType',
      'label': 'Scheduled Job Type'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_directory',
      'id': 'sys_set_directory',
      'label': 'Directory settings',
      'description': 'Settings for the Directory - Negotiator interaction.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_job_ScheduledJob',
      'id': 'sys_job_ScheduledJob',
      'label': 'Scheduled job'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_Package',
      'id': 'sys_md_Package',
      'label': 'Package',
      'description': 'Grouping of related entities'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_set_MailSettings',
      'id': 'sys_set_MailSettings',
      'label': 'Mail settings',
      'description': 'Configuration properties for email support. Will be used to send email from Molgenis. See also the MailSenderProp entity.'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_EntityType',
      'id': 'sys_md_EntityType',
      'label': 'Entity type',
      'description': 'Meta data for entity classes'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_Attribute',
      'id': 'sys_md_Attribute',
      'label': 'Attribute',
      'description': 'Meta data for attributes'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_test_FOOD60A1_Ref_test',
      'id': 'base_test_FOOD60A1_Ref_test',
      'label': 'test_FOOD60A1_Ref_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_HOP_GENDER_Ref_selenium',
      'id': 'base_HOP_GENDER_Ref_selenium',
      'label': 'HOP_GENDER_Ref_selenium'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_FOOD_POTATOES_Ref_selenium',
      'id': 'base_FOOD_POTATOES_Ref_selenium',
      'label': 'FOOD_POTATOES_Ref_selenium'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_test_GENDER_Ref_test',
      'id': 'base_test_GENDER_Ref_test',
      'label': 'test_GENDER_Ref_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_test_SEX_Ref_test',
      'id': 'base_test_SEX_Ref_test',
      'label': 'test_SEX_Ref_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_test_HEALTH351_Ref_test',
      'id': 'base_test_HEALTH351_Ref_test',
      'label': 'test_HEALTH351_Ref_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_DIS_HBP_Ref_selenium',
      'id': 'base_DIS_HBP_Ref_selenium',
      'label': 'DIS_HBP_Ref_selenium'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_test_NUCHTER1_Ref_test',
      'id': 'base_test_NUCHTER1_Ref_test',
      'label': 'test_NUCHTER1_Ref_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_test_FOOD59A1_Ref_test',
      'id': 'base_test_FOOD59A1_Ref_test',
      'label': 'test_FOOD59A1_Ref_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_lifelines_test',
      'id': 'base_lifelines_test',
      'label': 'lifelines_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_prevend_test',
      'id': 'base_prevend_test',
      'label': 'prevend_test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_disease_types',
      'id': 'eu_bbmri_eric_disease_types',
      'label': 'disease_types',
      'description': 'Disease types'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_biobank_size',
      'id': 'eu_bbmri_eric_biobank_size',
      'label': 'biobank_size',
      'description': 'Biobank sizes'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_countries',
      'id': 'eu_bbmri_eric_countries',
      'label': 'countries',
      'description': 'Countries'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_ontology_terms',
      'id': 'eu_bbmri_eric_ontology_terms',
      'label': 'ontology_terms',
      'description': 'Base class for value sets'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_persons',
      'id': 'eu_bbmri_eric_persons',
      'label': 'persons',
      'description': 'Contact Information'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_lab_standards',
      'id': 'eu_bbmri_eric_lab_standards',
      'label': 'lab_standards',
      'description': 'Laboratory Standards'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_age_units',
      'id': 'eu_bbmri_eric_age_units',
      'label': 'age_units',
      'description': 'Age units'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_material_types',
      'id': 'eu_bbmri_eric_material_types',
      'label': 'material_types',
      'description': 'Material types'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_data_types',
      'id': 'eu_bbmri_eric_data_types',
      'label': 'data_types',
      'description': 'Data categories'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_temp_types',
      'id': 'eu_bbmri_eric_temp_types',
      'label': 'temp_types',
      'description': 'Storage temperature types'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_image_data_types',
      'id': 'eu_bbmri_eric_image_data_types',
      'label': 'image_data_types',
      'description': 'Image dataset types'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_ops_standards',
      'id': 'eu_bbmri_eric_ops_standards',
      'label': 'ops_standards',
      'description': 'Operational Standards'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_sex_types',
      'id': 'eu_bbmri_eric_sex_types',
      'label': 'sex_types',
      'description': 'Sex types'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_body_parts',
      'id': 'eu_bbmri_eric_body_parts',
      'label': 'body_parts',
      'description': 'Body parts'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_collection_types',
      'id': 'eu_bbmri_eric_collection_types',
      'label': 'collection_types',
      'description': 'Collection types'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_imaging_modality',
      'id': 'eu_bbmri_eric_imaging_modality',
      'label': 'imaging_modality',
      'description': 'Imaging modalities'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_capabilities',
      'id': 'eu_bbmri_eric_capabilities',
      'label': 'capabilities',
      'description': 'Capabilities'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_networks',
      'id': 'eu_bbmri_eric_networks',
      'label': 'networks',
      'description': 'Biobank networks'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_biobanks',
      'id': 'eu_bbmri_eric_biobanks',
      'label': 'biobanks',
      'description': 'Biobank (or standalone collection) Organization'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/eu_bbmri_eric_collections',
      'id': 'eu_bbmri_eric_collections',
      'label': 'collections',
      'description': 'Biobanks and sample collections'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/base_HOP_selenium',
      'id': 'base_HOP_selenium',
      'label': 'HOP_selenium'
    }
  ]
}

export default state
