import getters from 'store/getters'

const rows = [{
  'entity': 'home',
  'entityLabel': 'Home',
  'acl': {
    'entityIdentity': {
      'entityTypeId': 'sys_Plugin',
      'entityId': 'home'
    },
    'owner': {'username': 'SYSTEM'},
    'entries': [
      {
        'permissions': ['READ'],
        'securityId': {'username': 'anonymous'},
        'granting': true
      },
      {
        'permissions': ['READ'],
        'securityId': {'authority': 'user'},
        'granting': true
      }
    ]
  }
}, {
  'entity': 'dataexplorer',
  'entityLabel': 'Data Explorer',
  'acl': {
    'entityIdentity': {
      'entityTypeId': 'sys_Plugin',
      'entityId': 'dataexplorer'
    },
    'owner': {'username': 'SYSTEM'},
    'entries': [
      {
        'permissions': ['READ'],
        'securityId': {'username': 'anonymous'},
        'granting': true
      },
      {
        'permissions': ['READ', 'WRITE'],
        'securityId': {'authority': 'user'},
        'granting': true
      }
    ]
  }
}]

describe('getters', () => {
  describe('tableRows', () => {
    it('should show the rows matching the SID', () => {
      const state = {
        rows,
        selectedRole: 'user',
        permissions: ['WRITEMETA', 'WRITE', 'READ']
      }
      const actual = getters.tableRows(state)
      console.log(actual)
      expect(actual).to.deep.equal([{
        entityLabel: 'Home',
        owner: 'SYSTEM',
        granting: true,
        WRITEMETA: false,
        WRITE: false,
        READ: true,
        aceIndex: 1
      }, {
        entityLabel: 'Data Explorer',
        owner: 'SYSTEM',
        granting: true,
        WRITEMETA: false,
        WRITE: true,
        READ: true,
        aceIndex: 1
      }])
    })

    it('should show empty ACEs if no entry exists for the selected SID', () => {
      const state = {
        rows,
        selectedRole: 'admin',
        permissions: ['WRITEMETA', 'WRITE', 'READ']
      }
      const actual = getters.tableRows(state)
      console.log(actual)
      expect(actual).to.deep.equal([{
        entityLabel: 'Home',
        owner: 'SYSTEM',
        granting: true,
        aceIndex: -1,
        WRITEMETA: false,
        WRITE: false,
        READ: false
      }, {
        entityLabel: 'Data Explorer',
        owner: 'SYSTEM',
        granting: true,
        aceIndex: -1,
        WRITEMETA: false,
        WRITE: false,
        READ: false
      }])
    })
  })
})
