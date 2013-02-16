#MOLGENIS walltime=00:05:00
#DESCRIPTION Documentation of QCReport.ftl, ${getStatisticsScript}
#string project

<#include "Helpers.ftl"/>

# We need some parameters folded per sample:
<#assign folded = foldParameters(parameters,"project,externalSampleID") />
<#assign samplehsmetrics 				= stringList(folded, "samplehsmetrics") />
<#assign samplealignmentmetrics 		= stringList(folded, "samplealignmentmetrics") />
<#assign sampleinsertsizemetrics 		= stringList(folded, "sampleinsertsizemetrics") />
<#assign sampleconcordancefile 			= stringList(folded, "sampleconcordancefile") />
<#assign externalSampleIDfolded			= stringList(folded, "externalSampleID") />
<#assign snpsfinalvcftabletypefolded	= stringList(folded, "snpsfinalvcftabletype") />
<#assign snpsfinalvcftableclassfolded	= stringList(folded, "snpsfinalvcftableclass") />
<#assign snpsfinalvcftableimpactfolded	= stringList(folded, "snpsfinalvcftableimpact") />
<#--assign typetableoutfolded				= stringList(folded, "typetableout") />
<#assign classtableoutfolded			= stringList(folded, "classtableout") />
<#assign impacttableoutfolded			= stringList(folded, "impacttableout") /-->

# parameters in *.tex template:
<#assign samplecoverageplotpdf 			= stringList(folded, "samplecoverageplotpdf") />
<#assign sampleinsertsizemetricspdf		= stringList(folded, "sampleinsertsizemetricspdf") />
<#assign samplegcbiasmetricspdf			= stringList(folded, "samplegcbiasmetricspdf") />

inputs ${ssvQuoted(samplehsmetrics)}
inputs ${ssvQuoted(samplealignmentmetrics)}
inputs ${ssvQuoted(sampleinsertsizemetrics)}
inputs ${ssvQuoted(dedupmetrics)}
inputs ${ssvQuoted(sampleconcordancefile)}
inputs ${qcstatisticscolnames}

export PATH=${R_HOME}/bin:<#noparse>${PATH}</#noparse>
export R_LIBS=${R_LIBS}

# get general sample statistics
Rscript ${getStatisticsScript} \
--hsmetrics ${csvQuoted(samplehsmetrics)} \
--alignment ${csvQuoted(samplealignmentmetrics)} \
--insertmetrics ${csvQuoted(sampleinsertsizemetrics)} \
--dedupmetrics ${csvQuoted(dedupmetrics)} \
--concordance ${csvQuoted(sampleconcordancefile)} \
--sample ${csvQuoted(externalSampleIDfolded)} \
--colnames ${qcstatisticscolnames} \
--csvout ${qcstatisticscsv} \
--tableout ${qcstatisticstex} \
--descriptionout ${qcstatisticsdescription} \
--baitsetout ${qcbaitset} \
--qcdedupmetricsout ${qcdedupmetricsout}

# get dedup info per flowcell-lane-barcode/sample
Rscript ${getDedupInfoScript} \
--dedupmetrics ${csvQuoted(dedupmetrics)} \
--flowcell ${csvQuoted(flowcell)} \
--lane ${csvQuoted(lane)} \
--sample ${csvQuoted(externalSampleID)} \
--paired TRUE \
--qcdedupmetricsout "${qcdedupmetricsout}"

# get snp stats per sample
Rscript ${createsnptablescript} \
--sample ${csvQuoted(externalSampleIDfolded)} \
--type ${csvQuoted(snpsfinalvcftabletypefolded)} \
--class ${csvQuoted(snpsfinalvcftableclassfolded)} \
--impact ${csvQuoted(snpsfinalvcftableimpactfolded)} \
--typetableout "${typetableout}" \
--classtableout "${classtableout}" \
--impacttableout "${impacttableout}"


# create workflow figure
echo "${graph(workflowElements)}" | ${dot} -Tpng > ${workflowpng}

# save latex template in file
echo "<#include "QCReportTemplate.tex"/>" > ${qcstatisticstexreport}

pdflatex -output-directory=${qcdir} ${qcstatisticstexreport}
pdflatex -output-directory=${qcdir} ${qcstatisticstexreport} <#--do twice to fill all cross references-->