<script lang="ts">
import { defineComponent } from "vue";
import Header from "./components/Header.vue";
import Footer from "./components/Footer.vue";

  export default defineComponent({
    components: {Footer, Header},
    data() {
      return {
        selectedCity: null,
        cities: [
          { name: 'New York', code: 'NY' },
          { name: 'Rome', code: 'RM' },
          { name: 'London', code: 'LDN' },
          { name: 'Istanbul', code: 'IST' },
          { name: 'Paris', code: 'PRS' }
        ]
      };
    }
  });
</script>

<template>
  <Header/>

  <main>
    <div class="p-float-label">
      <DropDown v-model="selectedCity" :options="cities" optionLabel="name" placeholder="Seleziona la Zona" class="w-full md:w-14rem" />
    </div>
    <div v-if="selectedCity">
      <div>
        <p># Pluviometri</p>
        <p></p>
      </div>
      <div>
        <p>Status Caserma</p>
        <p></p>
      </div>
      <div>
        <p>Status Zona {{this.selectedCity}}</p>
        <p></p>
      </div>

      <canvas id="myCanvas" width="800" height="200" style="border:1px solid #000000;">

      </canvas>
    </div>
  </main>

  <Footer/>

</template>

<style lang="scss">
  @import "./assets/base.css";
</style>
