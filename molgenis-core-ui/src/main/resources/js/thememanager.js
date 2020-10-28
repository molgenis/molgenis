var themeRepository = "/@molgenis-ui/molgenis-theme/dist/themes"

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
      var self = this
      $.get('/api/data/sys_set_app/app').then(function(settings) {
          self.themeUrl = settings.data.theme_url
          self.themeUrlLegacy = settings.data.legacy_theme_url

          $.get(`${themeRepository}/index.json`).then(function (allThemes) {
              // Only show themes that are meant to be public.
              self.themes = allThemes.filter((t) => t.share)
              var matchedTheme = self.themes.find(t => self.themeUrl.includes(t.id))
              // Theme repository should be part of the expected path; otherwise it is a custom url.
              if (matchedTheme && self.themeUrl.includes(themeRepository)) {
                self.selectedTheme = matchedTheme.id
                self.selectionMethod = 'listed'
              } else {
                self.selectionMethod = 'url'
              }
          });
      })
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