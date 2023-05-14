<script lang="ts">
import { defineComponent } from "vue";
import Header from "./components/Header.vue";
import Footer from "./components/Footer.vue";
import CanvasGrid from "./components/CanvasGrid.vue";

  export default defineComponent({
    components: {Header, Footer, CanvasGrid},
    data() {
      return {
        numSensors: 0,
        barrackStatus: "not available",
        zoneStatus: "ALLARME",
        selectedZone: null,
        zones: [
          { name: 'Zona1', code: 'Z1' },
          { name: 'Zona2', code: 'Z2' },
          { name: 'Zona3', code: 'Z3' },
          { name: 'Zona4', code: 'Z4' },
          { name: 'Zona5', code: 'Z5' }
        ]
      };
    }
  });
</script>

<template>
  <Header/>

  <main>
    <div class="p-float-label">
      <DropDown v-model="selectedZone" :options="zones" optionLabel="name" placeholder="Seleziona la Zona" class="w-full md:w-14rem" />
    </div>

    <div v-if="selectedZone">
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
          <p>Status {{ this.selectedZone["name"] }}</p>
          <p>{{this.zoneStatus}}</p>

          <ButtonComp v-if="zoneStatus === 'ALLARME'" type="button" icon="pi pi-exclamation-triangle" label="GESTISCI ALLARME" @click.prevent="filter()" severity="danger"/>
          <ButtonComp v-if="zoneStatus === 'In Gestione'" type="button" icon="pi pi-check" label="STOP GESTIONE" @click.prevent="filter()" severity="success"/>
        </div>
      </section>
      <CanvasGrid/>
    </div>
  </main>

  <Footer/>

</template>

<style lang="scss">
  @import "./assets/base.css";
</style>
