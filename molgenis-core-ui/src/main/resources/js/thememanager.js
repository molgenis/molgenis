//TODO: https://unpkg.com/@molgenis/molgenis-theme/css/
let themeRepository = "https://zen-chandrasekhar-e7bd44.netlify.app"

requirejs(["vue.min"], function(Vue) {
  new Vue({
    el: '#thememanager',
    data: {
      themes: [],
      selectedTheme: null
    },
    created() {
      let self = this
      $.get(`${themeRepository}/theme.json`).then(
        function (themes) {
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