<script lang="ts">
  import { defineComponent } from "vue";
  import Header from "./components/Header.vue";
  import Footer from "./components/Footer.vue";
  import router from "./router";

  export default defineComponent({
    components: {Header, Footer},
    mounted() {
      router.replace({ name: "homePage" });
    }
  });
</script>

<template>
  <Header/>

  <div class="card">
    <RouterView />
  </div>

  <Footer/>

</template>

<style lang="scss" setup>
  @import "./assets/base.css";
</style>
