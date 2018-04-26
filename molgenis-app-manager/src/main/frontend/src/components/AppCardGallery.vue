<template>
    <div class="app-card-carousel">
        <div class="app-card-container row">
            <div v-for="app in apps" :key="app.id" class="col-4">
                <app-card :app="app"></app-card>
            </div>
        </div>
    </div>
</template>

<script>
  import AppCard from './AppCard.vue'

  export default {
    name: 'AppCardGallery',
    props: ['apps'],
    components: {
      AppCard
    }
  }
</script>