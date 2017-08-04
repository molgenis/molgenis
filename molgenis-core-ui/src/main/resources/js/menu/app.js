webpackJsonp([1], {
  117: function (t, e) {},
  118: function (t, e) {},
  119: function (t, e, n) {
    var a = n(86)(n(122), n(312), null, null, null)
    t.exports = a.exports
  },
  121: function (t, e, n) {
    'use strict'
    Object.defineProperty(e, '__esModule', {value: !0})
    var a = n(116), s = (n.n(a), n(115)), o = (n.n(s), n(120)), l = n.n(o), i = n(119), r = n.n(i), c = n(117),
      u = (n.n(c), n(118))
    n.n(u)
    new l.a({el: '#molgenis-menu', template: '<Menu />', components: {Menu: r.a}})
  },
  122: function (t, e, n) {
    'use strict'
    Object.defineProperty(e, '__esModule', {value: !0})
    var a = n(309), s = n.n(a)
    e.default = {
      components: {SubMenu: s.a},
      name: 'molgenis-menu',
      data: function () {
        return {
          menu: window.__INITIAL_STATE__.menu,
          topLogo: window.__INITIAL_STATE__.topLogo,
          navBarLogo: window.__INITIAL_STATE__.navBarLogo,
          helpLink: window.__INITIAL_STATE__.helpLink,
          authenticated: window.__INITIAL_STATE__.authenticated,
          selectedPlugin: window.__INITIAL_STATE__.selectedPlugin,
          showLanguageDropdown: window.__INITIAL_STATE__.showLanguageDropdown,
          logoutFunction: window.__INITIAL_STATE__.logoutFunction,
          googleSignIn: window.__INITIAL_STATE__.googleSignIn,
          collapse: !0,
          selectedIndex: {default: -1, type: Number}
        }
      },
      methods: {
        setIndex: function (t) {this.selectedIndex === t ? this.selectedIndex = -1 : this.selectedIndex = t},
        toggleNavbar: function () {this.collapse = !this.collapse},
        showNavBarLogo: function (t) {return 0 === t && this.navBarLogo},
        isSelectedPlugin: function (t) {return t === this.selectedPlugin},
        logout: function () {this.logoutFunction && this.logoutFunction(), document.getElementById('logout-form').submit()},
        login: function () {document.getElementById('login-modal').classList.add('show')}
      }
    }
  },
  123: function (t, e, n) {
    'use strict'
    Object.defineProperty(e, '__esModule', {value: !0})
    var a = n(310), s = n.n(a)
    e.default = {
      components: {SubMenuContent: s.a},
      name: 'sub-menu',
      props: ['selected', 'id', 'label', 'callback', 'index', 'items']
    }
  },
  124: function (t, e, n) {
    'use strict'
    Object.defineProperty(e, '__esModule', {value: !0}), e.default = {
      name: 'sub-menu-content',
      props: ['parent', 'id', 'href', 'label', 'type', 'items', 'depth']
    }
  },
  309: function (t, e, n) {
    var a = n(86)(n(123), n(311), null, null, null)
    t.exports = a.exports
  },
  310: function (t, e, n) {
    var a = n(86)(n(124), n(313), null, null, null)
    t.exports = a.exports
  },
  311: function (t, e) {
    t.exports = {
      render: function () {
        var t = this, e = t.$createElement, n = t._self._c || e
        return n('li', {class: ['nav-item', 'dropdown', t.selected ? 'show' : '']}, [n('a', {
          staticClass: 'nav-link dropdown-toggle',
          attrs: {id: t.id, 'data-toggle': 'dropdown', 'aria-haspopup': 'true', 'aria-expanded': t.selected},
          on: {click: function (e) {t.callback(t.index)}}
        }, [t._v('\n    ' + t._s(t.label) + '\n  ')]), t._v(' '), n('div', {
          staticClass: 'dropdown-menu',
          attrs: {'aria-labelledby': 'navbarDropdownMenuLink'}
        }, [t._l(t.items, function (e, a) {
          return [n('sub-menu-content', {
            attrs: {
              parent: t.id,
              id: e.id,
              href: e.href,
              label: e.label,
              type: e.type,
              items: e.items,
              depth: 0
            }
          })]
        })], 2)])
      }, staticRenderFns: []
    }
  },
  312: function (t, e) {
    t.exports = {
      render: function () {
        var t = this, e = t.$createElement, n = t._self._c || e
        return n('div', [t.topLogo ? [n('div', {attrs: {id: 'TopLogo'}}, [n('a', {attrs: {href: '/'}}, [n('img', {
          attrs: {
            src: t.topLogo,
            alt: '',
            border: '0',
            height: '150'
          }
        })])])] : t._e(), t._v(' '), n('nav', {staticClass: 'navbar navbar-toggleable-md navbar-default'}, [n('button', {
          staticClass: 'navbar-toggler navbar-toggler-right',
          attrs: {
            type: 'button',
            'data-toggle': 'collapse',
            'aria-controls': 'navbarNavDropdown',
            'aria-expanded': 'false',
            'aria-label': 'Toggle navigation'
          },
          on: {click: t.toggleNavbar}
        }, [n('span', {staticClass: 'navbar-toggler-icon'})]), t._v(' '), n('div', {
          class: {
            collapse: t.collapse,
            'navbar-collapse': t.collapse
          }, attrs: {id: 'navbarNavDropdown'}
        }, [n('ul', {staticClass: 'navbar-nav mr-auto'}, [t._l(t.menu.items, function (e, a) {
          return ['PLUGIN' == e.type ? [n('li', {
            staticClass: 'nav-item',
            class: {active: t.isSelectedPlugin(e.id)}
          }, [t.showNavBarLogo(a) ? n('a', {
            staticClass: 'navbar-brand',
            attrs: {href: '/menu/main/' + e.href}
          }, [n('img', {attrs: {src: t.navBarLogo}})]) : n('a', {
            staticClass: 'nav-link',
            attrs: {href: '/menu/main/' + e.href}
          }, [t._v(t._s(e.label))])])] : ['MENU' == e.type ? [n('sub-menu', {
            attrs: {
              id: e.id,
              label: e.label,
              index: a,
              selected: a == t.selectedIndex,
              callback: t.setIndex,
              items: e.items
            }
          })] : t._e()]]
        })], 2), t._v(' '), n('ul', {staticClass: 'nav navbar-nav'}, [t._m(0), t._v(' '), n('li', {staticClass: 'nav-link'}, [n('a', {
          staticClass: 'nav-link navbar-right btn btn-secondary',
          attrs: {href: t.helpLink.href, target: '_blank'}
        }, [t._v(t._s(t.helpLink.label))])]), t._v(' '), t.authenticated ? n('li', {staticClass: 'nav-link'}, [n('form', {
          staticClass: 'navbar-form navbar-right',
          attrs: {id: 'logout-form', method: 'post', action: '/logout'}
        }, [n('button', {
          staticClass: 'btn btn-primary',
          attrs: {id: 'signout-button', type: 'button'},
          on: {click: t.logout}
        }, [t._v('Sign out\n            ')])])]) : n('li', {staticClass: 'nav-link'}, [n('a', {
          staticClass: 'nav-link navbar-right btn btn-secondary',
          on: {click: t.login}
        }, [t._v('Sign in')])])])])])], 2)
      }, staticRenderFns: [function () {
        var t = this, e = t.$createElement, n = t._self._c || e
        return n('li', {staticClass: 'nav-link'}, [n('div', {
          staticClass: 'navbar-right',
          attrs: {id: 'language-select-box'}
        })])
      }]
    }
  },
  313: function (t, e) {
    t.exports = {
      render: function () {
        var t = this, e = t.$createElement, n = t._self._c || e
        return 'PLUGIN' == t.type ? n('a', {
          staticClass: 'dropdown-item',
          attrs: {href: '/menu/' + t.parent + '/' + t.href}
        }, [t._v(t._s(t.label))]) : n('span', [n('h6', {
          staticClass: 'dropdown-header',
          class: 'menu-depth-' + t.depth
        }, [t._v(t._s(t.label))]), t._v(' '), t._l(t.items, function (e) {
          return [n('sub-menu-content', {
            attrs: {
              parent: t.id,
              id: e.id,
              href: e.href,
              label: e.label,
              type: e.type,
              items: e.items,
              depth: t.depth + 1
            }
          })]
        })], 2)
      }, staticRenderFns: []
    }
  },
  315: function (t, e) {}
}, [121])