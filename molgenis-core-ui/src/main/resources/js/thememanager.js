//TODO: https://unpkg.com/@molgenis/molgenis-theme/css/
let themeRepository = "/themes"

requirejs(["vue.min"], function(Vue) {
  new Vue({
    el: '#thememanager',
    data: {
      themes: [],
      selectionMethod: 'listed',
      selectedTheme: null,
      themeUrl: '',
      themeUrlLegacy: ''
    },
    created() {
      let self = this
      $.get(`${themeRepository}/index.json`).then(
        function (allThemes) {
          // Only show themes that are meant to be public.
          self.themes = allThemes.filter((t) => t.share)
          var matchedTheme = self.themes.find(t => __STATE__.theme.url.includes(t.id))
          if (matchedTheme) {
            self.selectedTheme = matchedTheme.id

            self.themeUrl = `${themeRepository}/mg-${matchedTheme.id}-4.css`
            self.themeUrlLegacy = `${themeRepository}/mg-${matchedTheme.id}-3.css`

          } else {
            self.themeUrl = __STATE__.theme.url
            self.themeUrlLegacy = __STATE__.theme.urlLegacy
            self.selectionMethod = 'url'
          }
        });
    },
    methods: {
      save() {
        $.ajax({
          type: 'PATCH',
          url: '/api/data/sys_set_app/app',
          data: JSON.stringify({
            legacy_theme_url: this.themeUrlLegacy,
            theme_url: this.themeUrl
          }),
          contentType : 'application/json'
        }).then(function(){
          location.reload();
        })
      }
    },
    watch: {
      selectedTheme: function(newVal) {
        this.themeUrl = `${themeRepository}/mg-${newVal}-4.css`
        this.themeUrlLegacy = `${themeRepository}/mg-${newVal}-3.css`
      }
    }
  })
});