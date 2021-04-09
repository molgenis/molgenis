create table "Version"
(
    id integer not null
        constraint "Version_pkey"
            primary key
);

create table acl_sid
(
    id bigserial not null
        constraint acl_sid_pkey
            primary key,
    principal boolean not null,
    sid varchar not null,
    constraint unique_uk_1
        unique (sid, principal)
);

create table acl_class
(
    id bigserial not null
        constraint acl_class_pkey
            primary key,
    class varchar not null
        constraint unique_uk_2
            unique,
    class_id_type varchar not null
);

create table acl_object_identity
(
    id bigserial not null
        constraint acl_object_identity_pkey
            primary key,
    object_id_class bigint not null
        constraint foreign_fk_2
            references acl_class
            on delete cascade,
    object_id_identity varchar not null,
    parent_object bigint
        constraint foreign_fk_1
            references acl_object_identity,
    owner_sid bigint
        constraint foreign_fk_3
            references acl_sid,
    entries_inheriting boolean not null,
    constraint unique_uk_3
        unique (object_id_class, object_id_identity)
);

create table acl_entry
(
    id bigserial not null
        constraint acl_entry_pkey
            primary key,
    acl_object_identity bigint not null
        constraint foreign_fk_4
            references acl_object_identity
            on delete cascade,
    ace_order integer not null,
    sid bigint not null
        constraint foreign_fk_5
            references acl_sid
            on delete cascade,
    mask integer not null,
    granting boolean not null,
    audit_success boolean not null,
    audit_failure boolean not null,
    constraint unique_uk_4
        unique (acl_object_identity, ace_order)
);

create table "sys_md_Tag#c2d685da"
(
    id varchar(255) not null
        constraint "sys_md_Tag#c2d685da_id_pkey"
            primary key,
    "objectIRI" text,
    label varchar(255) not null,
    "relationIRI" varchar(255) not null,
    "relationLabel" varchar(255) not null,
    "codeSystem" varchar(255)
);

create table "sys_md_Package#a6dc6fe7"
(
    id varchar(255) not null
        constraint "sys_md_Package#a6dc6fe7_id_pkey"
            primary key,
    label varchar(255) not null,
    description text,
    parent varchar(255)
        constraint "sys_md_Package#a6dc6fe7_parent_fkey"
            references "sys_md_Package#a6dc6fe7"
            deferrable initially deferred
);

create table "sys_md_Package#a6dc6fe7_tags"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_md_Package#a6dc6fe7_tags_id_fkey"
            references "sys_md_Package#a6dc6fe7"
            on delete cascade
            deferrable initially deferred,
    tags varchar(255) not null
        constraint "sys_md_Package#a6dc6fe7_tags_tags_fkey"
            references "sys_md_Tag#c2d685da"
            deferrable initially deferred,
    constraint "sys_md_Package#a6dc6fe7_tags_id_tags_key"
        unique (id, tags),
    constraint "sys_md_Package#a6dc6fe7_tags_order_id_key"
        unique ("order", id)
);

create index "sys_md_Pac#a6dc6fe7_tags_id_idx"
    on "sys_md_Package#a6dc6fe7_tags" (id);

create table "sys_md_EntityType#6a3870a0"
(
    id varchar(255) not null
        constraint "sys_md_EntityType#6a3870a0_id_pkey"
            primary key,
    label varchar(255) not null,
    description text,
    package varchar(255)
        constraint "sys_md_EntityType#6a3870a0_package_fkey"
            references "sys_md_Package#a6dc6fe7"
            deferrable initially deferred,
    "isAbstract" boolean not null,
    extends varchar(255)
        constraint "sys_md_EntityType#6a3870a0_extends_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    backend varchar(255) not null
        constraint "sys_md_EntityType#6a3870a0_backend_chk"
            check ((backend)::text = 'PostgreSQL'::text),
    "indexingDepth" integer not null,
    "labelEn" varchar(255),
    "descriptionEn" text,
    "labelNl" varchar(255),
    "descriptionNl" text,
    "labelDe" varchar(255),
    "descriptionDe" text,
    "labelEs" varchar(255),
    "descriptionEs" text,
    "labelIt" varchar(255),
    "descriptionIt" text,
    "labelPt" varchar(255),
    "descriptionPt" text,
    "labelFr" varchar(255),
    "descriptionFr" text,
    "labelXx" varchar(255),
    "descriptionXx" text
);

create table "sys_md_EntityType#6a3870a0_tags"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_md_EntityType#6a3870a0_tags_id_fkey"
            references "sys_md_EntityType#6a3870a0"
            on delete cascade
            deferrable initially deferred,
    tags varchar(255) not null
        constraint "sys_md_EntityType#6a3870a0_tags_tags_fkey"
            references "sys_md_Tag#c2d685da"
            deferrable initially deferred,
    constraint "sys_md_EntityType#6a3870a0_tags_id_tags_key"
        unique (id, tags),
    constraint "sys_md_EntityType#6a3870a0_tags_order_id_key"
        unique ("order", id)
);

create index "sys_md_Ent#6a3870a0_tags_id_idx"
    on "sys_md_EntityType#6a3870a0_tags" (id);

create table "sys_md_Attribute#c8d9a252"
(
    id varchar(255) not null
        constraint "sys_md_Attribute#c8d9a252_id_pkey"
            primary key,
    name varchar(255) not null,
    entity varchar(255) not null
        constraint "sys_md_Attribute#c8d9a252_entity_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    "sequenceNr" integer not null,
    type varchar(255) not null
        constraint "sys_md_Attribute#c8d9a252_type_chk"
            check ((type)::text = ANY ((ARRAY['bool'::character varying, 'categorical'::character varying, 'categoricalmref'::character varying, 'compound'::character varying, 'date'::character varying, 'datetime'::character varying, 'decimal'::character varying, 'email'::character varying, 'enum'::character varying, 'file'::character varying, 'html'::character varying, 'hyperlink'::character varying, 'int'::character varying, 'long'::character varying, 'mref'::character varying, 'onetomany'::character varying, 'script'::character varying, 'string'::character varying, 'text'::character varying, 'xref'::character varying])::text[])),
    "isIdAttribute" boolean,
    "isLabelAttribute" boolean,
    "lookupAttributeIndex" integer,
    parent varchar(255)
        constraint "sys_md_Attribute#c8d9a252_parent_fkey"
            references "sys_md_Attribute#c8d9a252"
            deferrable initially deferred,
    "refEntityType" varchar(255)
        constraint "sys_md_Attribute#c8d9a252_refEntityType_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    "isCascadeDelete" boolean,
    "mappedBy" varchar(255)
        constraint "sys_md_Attribute#c8d9a252_mappedBy_fkey"
            references "sys_md_Attribute#c8d9a252"
            deferrable initially deferred,
    "orderBy" varchar(255),
    expression varchar(255),
    "isNullable" boolean not null,
    "isAuto" boolean not null,
    "isVisible" boolean not null,
    label varchar(255),
    description text,
    "isAggregatable" boolean not null,
    "enumOptions" text,
    "rangeMin" bigint,
    "rangeMax" bigint,
    "isReadOnly" boolean not null,
    "isUnique" boolean not null,
    "nullableExpression" text,
    "visibleExpression" text,
    "validationExpression" text,
    "defaultValue" text,
    "labelEn" varchar(255),
    "descriptionEn" text,
    "labelNl" varchar(255),
    "descriptionNl" text,
    "labelDe" varchar(255),
    "descriptionDe" text,
    "labelEs" varchar(255),
    "descriptionEs" text,
    "labelIt" varchar(255),
    "descriptionIt" text,
    "labelPt" varchar(255),
    "descriptionPt" text,
    "labelFr" varchar(255),
    "descriptionFr" text,
    "labelXx" varchar(255),
    "descriptionXx" text
);

create table "sys_md_Attribute#c8d9a252_tags"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_md_Attribute#c8d9a252_tags_id_fkey"
            references "sys_md_Attribute#c8d9a252"
            on delete cascade
            deferrable initially deferred,
    tags varchar(255) not null
        constraint "sys_md_Attribute#c8d9a252_tags_tags_fkey"
            references "sys_md_Tag#c2d685da"
            deferrable initially deferred,
    constraint "sys_md_Attribute#c8d9a252_tags_id_tags_key"
        unique (id, tags),
    constraint "sys_md_Attribute#c8d9a252_tags_order_id_key"
        unique ("order", id)
);

create index "sys_md_Att#c8d9a252_tags_id_idx"
    on "sys_md_Attribute#c8d9a252_tags" (id);

create table "sys_idx_IndexActionGroup#dd7eea75"
(
    id varchar(255) not null
        constraint "sys_idx_IndexAction#dd7eea75_id_pkey"
            primary key,
    count integer not null
);

create table "sys_StaticContent#f1dd2665"
(
    key_ varchar(255) not null
        constraint "sys_StaticContent#f1dd2665_key__pkey"
            primary key,
    content text
);

create table "sys_scr_ScriptType#3e8906a6"
(
    name varchar(255) not null
        constraint "sys_scr_ScriptType#3e8906a6_name_pkey"
            primary key
);

create table "sys_L10nString#95a21e09"
(
    id varchar(255) not null
        constraint "sys_L10nString#95a21e09_id_pkey"
            primary key,
    msgid varchar(255) not null,
    namespace varchar(255) not null,
    description text,
    en text,
    nl text,
    de text,
    es text,
    it text,
    pt text,
    fr text,
    xx text
);

create table "sys_ont_Ontology#49a81949"
(
    id varchar(255) not null
        constraint "sys_ont_Ontology#49a81949_id_pkey"
            primary key,
    "ontologyIRI" varchar(255) not null,
    "ontologyName" varchar(255) not null
);

create table "sys_scr_ScriptParameter#88bc2dd2"
(
    name varchar(255) not null
        constraint "sys_scr_ScriptParam#88bc2dd2_name_pkey"
            primary key
);

create table "sys_ont_TermFrequency#aef76974"
(
    id varchar(255) not null
        constraint "sys_ont_TermFrequen#aef76974_id_pkey"
            primary key,
    term varchar(255) not null,
    frequency double precision not null,
    occurrence integer not null
);

create table "sys_Plugin#4fafb60a"
(
    id varchar(255) not null
        constraint "sys_Plugin#4fafb60a_id_pkey"
            primary key,
    label varchar(255) not null
        constraint "sys_Plugin#4fafb60a_label_key"
            unique,
    path varchar(255) not null
        constraint "sys_Plugin#4fafb60a_path_key"
            unique,
    description varchar(255)
);

create table "sys_ont_OntologyTermNodePath#adc5397c"
(
    id varchar(255) not null
        constraint "sys_ont_OntologyTer#adc5397c_id_pkey"
            primary key,
    "nodePath" text not null,
    root boolean not null
);

create table "sys_genomebrowser_GenomeBrowserAttributes#bf815a63"
(
    id varchar(255) not null
        constraint "sys_genomebrowser_G#bf815a63_id_pkey"
            primary key,
    "default" boolean not null,
    "order" integer
        constraint "sys_genomebrowser_Ge#bf815a63_order_key"
            unique,
    pos varchar(255) not null,
    chr varchar(255) not null,
    ref varchar(255),
    alt varchar(255),
    stop varchar(255)
);

create table "sys_ont_OntologyTermSynonym#ce764ed4"
(
    id varchar(255) not null
        constraint "sys_ont_OntologyTer#ce764ed4_id_pkey"
            primary key,
    "ontologyTermSynonym" text not null,
    "Score" double precision,
    "Combined_Score" double precision
);

create table "sys_dec_DynamicDecorator#8c3531bb"
(
    id varchar(255) not null
        constraint "sys_dec_DynamicDeco#8c3531bb_id_pkey"
            primary key,
    label varchar(255) not null,
    description varchar(255) not null,
    schema text
);

create table "sys_FileMeta#76d84794"
(
    id varchar(255) not null
        constraint "sys_FileMeta#76d84794_id_pkey"
            primary key,
    filename varchar(255) not null,
    "contentType" varchar(255),
    size bigint,
    url varchar(255) not null
        constraint "sys_FileMeta#76d84794_url_key"
            unique
);

create table "sys_ImportRun#5ed65642"
(
    id varchar(255) not null
        constraint "sys_ImportRun#5ed65642_id_pkey"
            primary key,
    "startDate" timestamp with time zone not null,
    "endDate" timestamp with time zone,
    username varchar(255) not null,
    status varchar(255) not null
        constraint "sys_ImportRun#5ed65642_status_chk"
            check ((status)::text = ANY ((ARRAY['RUNNING'::character varying, 'FINISHED'::character varying, 'FAILED'::character varying])::text[])),
    message text,
    progress integer not null,
    "importedEntities" text,
    notify boolean
);

create table "sys_beacons_BeaconOrganization#02fc7b88"
(
    id varchar(255) not null
        constraint "sys_beacons_BeaconO#02fc7b88_id_pkey"
            primary key,
    name varchar(255) not null,
    description text,
    address varchar(255),
    welcome_url varchar(255),
    contact_url varchar(255),
    logo_url varchar(255)
);

create table "sys_sec_VOGroup#2575d4a0"
(
    id varchar(255) not null
        constraint "sys_sec_VOGroup#2575d4a0_id_pkey"
            primary key,
    name varchar(255) not null
        constraint "sys_sec_VOGroup#2575d4a0_name_key"
            unique
);

create table "sys_sec_RecoveryCode#a3192a80"
(
    id varchar(255) not null
        constraint "sys_sec_RecoveryCod#a3192a80_id_pkey"
            primary key,
    "userId" varchar(255) not null,
    code varchar(255) not null
);

create table "sys_sec_oidc_OidcClient#3e7b1b4d"
(
    "registrationId" varchar(255) not null
        constraint "sys_sec_oidc_OidcCl#3e7b1b4d_registrationId_pkey"
            primary key,
    "clientId" varchar(255) not null
        constraint "sys_sec_oidc_OidcCli#3e7b1b4d_clientId_key"
            unique,
    "clientSecret" varchar(255) not null,
    "clientName" varchar(255) not null
        constraint "sys_sec_oidc_OidcCli#3e7b1b4d_clientName_key"
            unique,
    "issuerUri" varchar(255),
    "clientAuthenticationMethod" varchar(255),
    "authorizationGrantType" varchar(255)
        constraint "sys_sec_oidc_OidcCli#3e7b1b4d_authorizationGrantType_chk"
            check (("authorizationGrantType")::text = ANY ((ARRAY['authorization_code'::character varying, 'implicit'::character varying, 'refresh_token'::character varying])::text[])),
    scopes varchar(255),
    "authorizationUri" varchar(255),
    "tokenUri" varchar(255),
    "jwkSetUri" varchar(255),
    "userInfoUri" varchar(255),
    "claimsRolePath" varchar(255),
    "claimsVOGroupPath" varchar(255),
    "userNameAttributeName" varchar(255) not null,
    "emailAttributeName" varchar(255) not null
);

create table "sys_sec_User#953f6cde"
(
    id varchar(255) not null
        constraint "sys_sec_User#953f6cde_id_pkey"
            primary key,
    username varchar(255) not null
        constraint "sys_sec_User#953f6cde_username_key"
            unique,
    password_ varchar(255) not null,
    "activationCode" varchar(255),
    active boolean not null,
    superuser boolean not null,
    "FirstName" varchar(255),
    "MiddleNames" varchar(255),
    "LastName" varchar(255),
    "Title" varchar(255),
    "Affiliation" varchar(255),
    "Department" varchar(255),
    "Role" varchar(255),
    "Address" text,
    "Phone" varchar(255),
    "Email" varchar(255) not null
        constraint "sys_sec_User#953f6cde_Email_key"
            unique,
    "Fax" varchar(255),
    "tollFreePhone" varchar(255),
    "City" varchar(255),
    "Country" varchar(255),
    "changePassword" boolean not null,
    use2fa boolean,
    "languageCode" varchar(255)
        constraint "sys_sec_User#953f6cde_languageCode_chk"
            check (("languageCode")::text = ANY ((ARRAY['en'::character varying, 'nl'::character varying, 'de'::character varying, 'es'::character varying, 'it'::character varying, 'pt'::character varying, 'fr'::character varying, 'xx'::character varying])::text[])),
    "googleAccountId" varchar(255)
);

create table "sys_FreemarkerTemplate#1b0d9d12"
(
    id varchar(255) not null
        constraint "sys_FreemarkerTempl#1b0d9d12_id_pkey"
            primary key,
    "Name" varchar(255) not null
        constraint "sys_FreemarkerTempla#1b0d9d12_Name_key"
            unique,
    "Value" text not null
);

create table "sys_sec_UserSecret#f51533c3"
(
    id varchar(255) not null
        constraint "sys_sec_UserSecret#f51533c3_id_pkey"
            primary key,
    "userId" varchar(255) not null
        constraint "sys_sec_UserSecret#f51533c3_userId_key"
            unique,
    secret varchar(255) not null,
    last_failed_authentication timestamp with time zone,
    failed_login_attempts integer not null
);

create table "sys_ont_OntologyTermDynamicAnnotation#6454b1f8"
(
    id varchar(255) not null
        constraint "sys_ont_OntologyTer#6454b1f8_id_pkey"
            primary key,
    name varchar(255) not null,
    value varchar(255) not null,
    label varchar(255) not null
);

create table "sys_Language#ab857455"
(
    code varchar(255) not null
        constraint "sys_Language#ab857455_code_pkey"
            primary key,
    name varchar(255) not null,
    active boolean not null
);

create table "sys_negotiator_NegotiatorConfig#aced883a"
(
    id varchar(255) not null
        constraint "sys_negotiator_Nego#aced883a_id_pkey"
            primary key,
    negotiator_url varchar(255) not null,
    username varchar(255) not null,
    password varchar(255) not null
);

create table "sys_App#1723bf4f"
(
    id varchar(255) not null
        constraint "sys_App#1723bf4f_id_pkey"
            primary key,
    label varchar(255) not null,
    description text,
    "isActive" boolean not null,
    "appVersion" varchar(255),
    "apiDependency" varchar(255),
    "templateContent" text not null,
    "resourceFolder" varchar(255) not null,
    name varchar(255) not null
        constraint "sys_App#1723bf4f_name_key"
            unique,
    "appConfig" text,
    "includeMenuAndFooter" boolean not null
);

create table "sys_map_AttributeMapping#fdffac26"
(
    identifier varchar(255) not null
        constraint "sys_map_AttributeMa#fdffac26_identifier_pkey"
            primary key,
    "targetAttribute" varchar(255) not null,
    "sourceAttributes" text,
    algorithm text,
    "algorithmState" varchar(255)
        constraint "sys_map_AttributeMap#fdffac26_algorithmState_chk"
            check (("algorithmState")::text = ANY ((ARRAY['CURATED'::character varying, 'GENERATED_HIGH'::character varying, 'GENERATED_LOW'::character varying, 'DISCUSS'::character varying, 'MISSING_TARGET'::character varying])::text[]))
);

create table "sys_sec_oidc_OidcUserMapping#72060861"
(
    id varchar(255) not null
        constraint "sys_sec_oidc_OidcUs#72060861_id_pkey"
            primary key,
    label varchar(255) not null
        constraint "sys_sec_oidc_OidcUse#72060861_label_key"
            unique,
    "oidcClient" varchar(255) not null
        constraint "sys_sec_oidc_OidcUs#72060861_oidcClient_fkey"
            references "sys_sec_oidc_OidcClient#3e7b1b4d"
            deferrable initially deferred,
    "oidcUsername" varchar(255) not null,
    "user" varchar(255) not null
        constraint "sys_sec_oidc_OidcUs#72060861_user_fkey"
            references "sys_sec_User#953f6cde"
            deferrable initially deferred
);

create table "sys_job_SortaJobExecution#3df661b2"
(
    name varchar(255) not null,
    "resultEntity" varchar(255) not null,
    "sourceEntity" varchar(255) not null,
    "ontologyIri" varchar(255) not null,
    "deleteUrl" varchar(255) not null,
    "Threshold" double precision not null,
    identifier varchar(255) not null
        constraint "sys_job_SortaJobExe#3df661b2_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_SortaJobExec#3df661b2_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_ScriptJobExecution#26f219e1"
(
    name varchar(255) not null,
    parameters text,
    identifier varchar(255) not null
        constraint "sys_job_ScriptJobEx#26f219e1_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_ScriptJobExe#26f219e1_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_ResourceDownloadJobExecution#3d0dbc70"
(
    resources text,
    identifier varchar(255) not null
        constraint "sys_job_ResourceDow#3d0dbc70_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_ResourceDown#3d0dbc70_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_set_OpenCpuSettings#0ec0e4e8"
(
    scheme varchar(255) not null,
    host varchar(255) not null,
    port integer not null,
    "rootPath" varchar(255) not null,
    id varchar(255) not null
        constraint "sys_set_OpenCpuSett#0ec0e4e8_id_pkey"
            primary key
);

create table "sys_dec_DecoratorParameters#0c01537a"
(
    id varchar(255) not null
        constraint "sys_dec_DecoratorPa#0c01537a_id_pkey"
            primary key,
    decorator varchar(255) not null
        constraint "sys_dec_DecoratorPa#0c01537a_decorator_fkey"
            references "sys_dec_DynamicDecorator#8c3531bb"
            deferrable initially deferred,
    parameters text
);

create table "sys_set_dataexplorer#76c80fb0"
(
    searchbox boolean not null,
    item_select_panel boolean not null,
    launch_wizard boolean not null,
    header_abbreviate integer not null,
    show_navigator_link boolean,
    mod_aggregates boolean not null,
    agg_distinct boolean not null,
    agg_distinct_overrides text,
    mod_data boolean not null,
    gb_init_browser_links text not null,
    gb_init_coord_system text not null,
    gb_init_location text not null,
    gb_init_sources text not null,
    gb_init_highlight_region boolean not null,
    data_genome_browser boolean not null,
    use_vue_data_row_edit boolean not null,
    mod_reports boolean not null,
    mod_standalone_reports boolean,
    reports_entities text,
    id varchar(255) not null
        constraint "sys_set_dataexplore#76c80fb0_id_pkey"
            primary key
);

create table "sys_sec_PasswordResetToken#a04705dd"
(
    id varchar(255) not null
        constraint "sys_sec_PasswordRes#a04705dd_id_pkey"
            primary key,
    token varchar(255) not null
        constraint "sys_sec_PasswordRese#a04705dd_token_key"
            unique,
    "user" varchar(255) not null
        constraint "sys_sec_PasswordRese#a04705dd_user_key"
            unique
        constraint "sys_sec_PasswordRes#a04705dd_user_fkey"
            references "sys_sec_User#953f6cde"
            deferrable initially deferred,
    "expirationDate" timestamp with time zone not null
);

create table "sys_ont_OntologyTermHit#9ce87f6a"
(
    id varchar(255) not null
        constraint "sys_ont_OntologyTer#9ce87f6a_id_pkey"
            primary key,
    "Score" double precision,
    "Combined_Score" double precision,
    "ontologyTermIRI" varchar(255) not null,
    "ontologyTermName" text not null,
    ontology varchar(255) not null
        constraint "sys_ont_OntologyTer#9ce87f6a_ontology_fkey"
            references "sys_ont_Ontology#49a81949"
            deferrable initially deferred
);

create table "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermSynonym"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermSynonym_id_fkey"
            references "sys_ont_OntologyTermHit#9ce87f6a"
            on delete cascade
            deferrable initially deferred,
    "ontologyTermSynonym" varchar(255) not null
        constraint "sys_ont_OntologyTermHi#9ce87f6a_ontolo_ontologyTermSynonym_fkey"
            references "sys_ont_OntologyTermSynonym#ce764ed4"
            deferrable initially deferred,
    constraint "sys_ont_OntologyTermHi#9ce87f6a_onto_id_ontologyTermSynonym_key"
        unique (id, "ontologyTermSynonym"),
    constraint "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermSynony_order_id_key"
        unique ("order", id)
);

create index "sys_ont_On#9ce87f6a_ontologyTermSynonym_id_idx"
    on "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermSynonym" (id);

create table "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermDynamicAnnotation"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermDynamicAnno_id_fkey"
            references "sys_ont_OntologyTermHit#9ce87f6a"
            on delete cascade
            deferrable initially deferred,
    "ontologyTermDynamicAnnotation" varchar(255) not null
        constraint "sys_ont_OntologyTermHi#9ce87f_ontologyTermDynamicAnnotatio_fkey"
            references "sys_ont_OntologyTermDynamicAnnotation#6454b1f8"
            deferrable initially deferred,
    constraint "sys_ont_OntologyTermHi#9ce87f_id_ontologyTermDynamicAnnotat_key"
        unique (id, "ontologyTermDynamicAnnotation"),
    constraint "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermDynami_order_id_key"
        unique ("order", id)
);

create index "sys_ont_On#9ce87f6a_ontologyTe#d535cbb2_id_idx"
    on "sys_ont_OntologyTermHi#9ce87f6a_ontologyTermDynamicAnnotation" (id);

create table "sys_ont_OntologyTermHi#9ce87f6a_nodePath"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_ont_OntologyTermHi#9ce87f6a_nodePath_id_fkey"
            references "sys_ont_OntologyTermHit#9ce87f6a"
            on delete cascade
            deferrable initially deferred,
    "nodePath" varchar(255) not null
        constraint "sys_ont_OntologyTermHi#9ce87f6a_nodePath_nodePath_fkey"
            references "sys_ont_OntologyTermNodePath#adc5397c"
            deferrable initially deferred,
    constraint "sys_ont_OntologyTermHi#9ce87f6a_nodePath_id_nodePath_key"
        unique (id, "nodePath"),
    constraint "sys_ont_OntologyTermHi#9ce87f6a_nodePath_order_id_key"
        unique ("order", id)
);

create index "sys_ont_On#9ce87f6a_nodePath_id_idx"
    on "sys_ont_OntologyTermHi#9ce87f6a_nodePath" (id);

create table "sys_sec_Token#2dc001d0"
(
    id varchar(255) not null
        constraint "sys_sec_Token#2dc001d0_id_pkey"
            primary key,
    "User" varchar(255) not null
        constraint "sys_sec_Token#2dc001d0_User_fkey"
            references "sys_sec_User#953f6cde"
            deferrable initially deferred,
    token varchar(255) not null
        constraint "sys_sec_Token#2dc001d0_token_key"
            unique,
    "expirationDate" timestamp with time zone,
    "creationDate" timestamp with time zone not null,
    description text
);

create table "sys_idx_IndexAction#43bbc99b"
(
    id varchar(255) not null
        constraint "sys_idx_IndexAction#43bbc99b_id_pkey"
            primary key,
    "creationDateTime" timestamp with time zone not null,
    "indexActionGroup" varchar(255)
        constraint "sys_idx_IndexAction#43bbc99b_indexActionGroup_fkey"
            references "sys_idx_IndexActionGroup#dd7eea75"
            deferrable initially deferred,
    "actionOrder" integer not null,
    "entityTypeId" varchar(255) not null,
    "entityId" text,
    "indexStatus" varchar(255) not null
        constraint "sys_idx_IndexAction#43bbc99b_indexStatus_chk"
            check (("indexStatus")::text = ANY ((ARRAY['FINISHED'::character varying, 'CANCELED'::character varying, 'FAILED'::character varying, 'STARTED'::character varying, 'PENDING'::character varying])::text[]))
);

create table "sys_set_auth#98c4c015"
(
    signup boolean not null,
    signup_moderation boolean not null,
    sign_in_2fa varchar(255) not null
        constraint "sys_set_auth#98c4c015_sign_in_2fa_chk"
            check ((sign_in_2fa)::text = ANY ((ARRAY['Disabled'::character varying, 'Enabled'::character varying, 'Enforced'::character varying])::text[])),
    id varchar(255) not null
        constraint "sys_set_auth#98c4c015_id_pkey"
            primary key
);

create table "sys_set_auth#98c4c015_oidcClients"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_set_auth#98c4c015_oidcClients_id_fkey"
            references "sys_set_auth#98c4c015"
            on delete cascade
            deferrable initially deferred,
    "oidcClients" varchar(255) not null
        constraint "sys_set_auth#98c4c015_oidcClients_oidcClients_fkey"
            references "sys_sec_oidc_OidcClient#3e7b1b4d"
            deferrable initially deferred,
    constraint "sys_set_auth#98c4c015_oidcClients_id_oidcClients_key"
        unique (id, "oidcClients"),
    constraint "sys_set_auth#98c4c015_oidcClients_order_id_key"
        unique ("order", id)
);

create index "sys_set_au#98c4c015_oidcClients_id_idx"
    on "sys_set_auth#98c4c015_oidcClients" (id);

create table "sys_job_IndexJobExecution#1d1bc397"
(
    "indexActionJobID" varchar(255) not null,
    identifier varchar(255) not null
        constraint "sys_job_IndexJobExe#1d1bc397_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_IndexJobExec#1d1bc397_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_set_app#4f91996f"
(
    title varchar(255) not null,
    logo_href_navbar varchar(255),
    logo_href_top varchar(255),
    "logoTopMaxHeight" integer not null,
    footer text,
    molgenis_menu text,
    language_code varchar(255) not null,
    legacy_theme_url varchar(255) not null,
    theme_url varchar(255) not null,
    css_href varchar(255),
    aggregate_threshold integer,
    custom_javascript text,
    ga_privacy_friendly boolean not null,
    ga_tracking_id varchar(255),
    ga_acc_privacy_friendly boolean not null,
    ga_tracking_id_mgs varchar(255),
    ga_acc_privacy_friendly_mgs boolean not null,
    tracking_code_footer text,
    recaptcha_private_key varchar(255),
    recaptcha_public_key varchar(255),
    recaptcha_is_enabled boolean not null,
    recaptcha_verify_uri varchar(255) not null,
    recaptcha_bot_threshold double precision not null,
    id varchar(255) not null
        constraint "sys_set_app#4f91996f_id_pkey"
            primary key
);

create table "sys_ont_OntologyTerm#f0034aa0"
(
    id varchar(255) not null
        constraint "sys_ont_OntologyTer#f0034aa0_id_pkey"
            primary key,
    "ontologyTermIRI" varchar(255) not null,
    "ontologyTermName" text not null,
    ontology varchar(255) not null
        constraint "sys_ont_OntologyTer#f0034aa0_ontology_fkey"
            references "sys_ont_Ontology#49a81949"
            deferrable initially deferred
);

create table "sys_ont_OntologyTerm#f0034aa0_ontologyTermSynonym"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_ont_OntologyTerm#f0034aa0_ontologyTermSynonym_id_fkey"
            references "sys_ont_OntologyTerm#f0034aa0"
            on delete cascade
            deferrable initially deferred,
    "ontologyTermSynonym" varchar(255) not null
        constraint "sys_ont_OntologyTerm#f0034aa0_ontology_ontologyTermSynonym_fkey"
            references "sys_ont_OntologyTermSynonym#ce764ed4"
            deferrable initially deferred,
    constraint "sys_ont_OntologyTerm#f0034aa0_ontolo_id_ontologyTermSynonym_key"
        unique (id, "ontologyTermSynonym"),
    constraint "sys_ont_OntologyTerm#f0034aa0_ontologyTermSynonym_order_id_key"
        unique ("order", id)
);

create index "sys_ont_On#f0034aa0_ontologyTermSynonym_id_idx"
    on "sys_ont_OntologyTerm#f0034aa0_ontologyTermSynonym" (id);

create table "sys_ont_OntologyTerm#f0034aa0_ontologyTermDynamicAnnotation"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_ont_OntologyTerm#f0034aa0_ontologyTermDynamicAnnota_id_fkey"
            references "sys_ont_OntologyTerm#f0034aa0"
            on delete cascade
            deferrable initially deferred,
    "ontologyTermDynamicAnnotation" varchar(255) not null
        constraint "sys_ont_OntologyTerm#f0034aa0_ontologyTermDynamicAnnotatio_fkey"
            references "sys_ont_OntologyTermDynamicAnnotation#6454b1f8"
            deferrable initially deferred,
    constraint "sys_ont_OntologyTerm#f0034aa0_id_ontologyTermDynamicAnnotat_key"
        unique (id, "ontologyTermDynamicAnnotation"),
    constraint "sys_ont_OntologyTerm#f0034aa0_ontologyTermDynamicA_order_id_key"
        unique ("order", id)
);

create index "sys_ont_On#f0034aa0_ontologyTe#cc6dfae0_id_idx"
    on "sys_ont_OntologyTerm#f0034aa0_ontologyTermDynamicAnnotation" (id);

create table "sys_ont_OntologyTerm#f0034aa0_nodePath"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_ont_OntologyTerm#f0034aa0_nodePath_id_fkey"
            references "sys_ont_OntologyTerm#f0034aa0"
            on delete cascade
            deferrable initially deferred,
    "nodePath" varchar(255) not null
        constraint "sys_ont_OntologyTerm#f0034aa0_nodePath_nodePath_fkey"
            references "sys_ont_OntologyTermNodePath#adc5397c"
            deferrable initially deferred,
    constraint "sys_ont_OntologyTerm#f0034aa0_nodePath_id_nodePath_key"
        unique (id, "nodePath"),
    constraint "sys_ont_OntologyTerm#f0034aa0_nodePath_order_id_key"
        unique ("order", id)
);

create index "sys_ont_On#f0034aa0_nodePath_id_idx"
    on "sys_ont_OntologyTerm#f0034aa0_nodePath" (id);

create table "sys_job_MetadataUpsertJobExecution#0b27aef6"
(
    action varchar(255) not null
        constraint "sys_job_MetadataUpse#0b27aef6_action_chk"
            check ((action)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying])::text[])),
    "entityTypeData" text not null,
    identifier varchar(255) not null
        constraint "sys_job_MetadataUps#0b27aef6_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_MetadataUpse#0b27aef6_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_FileIngestJobExecution#091fdb52"
(
    file varchar(255)
        constraint "sys_job_FileIngestJ#091fdb52_file_fkey"
            references "sys_FileMeta#76d84794"
            deferrable initially deferred,
    url varchar(255) not null,
    loader varchar(255) not null
        constraint "sys_job_FileIngestJo#091fdb52_loader_chk"
            check ((loader)::text = 'CSV'::text),
    "targetEntityId" varchar(255) not null,
    identifier varchar(255) not null
        constraint "sys_job_FileIngestJ#091fdb52_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_FileIngestJo#091fdb52_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_OneClickImportJobExecution#c6636b72"
(
    file varchar(255) not null,
    "entityTypes" text,
    package varchar(255),
    identifier varchar(255) not null
        constraint "sys_job_OneClickImp#c6636b72_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_OneClickImpo#c6636b72_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_ResourceDeleteJobExecution#5d6022b0"
(
    resources text,
    identifier varchar(255) not null
        constraint "sys_job_ResourceDel#5d6022b0_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_ResourceDele#5d6022b0_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_AmazonBucketJobExecution#f9fb2a28"
(
    bucket varchar(255) not null,
    key varchar(255) not null,
    expression boolean not null,
    "accessKey" varchar(255) not null,
    "secretKey" varchar(255) not null,
    region varchar(255) not null,
    "targetEntityId" varchar(255),
    file varchar(255)
        constraint "sys_job_AmazonBucke#f9fb2a28_file_fkey"
            references "sys_FileMeta#76d84794"
            deferrable initially deferred,
    extension varchar(255),
    identifier varchar(255) not null
        constraint "sys_job_AmazonBucke#f9fb2a28_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_AmazonBucket#f9fb2a28_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_scr_Script#354cd12b"
(
    name varchar(255) not null
        constraint "sys_scr_Script#354cd12b_name_pkey"
            primary key,
    type varchar(255) not null
        constraint "sys_scr_Script#354cd12b_type_fkey"
            references "sys_scr_ScriptType#3e8906a6"
            deferrable initially deferred,
    content text not null,
    "generateToken" boolean,
    "resultFileExtension" varchar(255)
);

create table "sys_scr_Script#354cd12b_parameters"
(
    "order" integer,
    name varchar(255) not null
        constraint "sys_scr_Script#354cd12b_parameters_name_fkey"
            references "sys_scr_Script#354cd12b"
            on delete cascade
            deferrable initially deferred,
    parameters varchar(255) not null
        constraint "sys_scr_Script#354cd12b_parameters_parameters_fkey"
            references "sys_scr_ScriptParameter#88bc2dd2"
            deferrable initially deferred,
    constraint "sys_scr_Script#354cd12b_parameters_name_parameters_key"
        unique (name, parameters),
    constraint "sys_scr_Script#354cd12b_parameters_order_name_key"
        unique ("order", name)
);

create index "sys_scr_Sc#354cd12b_parameters_name_idx"
    on "sys_scr_Script#354cd12b_parameters" (name);

create table "sys_job_MetadataDeleteJobExecution#4ed53e2d"
(
    ids text not null,
    identifier varchar(255) not null
        constraint "sys_job_MetadataDel#4ed53e2d_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_MetadataDele#4ed53e2d_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_set_aud#77b13408"
(
    audit_system boolean not null,
    audit_data varchar(255) not null
        constraint "sys_set_aud#77b13408_audit_data_chk"
            check ((audit_data)::text = ANY ((ARRAY['None'::character varying, 'Tagged'::character varying, 'All'::character varying])::text[])),
    id varchar(255) not null
        constraint "sys_set_aud#77b13408_id_pkey"
            primary key
);

create table "sys_map_EntityMapping#4c287e1a"
(
    identifier varchar(255) not null
        constraint "sys_map_EntityMappi#4c287e1a_identifier_pkey"
            primary key,
    "sourceEntityType" varchar(255),
    "targetEntityType" varchar(255)
);

create table "sys_map_EntityMapping#4c287e1a_attributeMappings"
(
    "order" integer,
    identifier varchar(255) not null
        constraint "sys_map_EntityMapping#4c287e1a_attributeMapping_identifier_fkey"
            references "sys_map_EntityMapping#4c287e1a"
            on delete cascade
            deferrable initially deferred,
    "attributeMappings" varchar(255) not null
        constraint "sys_map_EntityMapping#4c287e1a_attribute_attributeMappings_fkey"
            references "sys_map_AttributeMapping#fdffac26"
            deferrable initially deferred,
    constraint "sys_map_EntityMapping#4c287e1a_identifier_attributeMappings_key"
        unique (identifier, "attributeMappings"),
    constraint "sys_map_EntityMapping#4c287e1a_attributeMa_order_identifier_key"
        unique ("order", identifier)
);

create index "sys_map_En#4c287e1a_attributeMappings_identifier_idx"
    on "sys_map_EntityMapping#4c287e1a_attributeMappings" (identifier);

create table "sys_job_MappingJobExecution#9d59355f"
(
    "mappingProjectId" varchar(255) not null,
    "targetEntityTypeId" varchar(255) not null,
    "addSourceAttribute" boolean,
    "packageId" varchar(255),
    label varchar(255),
    identifier varchar(255) not null
        constraint "sys_job_MappingJobE#9d59355f_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_MappingJobEx#9d59355f_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_job_ResourceCopyJobExecution#79e3e597"
(
    resources text,
    "targetPackage" varchar(255),
    identifier varchar(255) not null
        constraint "sys_job_ResourceCop#79e3e597_identifier_pkey"
            primary key,
    "user" varchar(255),
    status varchar(255) not null
        constraint "sys_job_ResourceCopy#79e3e597_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'CANCELING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])),
    type varchar(255) not null,
    "submissionDate" timestamp with time zone not null,
    "startDate" timestamp with time zone,
    "endDate" timestamp with time zone,
    "progressInt" integer,
    "progressMax" integer,
    "progressMessage" varchar(255),
    log text,
    "resultUrl" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    "scheduledJobId" varchar(255)
);

create table "sys_set_MailSettings#6daa44ed"
(
    host varchar(255) not null,
    port integer not null,
    protocol varchar(255) not null,
    username varchar(255),
    password varchar(255),
    "defaultEncoding" varchar(255) not null,
    "startTlsEnabled" varchar(255),
    "waitQuit" varchar(255),
    auth varchar(255),
    "fromAddress" varchar(255),
    "testConnection" boolean not null,
    id varchar(255) not null
        constraint "sys_set_MailSetting#6daa44ed_id_pkey"
            primary key
);

create table "sys_mail_JavaMailProperty#ddcd42a8"
(
    "mailSettings" varchar(255) not null
        constraint "sys_mail_JavaMailPr#ddcd42a8_mailSettings_fkey"
            references "sys_set_MailSettings#6daa44ed"
            deferrable initially deferred,
    key varchar(255) not null
        constraint "sys_mail_JavaMailPr#ddcd42a8_key_pkey"
            primary key,
    value varchar(255)
);

create table "sys_dec_DecoratorConfiguration#e9347da9"
(
    id varchar(255) not null
        constraint "sys_dec_DecoratorCo#e9347da9_id_pkey"
            primary key,
    "entityTypeId" varchar(255) not null
        constraint "sys_dec_DecoratorCon#e9347da9_entityTypeId_key"
            unique
);

create table "sys_dec_DecoratorConfi#e9347da9_parameters"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_dec_DecoratorConfi#e9347da9_parameters_id_fkey"
            references "sys_dec_DecoratorConfiguration#e9347da9"
            on delete cascade
            deferrable initially deferred,
    parameters varchar(255) not null
        constraint "sys_dec_DecoratorConfi#e9347da9_parameters_parameters_fkey"
            references "sys_dec_DecoratorParameters#0c01537a"
            deferrable initially deferred,
    constraint "sys_dec_DecoratorConfi#e9347da9_parameters_id_parameters_key"
        unique (id, parameters),
    constraint "sys_dec_DecoratorConfi#e9347da9_parameters_order_id_key"
        unique ("order", id)
);

create index "sys_dec_De#e9347da9_parameters_id_idx"
    on "sys_dec_DecoratorConfi#e9347da9_parameters" (id);

create table "sys_map_MappingTarget#8e135dd7"
(
    identifier varchar(255) not null
        constraint "sys_map_MappingTarg#8e135dd7_identifier_pkey"
            primary key,
    target varchar(255) not null
);

create table "sys_map_MappingTarget#8e135dd7_entityMappings"
(
    "order" integer,
    identifier varchar(255) not null
        constraint "sys_map_MappingTarget#8e135dd7_entityMappings_identifier_fkey"
            references "sys_map_MappingTarget#8e135dd7"
            on delete cascade
            deferrable initially deferred,
    "entityMappings" varchar(255) not null
        constraint "sys_map_MappingTarget#8e135dd7_entityMappin_entityMappings_fkey"
            references "sys_map_EntityMapping#4c287e1a"
            deferrable initially deferred,
    constraint "sys_map_MappingTarget#8e135dd7_en_identifier_entityMappings_key"
        unique (identifier, "entityMappings"),
    constraint "sys_map_MappingTarget#8e135dd7_entityMappi_order_identifier_key"
        unique ("order", identifier)
);

create index "sys_map_Ma#8e135dd7_entityMappings_identifier_idx"
    on "sys_map_MappingTarget#8e135dd7_entityMappings" (identifier);

create table "sys_sec_Group#d325f6e2"
(
    id varchar(255) not null
        constraint "sys_sec_Group#d325f6e2_id_pkey"
            primary key,
    name varchar(255) not null
        constraint "sys_sec_Group#d325f6e2_name_key"
            unique,
    label varchar(255) not null,
    "labelEn" varchar(255),
    "labelNl" varchar(255),
    "labelDe" varchar(255),
    "labelEs" varchar(255),
    "labelIt" varchar(255),
    "labelPt" varchar(255),
    "labelFr" varchar(255),
    "labelXx" varchar(255),
    description text,
    "descriptionEn" text,
    "descriptionNl" text,
    "descriptionDe" text,
    "descriptionEs" text,
    "descriptionIt" text,
    "descriptionPt" text,
    "descriptionFr" text,
    "descriptionXx" text,
    public boolean not null,
    "rootPackage" varchar(255) not null
        constraint "sys_sec_Group#d325f6e2_rootPackage_key"
            unique
        constraint "sys_sec_Group#d325f6e2_rootPackage_fkey"
            references "sys_md_Package#a6dc6fe7"
            deferrable initially deferred
);

create table "sys_map_MappingProject#c2f22991"
(
    identifier varchar(255) not null
        constraint "sys_map_MappingProj#c2f22991_identifier_pkey"
            primary key,
    name varchar(255) not null
);

create table "sys_map_MappingProject#c2f22991_mappingtargets"
(
    "order" integer,
    identifier varchar(255) not null
        constraint "sys_map_MappingProject#c2f22991_mappingtargets_identifier_fkey"
            references "sys_map_MappingProject#c2f22991"
            on delete cascade
            deferrable initially deferred,
    mappingtargets varchar(255) not null
        constraint "sys_map_MappingProject#c2f22991_mappingtarg_mappingtargets_fkey"
            references "sys_map_MappingTarget#8e135dd7"
            deferrable initially deferred,
    constraint "sys_map_MappingProject#c2f22991_m_identifier_mappingtargets_key"
        unique (identifier, mappingtargets),
    constraint "sys_map_MappingProject#c2f22991_mappingtar_order_identifier_key"
        unique ("order", identifier)
);

create index "sys_map_Ma#c2f22991_mappingtargets_identifier_idx"
    on "sys_map_MappingProject#c2f22991_mappingtargets" (identifier);

create table "sys_job_ScheduledJobType#d68c491a"
(
    name varchar(255) not null
        constraint "sys_job_ScheduledJo#d68c491a_name_pkey"
            primary key,
    label varchar(255) not null,
    description text,
    "jobExecutionType" varchar(255) not null
        constraint "sys_job_ScheduledJo#d68c491a_jobExecutionType_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    schema text
);

create table "sys_sec_Role#b6639604"
(
    id varchar(255) not null
        constraint "sys_sec_Role#b6639604_id_pkey"
            primary key,
    name varchar(255) not null
        constraint "sys_sec_Role#b6639604_name_key"
            unique,
    label varchar(255) not null,
    "labelEn" varchar(255),
    "labelNl" varchar(255),
    "labelDe" varchar(255),
    "labelEs" varchar(255),
    "labelIt" varchar(255),
    "labelPt" varchar(255),
    "labelFr" varchar(255),
    "labelXx" varchar(255),
    description varchar(255),
    "descriptionEn" text,
    "descriptionNl" text,
    "descriptionDe" text,
    "descriptionEs" text,
    "descriptionIt" text,
    "descriptionPt" text,
    "descriptionFr" text,
    "descriptionXx" text,
    "group" varchar(255)
        constraint "sys_sec_Role#b6639604_group_fkey"
            references "sys_sec_Group#d325f6e2"
            deferrable initially deferred
);

create table "sys_sec_Role#b6639604_includes"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_sec_Role#b6639604_includes_id_fkey"
            references "sys_sec_Role#b6639604"
            on delete cascade
            deferrable initially deferred,
    includes varchar(255) not null
        constraint "sys_sec_Role#b6639604_includes_includes_fkey"
            references "sys_sec_Role#b6639604"
            deferrable initially deferred,
    constraint "sys_sec_Role#b6639604_includes_id_includes_key"
        unique (id, includes),
    constraint "sys_sec_Role#b6639604_includes_order_id_key"
        unique ("order", id)
);

create index "sys_sec_Ro#b6639604_includes_id_idx"
    on "sys_sec_Role#b6639604_includes" (id);

create table "sys_beacons_BeaconDataset#17b7de29"
(
    id varchar(255) not null
        constraint "sys_beacons_BeaconD#17b7de29_id_pkey"
            primary key,
    label varchar(255) not null,
    description varchar(255),
    data_set_entity_type varchar(255) not null
        constraint "sys_beacons_BeaconD#17b7de29_data_set_entity_type_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    genome_browser_attributes varchar(255) not null
        constraint "sys_beacons_BeaconD#17b7de29_genome_browser_attributes_fkey"
            references "sys_genomebrowser_GenomeBrowserAttributes#bf815a63"
            deferrable initially deferred
);

create table "sys_ts_DataExplorerEntitySettings#a7f151bf"
(
    id varchar(255) not null
        constraint "sys_ts_DataExplorer#a7f151bf_id_pkey"
            primary key,
    "table" varchar(255) not null
        constraint "sys_ts_DataExplorerE#a7f151bf_table_key"
            unique
        constraint "sys_ts_DataExplorer#a7f151bf_table_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    card_template text,
    detail_template text,
    shop boolean,
    template_attrs varchar(255),
    collapse_limit varchar(255),
    default_filters varchar(255)
);

create table "sys_job_ScheduledJob#d2aed7e4"
(
    id varchar(255) not null
        constraint "sys_job_ScheduledJo#d2aed7e4_id_pkey"
            primary key,
    name varchar(255) not null
        constraint "sys_job_ScheduledJob#d2aed7e4_name_key"
            unique,
    description text,
    "cronExpression" varchar(255) not null,
    active boolean not null,
    "user" varchar(255),
    "failureEmail" varchar(255),
    "successEmail" varchar(255),
    type varchar(255) not null
        constraint "sys_job_ScheduledJo#d2aed7e4_type_fkey"
            references "sys_job_ScheduledJobType#d68c491a"
            deferrable initially deferred,
    parameters text not null
);

create table "sys_sec_MembershipInvitation#27f7958b"
(
    id varchar(255) not null
        constraint "sys_sec_MembershipI#27f7958b_id_pkey"
            primary key,
    token varchar(255) not null,
    email varchar(255) not null,
    "from" timestamp with time zone not null,
    "to" timestamp with time zone,
    role varchar(255) not null
        constraint "sys_sec_MembershipI#27f7958b_role_fkey"
            references "sys_sec_Role#b6639604"
            deferrable initially deferred,
    "invitedBy" varchar(255) not null
        constraint "sys_sec_MembershipI#27f7958b_invitedBy_fkey"
            references "sys_sec_User#953f6cde"
            deferrable initially deferred,
    issued timestamp with time zone not null,
    "lastUpdate" timestamp with time zone not null,
    "invitationText" text,
    "declineReason" text,
    status varchar(255) not null
        constraint "sys_sec_MembershipIn#27f7958b_status_chk"
            check ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REVOKED'::character varying, 'EXPIRED'::character varying, 'DECLINED'::character varying])::text[]))
);

create table "sys_sec_VOGroupRoleMembership#b5b67558"
(
    id varchar(255) not null
        constraint "sys_sec_VOGroupRole#b5b67558_id_pkey"
            primary key,
    "voGroup" varchar(255) not null
        constraint "sys_sec_VOGroupRole#b5b67558_voGroup_fkey"
            references "sys_sec_VOGroup#2575d4a0"
            deferrable initially deferred,
    role varchar(255) not null
        constraint "sys_sec_VOGroupRole#b5b67558_role_fkey"
            references "sys_sec_Role#b6639604"
            deferrable initially deferred,
    "from" timestamp with time zone not null,
    "to" timestamp with time zone
);

create table "sys_sec_RoleMembership#2f0e0432"
(
    id varchar(255) not null
        constraint "sys_sec_RoleMembers#2f0e0432_id_pkey"
            primary key,
    "user" varchar(255) not null
        constraint "sys_sec_RoleMembers#2f0e0432_user_fkey"
            references "sys_sec_User#953f6cde"
            deferrable initially deferred,
    role varchar(255) not null
        constraint "sys_sec_RoleMembers#2f0e0432_role_fkey"
            references "sys_sec_Role#b6639604"
            deferrable initially deferred,
    "from" timestamp with time zone not null,
    "to" timestamp with time zone
);

create table "sys_negotiator_NegotiatorEntityConfig#9a61747d"
(
    id varchar(255) not null
        constraint "sys_negotiator_Nego#9a61747d_id_pkey"
            primary key,
    entity varchar(255) not null
        constraint "sys_negotiator_Nego#9a61747d_entity_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    "negotiatorConfig" varchar(255) not null
        constraint "sys_negotiator_Nego#9a61747d_negotiatorConfig_fkey"
            references "sys_negotiator_NegotiatorConfig#aced883a"
            deferrable initially deferred,
    "collectionId" varchar(255) not null
        constraint "sys_negotiator_Nego#9a61747d_collectionId_fkey"
            references "sys_md_Attribute#c8d9a252"
            deferrable initially deferred,
    "biobankId" varchar(255) not null
        constraint "sys_negotiator_Nego#9a61747d_biobankId_fkey"
            references "sys_md_Attribute#c8d9a252"
            deferrable initially deferred,
    "enabledExpression" text
);

create table "sys_genomebrowser_GenomeBrowserSettings#294012a4"
(
    id varchar(255) not null
        constraint "sys_genomebrowser_G#294012a4_id_pkey"
            primary key,
    label varchar(255) not null,
    entity varchar(255) not null
        constraint "sys_genomebrowser_G#294012a4_entity_fkey"
            references "sys_md_EntityType#6a3870a0"
            deferrable initially deferred,
    genome_browser_attrs varchar(255) not null
        constraint "sys_genomebrowser_G#294012a4_genome_browser_attrs_fkey"
            references "sys_genomebrowser_GenomeBrowserAttributes#bf815a63"
            deferrable initially deferred,
    "labelAttr" varchar(255) not null
        constraint "sys_genomebrowser_G#294012a4_labelAttr_fkey"
            references "sys_md_Attribute#c8d9a252"
            deferrable initially deferred,
    track_type varchar(255) not null
        constraint "sys_genomebrowser_Ge#294012a4_track_type_chk"
            check ((track_type)::text = ANY ((ARRAY['VARIANT'::character varying, 'NUMERIC'::character varying, 'EXON'::character varying])::text[])),
    exon_key varchar(255),
    "scoreAttr" varchar(255),
    attrs varchar(255),
    molgenis_reference_tracks_mode varchar(255) not null
        constraint "sys_genomebrowser_Ge#294012a4_molgenis_reference_t#d9374088_chk"
            check ((molgenis_reference_tracks_mode)::text = ANY ((ARRAY['ALL'::character varying, 'CONFIGURED'::character varying, 'NONE'::character varying])::text[])),
    actions text,
    feature_info_plugin text
);

create table "sys_genomebrowser_Geno#294012a4_molgenis_reference_tracks"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_genomebrowser_Geno#294012a4_molgenis_reference_trac_id_fkey"
            references "sys_genomebrowser_GenomeBrowserSettings#294012a4"
            on delete cascade
            deferrable initially deferred,
    molgenis_reference_tracks varchar(255) not null
        constraint "sys_genomebrowser_Geno#294012a4__molgenis_reference_tracks_fkey"
            references "sys_genomebrowser_GenomeBrowserSettings#294012a4"
            deferrable initially deferred,
    constraint "sys_genomebrowser_Geno#294012a_id_molgenis_reference_tracks_key"
        unique (id, molgenis_reference_tracks),
    constraint "sys_genomebrowser_Geno#294012a4_molgenis_reference_order_id_key"
        unique ("order", id)
);

create index "sys_genome#294012a4_molgenis_r#c0f32d8f_id_idx"
    on "sys_genomebrowser_Geno#294012a4_molgenis_reference_tracks" (id);

create table "sys_beacons_Beacon#8e99cfb8"
(
    id varchar(255) not null
        constraint "sys_beacons_Beacon#8e99cfb8_id_pkey"
            primary key,
    name varchar(255) not null,
    api_version varchar(255) not null,
    beacon_organization varchar(255)
        constraint "sys_beacons_Beacon#8e99cfb8_beacon_organization_fkey"
            references "sys_beacons_BeaconOrganization#02fc7b88"
            deferrable initially deferred,
    description text,
    version varchar(255),
    welcome_url varchar(255)
);

create table "sys_beacons_Beacon#8e99cfb8_data_sets"
(
    "order" integer,
    id varchar(255) not null
        constraint "sys_beacons_Beacon#8e99cfb8_data_sets_id_fkey"
            references "sys_beacons_Beacon#8e99cfb8"
            on delete cascade
            deferrable initially deferred,
    data_sets varchar(255) not null
        constraint "sys_beacons_Beacon#8e99cfb8_data_sets_data_sets_fkey"
            references "sys_beacons_BeaconDataset#17b7de29"
            deferrable initially deferred,
    constraint "sys_beacons_Beacon#8e99cfb8_data_sets_id_data_sets_key"
        unique (id, data_sets),
    constraint "sys_beacons_Beacon#8e99cfb8_data_sets_order_id_key"
        unique ("order", id)
);

create index "sys_beacon#8e99cfb8_data_sets_id_idx"
    on "sys_beacons_Beacon#8e99cfb8_data_sets" (id);