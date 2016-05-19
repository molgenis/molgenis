<div class="modal fade" tabindex="-1" role="dialog" id="tutorialModal">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" id="closeModal" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Tutorial</h4>
      </div>
      <div class="modal-body">
		<p>
			The Glia Open Access Database (GOAD) is a comprehensive web-based tool to access and analyze glia transcriptome data. <br/>
			The tool has several features that can be accessed by using the drop-down menu: 
			<ul>
				<li>Differential gene Expression (DE) Analysis</li>
				<li>Quantitative gene Expression (QE) analysis</li>
			</ul>
			<br/>
			<div class="btn-group btn-group-justified">
				<div class="btn-group"><button id="tutorialDEinfo" class="btn btn-default" type="button" data-toggle="collapse" data-target="#DEanalysis" aria-expanded="false">Differential Gene Expression Analysis</button></div>
				<div class="btn-group"><button id="tutorialQEinfo" class="btn btn-default" type="button" data-toggle="collapse" data-target="#QEanalysis" aria-expanded="false">Quantitative Gene Expression Analysis</button></div>
			</div>
			<div class="collapse" id="DEanalysis">
			  <div class="well">
			  	The Differential Expression (DE) analysis can be used to generate gene lists differentially expressed genes with the associated log fold changes and multiple testing corrected p-values between two conditions of interest (A vs. B). <br/>
				<br/>
			  	After performing the DE analysis, an interactive volcano scatterplot will be generated showing the most siginificant genes (max 2000 are shown within the plot).
			  	The volcano scatterplot can be used to search a specific gene with the use of a zoom.
			  	Information as the LogFC and FDR values can be seen with the use of a hover function.<br/>
			  	<br/>
			  	The table next to the scatterplot shows all of the significant genes (FDR 0.05).
			  	The columns: Gene symbol, LogFC and FDR can be found within this table.
			  	Genes of interest can be found with the use of the search bar on top of the table.<br/>
			  	<br/>
			  	The DE analysis is done with the use of R, using a pairwise comparison with edgeR.
			  	The raw dataset is filtered for genes with a ount per million (CPM) >= 2 for each row.
			  	Gene symbols are obtained using the biomaRt package.<br/>
			  </div>
			</div>
			<div class="collapse" id="QEanalysis">
			  <div class="well">
			  	The Quantitative Expression (QE) analysis can be used to determine which genes are expressed in particular cell type and to what degree.
			  	A table is obtained showing the gene symbols on the left side and the detected cell types on the right of the genesymbol.
			  	The TPM values of the given gene in a given cell type is shown.<br/>
			  	<br/>
			  	Transcripts Per Million (TPM) values are used to quantify gene expression in RNA sequencing data.
			  	TPM is a modification of RPKM, respecting average invariance and elimination statistical biases from the RPKM measure.
			  	The difference with TPM and RPKM/FPKM is that the normalization of the gene length is done first and normalization of the sequencing depth is done second.
			  	Leading to the same number when all of the TPMs are added in each sample, which makes it easier to compare the samples.<br/>
			  	<br/>
				<strong>Calculation: TPM = (number of reads * 10^6) / (sum normalized transcript counts) * gene length.</strong><br/>
			  	<br/>
			  	A bar graph is shown when clicking on a gene of interest, showing the TPM values (TPM low, TPM high and TPM) of that gene within all of the known cell types.
			  </div>
			</div>
		</p>
      </div>
    </div>
  </div>
</div>