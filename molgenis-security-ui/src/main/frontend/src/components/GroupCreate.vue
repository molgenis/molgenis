<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>Create group</h1>
      </div>
    </div>

    <div class="row">
      <div class="col-md-6">
        <form>

          <div class="form-group">
            <label for="groupNameInput">Group name</label>
            <input v-model="groupName" type="text" class="form-control" id="groupNameInput" aria-describedby="groupName"
                   placeholder="My group">
            <small v-if="!isGroupNameAvailable" class="form-text text-danger ">This group name is not available any more, please choose a different name.</small>
            <small v-else id="groupNameHelp" class="form-text text-muted">The group name as shown in the interface</small>
          </div>

          <div class="form-group">
            <label for="groupIdentifierInput">Group identifier</label>
            <input v-model="groupIdentifier" readonly type="text" class="form-control" id="groupIdentifierInput"
                   placeholder="my-group">
            <small id="groupIdentifierHelp" class="form-text text-muted">Name as used in URL</small>
          </div>

          <router-link to="/">
            <a href="#" class="btn btn-secondary" role="button">Cancel</a>
          </router-link>

          <button
            v-if="!isCreating"
            id="create-btn"
            class="btn btn-success"
            type="submit"
            @click.prevent="onSubmit"
            :disabled="!groupName || !isGroupNameAvailable">
            Create
          </button>

          <button
            v-else
            id="save-btn-saving"
            class="btn btn-primary"
            type="button"
            disabled="disabled">
            Create <i class="fa fa-spinner fa-spin "></i>
          </button>

        </form>
      </div>
    </div>


  </div>
</template>

<script>
  import Toast from './Toast'
  import slugService from '../service/slugService'
  import _ from 'lodash'

  export default {
    name: 'GroupCreate',
    data () {
      return {
        groupName: '',
        isCreating: false,
        isGroupNameAvailable: true,
        isCheckingGroupName: true
      }
    },
    computed: {
      groupIdentifier () {
        return slugService.slugify(this.groupName)
      }
    },
    watch: {
      groupName (newVal) {
        if (newVal) {
          this.checkGroupName()
        }
      }
    },
    methods: {
      onSubmit () {
        this.isCreating = !this.isCreating
        const createGroupCommand = {groupIdentifier: this.groupIdentifier, name: this.groupName}
        this.$store.dispatch('createGroup', createGroupCommand)
          .then(() => {
            this.$router.push({name: 'groupOverView'})
          }, () => {
            this.isCreating = !this.isCreating
          })
      },

      checkGroupName: _.throttle(function () {
        const pipesRegEx = '/-/g'
        const packageName = this.groupIdentifier.replace(pipesRegEx, '_')
        this.$store.dispatch('checkRootPackageExists', packageName).then((exists) => {
          this.isGroupNameAvailable = !exists
        })
      }, 300)
    },
    components: {
      Toast
    }
  }
</script>
