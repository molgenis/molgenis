<template>
    <div class="card">
        <div class="card-header">
            <h3>{{ app.label }}</h3>
            <span>
                Status:
                <span v-if="app.isActive" style="color:green">Active</span>
                <span v-else style="color:red">Disabled</span>
            </span>
        </div>

        <div class="card-body">
            <p class="lead"> {{ app.description }} </p>
            <p><a :href="'/menu/plugins/app/' + app.uri">Go to app</a></p>
        </div>
    </div>
</template>

<script>
  export default {
    name: 'AppCardComponent',
    props: ['app']
  }
</script>