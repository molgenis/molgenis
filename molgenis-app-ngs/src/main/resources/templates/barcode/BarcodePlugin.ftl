<#macro BarcodePlugin screen>
	<form method="post" CLASS="form-horizontal" enctype="multipart/form-data" name="${screen.name}" action="">
		<!--needed in every form: to redirect the request to the right screen-->
		<input type="hidden" name="__target" value="${screen.name}">
		<!--needed in every form: to define the action. This can be set by the submit button-->
		<input type="hidden" name="__action">
		<!--need to be set to "true" in order to force a download-->
		<input type="hidden" name="__show">
		
		<div class="formscreen">
			<div class="form_header" id="${screen.name}"><i class="icon-barcode icon-white"></i> Barcode selector</div>
			<DIV style="padding:16px">
				<P>This page calculates the most optimal set of barcodes to analyze your samples with.</P>
				<div class="control-group">
				    <label class="control-label" for="type">Please select a barcode type</label>
				    <div class="controls">
						<SELECT NAME="type" CLASS="span1">
							<#list screen.getBarcodeTypes() as t>
							  <OPTION VALUE="${t}">${t}</OPTION>
							</#list>
						</SELECT>
				    </div>
				</div>

				<div class="control-group">
				    <label class="control-label" for="type">How many samples?</label>
				    <div class="controls">
						<INPUT TYPE="text" NAME="number" CLASS="span1">
				    </div>
				</div>

				<div class="control-group">
				    <div class="controls">
						<BUTTON TYPE="submit" CLASS="btn btn-primary"><i class="icon-barcode icon-white"></i> Get optimal set</BUTTON> (may take a while)
				    </div>
				</div>
				
				<#if screen.isException()>
					<TABLE CLASS="table" STYLE="border:  1px solid #ddd;">
					<THEAD>
						<TR>
							<TH>Plexity</TH>
							<TH>Solution</TH>
							<TH>Set A only</TH>
							<TH>Set B only</TH>
						</TR>
					</THEAD>
					<TR>
						<TD>2</TD><TD>1</TD><TD>RPI 06, RPI 12</TD><TD>Not recommended</TD>
					</TR>
					<TR>
						<TD> </TD><TD>2</TD><TD>RPI 05, RPI 19</TD><TD></TD>
					</TR>
					<TR>
						<TD>3</TD><TD>1</TD><TD>RPI 02, RPI 07, RPI 19</TD><TD>RPI 01, RPI 10, RPI 20</TD>
					</TR>
					<TR>
						<TD> </TD><TD>2</TD><TD>RPI 05, RPI 06, RPI 15</TD><TD>RPI 03, RPI 09, RPI 25</TD>
					</TR>
					<TR>
						<TD> </TD><TD>3</TD><TD>2-plex solution with any other adapter</TD><TD>RPI 08, RPI 11, RPI 22</TD>
					</TR>
					<TR>
						<TD>4</TD><TD>1</TD><TD>RPI 05, RPI 06, RPI 12, RPI 19</TD><TD>RPI 01, RPI 08, RPI 10, RPI 11</TD>
					</TR>
					<TR>
						<TD> </TD><TD>2</TD><TD>RPI 02, RPI 04, RPI 7, RPI 16</TD><TD>RPI 03, RPI 09, RPI 22, RPI 27</TD>
					</TR>
					<TR>
						<TD> </TD><TD>3</TD><TD>3-plex solution with any other adapter</TD><TD>3-plex solution with any other adapter</TD>
					</TR>
					</TABLE>
				<#else>
					<#if 0 < screen.getOptimalCombinations()?size>
						<H3>Result</H3>
						<P>We have found ${screen.getOptimalCombinations()?size} optimal set(s) of ${screen.getCurrentNumber()} barcodes each. Within each set each barcode-pair differs with at least ${screen.getMinimumDistance()} nucleotides, and on average with ${screen.getAverageDistance()} nucleotides. The sets follow below.</P>
						<P STYLE="color:brown">Be careful, this software is not very well tested yet.</P>
						<#list screen.getOptimalCombinations() as combination>
							Solution number ${combination_index + 1}:
							<TABLE CLASS="table" STYLE="width: 1%; border:  1px solid #ddd;">
								<#list combination as bc>
									<TR>
										<#list bc as b>
											<TD STYLE="font-family:monospace;">${b}</TD>
										</#list>
									</TR>
								</#list>
							</TABLE>
						</#list>
					</#if>
				</#if>				
			</DIV>
		</div>
	</form>
</#macro>