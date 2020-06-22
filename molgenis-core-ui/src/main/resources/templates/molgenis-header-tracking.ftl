<#-- Google Analytics tracking code -->
<#if app_settings.googleAnalyticsTrackingId?? || app_settings.googleAnalyticsTrackingIdMolgenis??>
<script>
        (function(){
          window.gaOptout = function () {
            $.cookie('gaOptout', 'true', { expires: 3650 });
            location.reload();
          }
        <#if cookieWall>
          if ($.cookie('permissionforcookies') !== 'true') {
            return;
          }
        </#if>
        if ($.cookie('gaOptout') === 'true') {
          $(function() {$('body').addClass('ga-opted-out')})
          return;
        }
        (function (i, s, o, g, r, a, m) {
            i['GoogleAnalyticsObject'] = r;
            i[r] = i[r] || function () {
                        (i[r].q = i[r].q || []).push(arguments)
                    }, i[r].l = 1 * new Date();
            a = s.createElement(o),
                    m = s.getElementsByTagName(o)[0];
            a.async = 1;
            a.src = g;
            m.parentNode.insertBefore(a, m)
        })(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');
        <#if app_settings.googleAnalyticsTrackingId??>
            ga('create', '${app_settings.googleAnalyticsTrackingId?html}', 'auto');
        </#if>
        <#if app_settings.googleAnalyticsTrackingIdMolgenis??>
            ga('create', '${app_settings.googleAnalyticsTrackingIdMolgenis?html}', 'auto', {'name': 'molgenisTracker'});
        </#if>
        ga('set', 'forceSSL', true);
        ga('set', 'anonymizeIp', ${app_settings.googleAnalyticsIpAnonymization?string('true', 'false')});
        <#if app_settings.googleAnalyticsTrackingId??>
            ga('send', 'pageview');
        </#if>
        <#if app_settings.googleAnalyticsTrackingIdMolgenis??>
            ga('molgenisTracker.send', 'pageview');
        </#if>
        })();
</script>
</#if>