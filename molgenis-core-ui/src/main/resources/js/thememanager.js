//TODO: https://unpkg.com/@molgenis/molgenis-theme/css/
let themeRepository = "/themes"

requirejs(["vue.min"], function(Vue) {
  new Vue({
    el: '#thememanager',
    data: {
      themes: [],
      selectedTheme: null
    },
    created() {
      let self = this
      $.get(`${themeRepository}/index.json`).then(
        function (themes) {
          // self.themes = themes.filter((t) => t.share)
          themes.forEach(function(theme) {
            if (__STATE__.theme.includes(theme.id)) {
              self.selectedTheme = theme.id
            }
          })
          self.themes = themes
        });
    },
    methods: {
      save() {
        if (!this.selectedTheme) {
          return
        }
        $.ajax({
          type: 'PATCH',
          url: '/api/data/sys_set_app/app',
          data: JSON.stringify({
            legacy_theme_url: `${themeRepository}/mg-${this.selectedTheme}-3.css`,
            theme_url: `${themeRepository}/mg-${this.selectedTheme}-4.css`
          }),
          contentType : 'application/json'
        }).then(function(){
          location.reload();
        })
      }
    }
  })
});