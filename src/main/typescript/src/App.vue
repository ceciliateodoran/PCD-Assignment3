<script lang="ts">
import { defineComponent } from "vue";
import Header from "./components/Header.vue";
import Footer from "./components/Footer.vue";

  export default defineComponent({
    components: {Footer, Header},
    data() {
      return {
        gridRows: 0,
        gridColumns: 0,
        numSensors: 0,
        barrackStatus: "not available",
        zoneStatus: "not available",
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
      <section>
        <div class="paragraph">
          <p># Pluviometri</p>
          <p>{{this.numSensors}}</p>
        </div>

        <div class="paragraph">
          <p>Status Caserma</p>
          <p>{{this.barrackStatus}}</p>
        </div>

        <div class="paragraph">
          <p>Status Zona {{this.selectedCity["name"]}}</p>
          <p>{{this.zoneStatus}}</p>

          <ButtonComp v-if="zoneStatus === 'ALLARME'" type="button" icon="pi pi-exclamation-triangle" label="GESTISCI ALLARME" @click.prevent="filter()" severity="danger"/>
          <ButtonComp v-if="zoneStatus === 'In Gestione'" type="button" icon="pi pi-check" label="STOP GESTIONE" @click.prevent="filter()" severity="success"/>
        </div>
      </section>

      <canvas></canvas>
    </div>
  </main>

  <Footer/>

</template>

<style lang="scss">
  @import "./assets/base.css";
</style>
