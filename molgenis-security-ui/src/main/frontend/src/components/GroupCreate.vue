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
            <small id="groupNameHelp" class="form-text text-muted">The group name as shown in the interface</small>
          </div>

          <div class="form-group">
            <label for="groupIdentifierInput">Group identifier</label>
            <input v-model="groupIdentifier" readonly type="text" class="form-control" id="groupIdentifierInput" placeholder="my-group">
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
            :disabled="!groupName">
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

  export default {
    name: 'GroupCreate',
    data () {
      return {
        groupName: '',
        isCreating: false
      }
    },
    computed: {
      groupIdentifier () {
        return this.slugify(this.groupName)
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
      slugify (text) {
        text = text.toString().toLowerCase().trim()

        const sets = [
          {to: 'a', from: '[ÀÁÂÃÄÅÆĀĂĄẠẢẤẦẨẪẬẮẰẲẴẶ]'},
          {to: 'c', from: '[ÇĆĈČ]'},
          {to: 'd', from: '[ÐĎĐÞ]'},
          {to: 'e', from: '[ÈÉÊËĒĔĖĘĚẸẺẼẾỀỂỄỆ]'},
          {to: 'g', from: '[ĜĞĢǴ]'},
          {to: 'h', from: '[ĤḦ]'},
          {to: 'i', from: '[ÌÍÎÏĨĪĮİỈỊ]'},
          {to: 'j', from: '[Ĵ]'},
          {to: 'ij', from: '[Ĳ]'},
          {to: 'k', from: '[Ķ]'},
          {to: 'l', from: '[ĹĻĽŁ]'},
          {to: 'm', from: '[Ḿ]'},
          {to: 'n', from: '[ÑŃŅŇ]'},
          {to: 'o', from: '[ÒÓÔÕÖØŌŎŐỌỎỐỒỔỖỘỚỜỞỠỢǪǬƠ]'},
          {to: 'oe', from: '[Œ]'},
          {to: 'p', from: '[ṕ]'},
          {to: 'r', from: '[ŔŖŘ]'},
          {to: 's', from: '[ßŚŜŞŠ]'},
          {to: 't', from: '[ŢŤ]'},
          {to: 'u', from: '[ÙÚÛÜŨŪŬŮŰŲỤỦỨỪỬỮỰƯ]'},
          {to: 'w', from: '[ẂŴẀẄ]'},
          {to: 'x', from: '[ẍ]'},
          {to: 'y', from: '[ÝŶŸỲỴỶỸ]'},
          {to: 'z', from: '[ŹŻŽ]'},
          {to: '-', from: '[·/_,:;\']'}
        ]

        sets.forEach(set => {
          text = text.replace(new RegExp(set.from, 'gi'), set.to)
        })

        return text
          .replace(/\s+/g, '-') // Replace spaces with -
          .replace(/[^\w-]+/g, '') // Remove all non-word chars
          .replace(/--+/g, '-') // Replace multiple - with single -
          .replace(/^-+/, '') // Trim - from start of text
          .replace(/-+$/, '') // Trim - from end of text
      }
    },
    components: {
      Toast
    }
  }
</script>
