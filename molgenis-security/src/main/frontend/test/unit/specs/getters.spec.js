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
        selectedSid: 'user',
        permissions: ['WRITEMETA', 'WRITE', 'READ']
      }
      expect(getters.tableRows(state)).to.deep.equal([{
        entityLabel: 'Home',
        owner: 'SYSTEM',
        granting: true,
        WRITEMETA: false,
        WRITE: false,
        READ: true
      }, {
        entityLabel: 'Data Explorer',
        owner: 'SYSTEM',
        granting: true,
        WRITEMETA: false,
        WRITE: true,
        READ: true
      }])
    })

    it('should show empty ACEs if no entry exists for the selected SID', () => {
      const state = {
        rows,
        selectedSid: 'admin',
        permissions: ['WRITEMETA', 'WRITE', 'READ']
      }
      expect(getters.tableRows(state)).to.deep.equal([{
        entityLabel: 'Home',
        owner: 'SYSTEM',
        granting: true,
        WRITEMETA: false,
        WRITE: false,
        READ: false
      }, {
        entityLabel: 'Data Explorer',
        owner: 'SYSTEM',
        granting: true,
        WRITEMETA: false,
        WRITE: false,
        READ: false
      }])
    })
  })
})
