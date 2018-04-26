<#--
Different style of reporting template, inspired by http://www.biomedcentral.com/content/supplementary/s12881-014-0134-1-s4.pdf
-->

<#-- Preprocessing, taken from previous template -->
<#assign severelateonset = ["AIP", "ALK", "APC", "AXIN2", "BAP1", "BMPR1A", "BRCA1", "CDH1", "CDK4", "CDKN2A", "CEBPA", "CHEK2", "CTHRC1", "CTNNA1", "DICER1", "EGFR", "FH", "FLCN", "GATA2", "KIT", "MAX", "MLH1", "MLH3", "MSH2", "MSH3", "MSH6", "MUTYH", "NF2", "PAX5", "PDGFRA", "PMS2", "PRKAR1A", "RAD51D", "STK11", "TMEM127", "TP53"]>

<#assign dom_high_candidate_genes = {}>
<#assign dom_mod_candidate_genes = {}>
<#assign rec_high_candidate_genes = {}>
<#assign rec_mod_candidate_genes = {}>
<#assign other_candidate_genes = {}>
<#assign all_candidates = {}><#-- all genes + counts per category -->

<#-- these need some post-processing -->
<#assign com_high_candidate_raw = {}>
<#assign com_mod_candidate_raw = {}>
<#assign excluded_mod_compound_raw = {}>
<#assign excluded_high_compound_raw = {}>

<#list datasetRepository as row>

    <#assign geneName = row.getString("INFO_ANN")?split("|")[3]>

<#-- INCLUDED_DOMINANT variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT_HIGHIMPACT">
        <#if dom_high_candidate_genes[geneName]??>
            <#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName: dom_high_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT">
        <#if dom_mod_candidate_genes[geneName]??>
            <#assign dom_mod_candidate_genes = dom_mod_candidate_genes + {geneName : dom_mod_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign dom_mod_candidate_genes = dom_mod_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

<#-- INCLUDED_RECESSIVE variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_HIGHIMPACT">
        <#if rec_high_candidate_genes[geneName]??>
            <#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName: rec_high_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE">
        <#if rec_mod_candidate_genes[geneName]??>
            <#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName: rec_mod_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

<#-- INCLUDED_RECESSIVE_COMPOUND variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT">
        <#if com_high_candidate_raw[geneName]??>
            <#assign com_high_candidate_raw = com_high_candidate_raw + {geneName: com_high_candidate_raw[geneName] + [row] } />
        <#else>
            <#assign com_high_candidate_raw = com_high_candidate_raw + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_COMPOUND">
        <#if com_mod_candidate_raw[geneName]??>
            <#assign com_mod_candidate_raw = com_mod_candidate_raw + {geneName: com_mod_candidate_raw[geneName] + [row] } />
        <#else>
            <#assign com_mod_candidate_raw = com_mod_candidate_raw + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

<#-- store EXCLUDED_FIRST_OF_COMPOUND / _HIGHIMPACT that will be retrieved once there is a INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT or INCLUDED_RECESSIVE_COMPOUND for this gene! -->
<#-- there should be only 1 such variant per compound candidate gene, or else error -->
    <#if row.getString("INFO_MONGENDISCAND") == "EXCLUDED_FIRST_OF_COMPOUND">
        <#if excluded_mod_compound_raw[geneName]??>
        ERROR: multiple EXCLUDED_FIRST_OF_COMPOUND variants for gene ${geneName} !!
        <#else>
            <#assign excluded_mod_compound_raw = excluded_mod_compound_raw + {geneName : [row] } />
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT">
        <#if excluded_high_compound_raw[geneName]??>
        ERROR: multiple EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT variants for gene ${geneName} !!
        <#else>
            <#assign excluded_high_compound_raw = excluded_high_compound_raw + {geneName : [row] } />
        </#if>
    </#if>

<#-- INCLUDED_OTHER variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_OTHER">
        <#if other_candidate_genes[geneName]??>
            <#assign other_candidate_genes = other_candidate_genes + {geneName: other_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign other_candidate_genes = other_candidate_genes + {geneName : [row] } />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

</#list>



<#-- POST PROCESSING OF COMPOUND RECESSIVE VARIANTS INTO HIGH AND MODERATE CANDIDATE GENES -->

<#assign com_high_candidate_genes = {}>
<#assign com_mod_candidate_genes = {}>

<#-- if already a candidate HIGH impact variant, always put under 'compound high', and copy over any MODERATE variants, and the EXCLUDED_FIRST_OF_COMPOUND variant (high or mod impact) -->
<#list com_high_candidate_raw?keys as gene>

<#-- always add candidate -->
    <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_raw[gene] } />
<#--assign all_candidate_genes = all_candidate_genes + [geneName]-->

<#-- also add any MODERATE variants to the candidate-->
    <#if com_mod_candidate_raw[gene]??>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + com_mod_candidate_raw[gene] } />
    </#if>

<#--include FIRST_OF_COMPOUND: either HIGH or MODERATE variant-->
    <#if excluded_high_compound_raw[gene]??>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_high_compound_raw[gene] } />
    <#else>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_mod_compound_raw[gene] } />
    </#if>
</#list>

<#-- if a candidate MODERATE impact variant, put in HIGH anyway when the EXCLUDED_FIRST_OF_COMPOUND has a HIGH impact.  -->
<#list com_mod_candidate_raw?keys as gene>

<#-- if FIRST_OF_COMPOUND for this gene was HIGH impact, add this variant to HIGH candidates plus the original MODERATE variants-->
    <#if excluded_high_compound_raw[gene]??>
        <#if com_high_candidate_genes[gene]??>
            <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_high_compound_raw[gene] } />
        <#else>
            <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : excluded_high_compound_raw[gene] } />
        </#if>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + com_mod_candidate_raw[gene] } />

    <#--FIRST_OF_COMPOUND was a MODERATE, and so is the rest! add them to MODERATE COMPOUND candidates -->
    <#else>
        <#if com_mod_candidate_genes[gene]??>
            <#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : com_mod_candidate_genes[gene] + excluded_mod_compound_raw[gene] } />
        <#else>
            <#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : excluded_mod_compound_raw[gene] } />
        </#if>
        <#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : com_mod_candidate_genes[gene] + com_mod_candidate_raw[gene]} />
    </#if>
</#list>


<#-- HTML -->

<table width="100%" style="background-color: lightgrey">
    <tr>
        <td>
            <h5>UNIVERSITARY MEDICAL CENTER GRONINGEN</h5>
            <h5>DEPARTMENT OF GENETICS</h5>
            Hanzeplein 1, 9700 RB Groningen, The Netherlands<br>
            Phone: +31 050 361 6161, URL: https://www.umcg.nl
        </td>
        <td>
            <img width="50%" height="50%" src="https://www.umcg.nl/_layouts/15/UmcgPortal/images/logo_umcg.png"/>
        </td>
        <td>
            <img width="50%" height="50%" src="http://www.rug.nl/_definition/shared/images/logo_en.png"/>
        </td>
    </tr>
</table>

<br><br>

<table style="font-weight: bold;white-space: nowrap;">
    <tr style="vertical-align: top;">
        <td style="background-color: blue;padding: 5px;">
            &nbsp;
        </td>
        <td style="color: grey;padding: 5px;">
            Name:<br>
            DOB:<br>
            Sex:<br>
            Ethnicity:<br>
            Indication of testing:<br>
            Test:
        </td>
        <td style="padding: 5px;">
            Doe, Jeffrey<br>
            12/34/5678<br>
            Male<br>
            Caucasian<br>
            5GPM<br>
            WES
        </td>
        <td style="color: grey;padding: 5px;">
            MRN:<br>
            Specimen:<br>
            Received:<br>
        </td>
        <td style="padding: 5px;">
            123456789<br>
            Blood, peripheral<br>
            12/34/5678
        </td>

        <td style="color: grey;padding: 5px;">
            Patient #:<br>
            DNA #:<br>
            Family #:<br>
            Referring physician:<br>
            Referring facility:
        </td>
        <td style="padding: 5px;">
        ${datasetRepository.getName()}<br>
            98765<br>
            ZXY4562<br>
            Doe, Jane<br>
            NICU
        </td>
    </tr>
</table>

<h3>GENOME REPORT</h3>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: teal;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="teal">RESULT SUMMARY</font></h4>
            Sequencing of this individual’s genome was performed and covered 95.7% of all positions at 8X coverage or
            higher, resulting in over 5.4 million variants compared to a reference genome. These data were analyzed to
            identify previously reported variants of potential clinical relevance as well as novel variants that could
            reasonably be assumed to cause disease (see methodology below). All results are summarized on page 1 with
            further details on subsequent pages.

            <h5><font color="teal">A. MONOGENIC DISEASE RISK: 1 VARIANT IDENTIFIED</font></h5>


            <table>
                <tr style="vertical-align: top; background-color: lightgrey;">
                    <th style="padding: 5px">
                        Disease<br>Inheritance
                    </th>
                    <th style="padding: 5px">
                        Phenotype
                    </th>
                    <th style="padding: 5px">
                        Gene<br>Transcript
                    </th>
                    <th style="padding: 5px">
                        Zygosity<br>Variant
                    </th>
                    <th style="padding: 5px">
                        Classification
                    </th>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="padding: 5px">

                    <#list dom_high_candidate_genes?keys as geneName>

							<#if severelateonset?seq_contains(geneName)>
                    <#-- skipping this gene -->
                    <#else>
                    ${geneName}
                    </#if>
						</#list>

                    </td>
                    <td style="padding: 5px">
                        sss
                    </td>
                    <td style="padding: 5px">
                        ddd
                    </td>
                    <td style="padding: 5px">
                        fff
                    </td>
                    <td style="padding: 5px">
                        ggg
                    </td>
                </tr>
            </table>


            <h5><font color="teal">CAT.I: PHENOTYPE MATCH AND PROTEIN AFFECTING: 1 VARIANT</h5>

            <h5><font color="teal">CAT.II: SOMETHING</h5>

            <h5><font color="teal">B. CARRIER RISK: 2 VARIANTS IDENTIFIED</h5>


        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: green;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="green">DETAILED VARIANT INFORMATION</font></h4>

        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: DarkGoldenRod ;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="DarkGoldenRod ">PHARMACOGENOMIC ASSOCIATIONS AND BLOOD GROUPS</font></h4>

        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: DarkRed ;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="DarkRed ">RISK ALLELES</font></h4>

        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: indigo;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="indigo">METHODOLOGY</font></h4>

        </td>

    </tr>
</table>

