<#macro footer version=1>
</div><#-- close plugin-container -->
</div><#-- close col-md-12 -->
</div><#-- close row -->
</div><#-- close container-fluid -->
    <div class="row footer">
        <div class="col-md-12">

        <#-- VUE -->
            <div id="footer"></div>

            <script type=text/javascript>
                window.molgenisFooter = {
                        <#if app_settings.footer??>additionalMessage: '${app_settings.footer}'</#if>
                    <#if app_settings.footer?? && (molgenis_version??||molgenis_build_date??)>,</#if>
                        <#if molgenis_version??>version: '${molgenis_version}'</#if>
                    <#if molgenis_version?? && molgenis_build_date??>,</#if>
                        <#if molgenis_build_date??>buildDate: '${molgenis_build_date}'</#if>}
            </script>


            <script type=text/javascript src="<@resource_href "/js/footer/manifest.js"/>"></script>
            <script type=text/javascript src="<@resource_href "/js/footer/vendor.js"/>"></script>
            <script type=text/javascript src="<@resource_href "/js/footer/app.js"/>"></script>

            <#if app_settings.googleAnalyticsTrackingId?? || app_settings.googleAnalyticsTrackingIdMolgenis??>
                <p class="text-muted text-center small ga-opt-out">
                    <em>We use Google Analytics to review this site's usage and improve our services.<br/>
                        To optimally protect your privacy we have signed the Data Processing Amendment, masked parts of
                        your IP address and disabled data sharing with other Google services.</em><br/>
                    <em>Click <a href="javascript:gaOptout()">here</a> to opt-out of Google Analytics.</em>
                </p>
                <p class="text-muted text-center small ga-opted-out hidden">
                    <em>You have opted out of Google Analytics.</em>
                </p>
            </#if>
        </div>
</div>
    <#if authenticated?? && authenticated>
    <#else>
        <#include "/login-modal.ftl">
    </#if>
</body>
    <#if app_settings.trackingCodeFooter?has_content>
    <script id="app-tracking-code-footer" type="text/javascript">
        ${app_settings.trackingCodeFooter?string}
		</script>
    </#if>
</html>
</#macro>