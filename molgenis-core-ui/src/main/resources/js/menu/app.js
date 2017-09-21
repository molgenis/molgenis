webpackJsonp([1], {
  117: function (e, t) {},
  118: function (e, t) {},
  119: function (e, t, n) {
    var a = n(86)(n(122), n(311), null, null, null)
    e.exports = a.exports
  },
  121: function (e, t, n) {
    'use strict'
    Object.defineProperty(t, '__esModule', {value: !0})
    var a = n(116), s = (n.n(a), n(115)), i = (n.n(s), n(120)), l = n.n(i), o = n(119), r = n.n(o), u = n(117),
      c = (n.n(u), n(118))
    n.n(c)
    new l.a({el: '#molgenis-menu', template: '<MolgenisMenu />', components: {MolgenisMenu: r.a}})
  },
  122: function (e, t, n) {
    'use strict'
    Object.defineProperty(t, '__esModule', {value: !0})
    var a = n(309), s = n.n(a)
    t.default = {
      components: {SubMenu: s.a},
      name: 'molgenis-menu',
      data: function () {
        return {
          menu: window.molgenisMenu.menu,
          topLogo: window.molgenisMenu.topLogo,
          navBarLogo: window.molgenisMenu.navBarLogo,
          helpLink: window.molgenisMenu.helpLink,
          authenticated: window.molgenisMenu.authenticated,
          selectedPlugin: window.molgenisMenu.selectedPlugin,
          showLanguageDropdown: window.molgenisMenu.showLanguageDropdown,
          logoutFunction: window.molgenisMenu.logoutFunction,
          googleSignIn: window.molgenisMenu.googleSignIn,
          collapse: !0,
          selectedIndex: {default: -1, type: Number}
        }
      },
      methods: {
        setIndex: function (e) {this.selectedIndex === e ? this.selectedIndex = -1 : this.selectedIndex = e},
        toggleNavbar: function () {this.collapse = !this.collapse},
        isSelectedPlugin: function (e) {return e === this.selectedPlugin},
        logout: function () {this.logoutFunction && this.logoutFunction(), document.getElementById('logout-form').submit()},
        login: function () {document.getElementById('login-modal').classList.add('show')}
      },
      mounted: function () {
        var e = this.setIndex
        window.addEventListener('click', function (t) {'nav-link dropdown-toggle' !== t.target.className && e(-1)}, !1)
      }
    }
  },
  123: function (e, t, n) {
    'use strict'
    Object.defineProperty(t, '__esModule', {value: !0})
    var a = n(310), s = n.n(a)
    t.default = {
      components: {SubMenuContent: s.a},
      name: 'sub-menu',
      props: ['selected', 'id', 'label', 'callback', 'index', 'items']
    }
  },
  124: function (e, t, n) {
    'use strict'
    Object.defineProperty(t, '__esModule', {value: !0}), t.default = {
      name: 'sub-menu-content',
      props: ['parent', 'id', 'href', 'label', 'type', 'items', 'depth']
    }
  },
  309: function (e, t, n) {
    var a = n(86)(n(123), n(312), null, null, null)
    e.exports = a.exports
  },
  310: function (e, t, n) {
    var a = n(86)(n(124), n(313), null, null, null)
    e.exports = a.exports
  },
  311: function (e, t) {
    e.exports = {
      render: function () {
        var e = this, t = e.$createElement, n = e._self._c || t
        return n('div', {staticClass: 'fixed-top'}, [e.topLogo ? [n('div', {attrs: {id: 'Intro'}}, [n('a', {attrs: {href: '/'}}, [n('img', {
          attrs: {
            src: e.topLogo,
            alt: '',
            border: '0',
            height: '150'
          }
        })])])] : e._e(), e._v(' '), n('nav', {staticClass: 'navbar navbar-toggleable-md navbar-light bg-faded navbar-fixed-top'}, [n('button', {
          staticClass: 'navbar-toggler navbar-toggler-right',
          attrs: {
            type: 'button',
            'data-toggle': 'collapse',
            'aria-controls': 'navbarNavDropdown',
            'aria-expanded': 'false',
            'aria-label': 'Toggle navigation'
          },
          on: {click: e.toggleNavbar}
        }, [n('span', {staticClass: 'navbar-toggler-icon'})]), e._v(' '), e.navBarLogo ? n('a', {
          staticClass: 'navbar-brand',
          attrs: {href: '/menu/main/' + e.menu.items[0].href}
        }, [n('img', {attrs: {src: e.navBarLogo, style:"max-height: 30px"}})]) : n('a', {
          staticClass: 'navbar-brand',
          attrs: {href: '#'}
        }), e._v(' '), n('div', {
          class: {collapse: e.collapse, 'navbar-collapse': e.collapse},
          attrs: {id: 'navbarNavDropdown'}
        }, [n('ul', {staticClass: 'navbar-nav mr-auto'}, [e._l(e.menu.items, function (t, a) {
          return ['PLUGIN' != t.type || 0 == a && e.navBarLogo ? ['MENU' == t.type && 0 != a ? [n('sub-menu', {
            attrs: {
              id: t.id,
              label: t.label,
              index: a,
              selected: a == e.selectedIndex,
              callback: e.setIndex,
              items: t.items
            }
          })] : e._e()] : [n('li', {
            staticClass: 'nav-item',
            class: {active: e.isSelectedPlugin(t.id)}
          }, [n('a', {staticClass: 'nav-link', attrs: {href: '/menu/main/' + t.href}}, [e._v(e._s(t.label))])])]]
        })], 2), e._v(' '), n('ul', {staticClass: 'nav navbar-nav'}, [e._m(0), e._v(' '), n('li', {staticClass: 'nav-item'}, [n('a', {
          staticClass: 'nav-link navbar-right',
          attrs: {href: e.helpLink.href, target: '_blank'}
        }, [e._v(e._s(e.helpLink.label))])]), e._v(' '), e.authenticated ? n('li', {staticClass: 'nav-link'}, [n('form', {
          staticClass: 'navbar-form navbar-right',
          attrs: {id: 'logout-form', method: 'post', action: '/logout'}
        }, [n('button', {
          staticClass: 'btn btn-primary btn-sm',
          attrs: {id: 'signout-button', type: 'button'},
          on: {click: e.logout}
        }, [e._v('Sign out\n            ')])])]) : n('li', {staticClass: 'nav-item'}, [n('a', {
          staticClass: 'nav-link navbar-right btn btn-secondary',
          on: {click: e.login}
        }, [e._v('Sign in')])])])])])], 2)
      }, staticRenderFns: [function () {
        var e = this, t = e.$createElement, n = e._self._c || t
        return n('li', {staticClass: 'nav-item'}, [n('div', {
          staticClass: 'navbar-right',
          attrs: {id: 'language-select-box'}
        })])
      }]
    }
  },
  312: function (e, t) {
    e.exports = {
      render: function () {
        var e = this, t = e.$createElement, n = e._self._c || t
        return n('li', {class: ['nav-item', 'dropdown', e.selected ? 'show' : '']}, [n('a', {
          staticClass: 'nav-link dropdown-toggle',
          attrs: {id: e.id, 'data-toggle': 'dropdown', 'aria-haspopup': 'true', 'aria-expanded': e.selected, href: '#'},
          on: {click: function (t) {e.callback(e.index)}}
        }, [e._v('\n    ' + e._s(e.label) + '\n  ')]), e._v(' '), n('div', {
          staticClass: 'dropdown-menu',
          attrs: {'aria-labelledby': 'navbarDropdownMenuLink'}
        }, [e._l(e.items, function (t, a) {
          return [n('sub-menu-content', {
            attrs: {
              parent: e.id,
              id: t.id,
              href: t.href,
              label: t.label,
              type: t.type,
              items: t.items,
              depth: 0
            }
          })]
        })], 2)])
      }, staticRenderFns: []
    }
  },
  313: function (e, t) {
    e.exports = {
      render: function () {
        var e = this, t = e.$createElement, n = e._self._c || t
        return 'PLUGIN' == e.type ? n('a', {
          staticClass: 'dropdown-item',
          attrs: {href: '/menu/' + e.parent + '/' + e.href}
        }, [e._v(e._s(e.label))]) : n('span', [n('h6', {
          staticClass: 'dropdown-header',
          class: 'menu-depth-' + e.depth
        }, [e._v(e._s(e.label))]), e._v(' '), e._l(e.items, function (t) {
          return [n('sub-menu-content', {
            attrs: {
              parent: e.id,
              id: t.id,
              href: t.href,
              label: t.label,
              type: t.type,
              items: t.items,
              depth: e.depth + 1
            }
          })]
        })], 2)
      }, staticRenderFns: []
    }
  },
  315: function (e, t) {}
}, [121])