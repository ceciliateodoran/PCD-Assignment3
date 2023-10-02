<script lang="ts">
  import axios from "axios";
  import {computed, defineComponent} from "vue";
  import {useRoute} from "vue-router";
  import {Environment} from "../environment.ts";
  import CanvasGrid from "../components/CanvasGrid.vue";

  export default defineComponent({
    components: {CanvasGrid},
    data() {
      return {
        timer: null,
        numSensors: null,
        barrackStatus: null,
        zoneStatus: null,
        currentZone: null,
        selectedZone: null,
        myZone: null,
        zones: [],
        sensorsInfo: [],
        changedZone: false,
        partialData: null,
        isSilenced: false,
        loading: "",
      };
    },
    beforeMount() {
      //set list of all zones
      this.allZones();
      //set the value of my zone
      this.getMyZone();
    },
    mounted() {
      // every 10s
      this.timer = setInterval(() => {
      //ask for selected zone info
      this.selectedZoneInfo();
      }, 10000);
    },
    methods: {
      allZones(){
        //get all zones of the city
        axios
            .get(`${Environment.APPLICATION_BACKEND_HOST}/zones`)
            .then((response) => {
              this.reset();
              response.data.zones.forEach((zone: any) => {
                this.zones.push({
                  name: "Zona"+`${zone}`,
                  code: `${zone}`,
                });
              });
            })
            .catch((error) => {
              console.log(error);
            });
      },
      getMyZone(){
        axios
            .get(`${Environment.APPLICATION_BACKEND_HOST}/myzone`)
            .then((response) => {
              this.myZone = response.data.myzone;
            })
            .catch((error) => {
              console.log(error);
            });
      },
      selectedZoneInfo() {
        if (this.currentZone!==null){
          //set the currentZone zone to this.currentZone["code"]
          axios
              .post(`${Environment.APPLICATION_BACKEND_HOST}/zone/`+this.currentZone["code"])
              .then((response) => {
                this.sensorsInfo = [];
                response.data.sensorsinfo.forEach((info: any) => {
                  if(info === "ERROR"){
                    this.sensorsInfo = [];
                  }else{
                    let informations = info.split("|");
                    this.sensorsInfo.push({
                      id: `${informations[0].replace("sensor:","")}`,
                      value: `${informations[1]}`,
                      limit: `${informations[2]}`,
                    });
                  }
                });
              })
              .catch((error) => {
                console.log(error);
              });
          //get number of sensors of the currentZone zone
          axios
              .get(`${Environment.APPLICATION_BACKEND_HOST}/sensors`)
              .then((response) => {
                if(response.data === -1){
                  this.numSensors = "non disponibile al momento";
                }else {
                  this.numSensors = response.data.sensors;
                }
              })
              .catch((error) => {
                console.log(error);
              });
          //get the status of the currentZone zone and info about partial data
          axios
              .get(`${Environment.APPLICATION_BACKEND_HOST}/barrackstatus`)
              .then((response) => {
                let informations = response.data.barrackstatus.split("|");

                switch(informations[0]) {
                  case "FLOOD": {
                    this.zoneStatus = "ALLARME";
                    this.barrackStatus = "LIBERA";
                    break;
                  }
                  case "OK": {
                    if(this.currentZone['code'] != this.myZone ||
                        (this.currentZone['code'] == this.myZone && this.loading==='OK') ||
                        (this.currentZone['code'] == this.myZone && this.changedZone)){
                      this.zoneStatus = "OK";
                      this.barrackStatus = "LIBERA";
                    }
                    break;
                  }
                  case "COMMITTED": {
                    if(this.currentZone['code'] != this.myZone ||
                        (this.currentZone['code'] == this.myZone && this.loading==='IN GESTIONE') ||
                        (this.currentZone['code'] == this.myZone && this.changedZone)){
                      this.zoneStatus = "IN GESTIONE";
                      this.barrackStatus = "OCCUPATA";
                      this.loading = "";
                    }
                    break;
                  }
                  case "SILENCED":{
                    if((this.currentZone['code'] == this.myZone && this.loading==='SILENCED') ||
                        (this.currentZone['code'] == this.myZone && this.changedZone)){
                      this.isSilenced = true;
                      this.zoneStatus = "OK";
                      this.barrackStatus = "LIBERA";
                      this.loading = "";
                    }
                    break;
                  }
                  case "No barrack status":{
                    this.zoneStatus = "non disponibile";
                    this.barrackStatus = "non disponibile";
                    break;
                  }
                }
                if(informations[1]==="true"){
                  this.partialData = "sì";
                }else{
                  this.partialData = "no";
                }
                this.changedZone = false;
              })
              .catch((error) => {
                console.log(error);
              });
        }
        if(this.myZone===null){
          this.getMyZone();
        }
        if(this.zones.length<=0){
          this.allZones();
        }
      },
      manageAlarm(){
        //set zoneStatus in 'IN GESTIONE' and barrack in 'OCCUPATA'
        axios
            .post(`${Environment.APPLICATION_BACKEND_HOST}/alarm/manage/`+this.currentZone["code"])
            .then((response) => {
              this.loading = "IN GESTIONE";
            })
            .catch((error) => {
              console.log(error);
            });
      },
      stopAlarmManagement(){
        //set zoneStatus in 'OK' and barrack in 'LIBERA'
        axios
            .post(`${Environment.APPLICATION_BACKEND_HOST}/alarm/stop/`+this.currentZone["code"])
            .then((response) => {
              this.loading = "OK";
            })
            .catch((error) => {
              console.log(error);
            });
      },
      silenceAlarm(){
        //change the status of the barrack in silenced
        axios
            .post(`${Environment.APPLICATION_BACKEND_HOST}/alarm/silence/`+this.currentZone["code"])
            .then((response) => {
              this.loading = "SILENCED";
            })
            .catch((error) => {
              console.log(error);
            });
      },
      desilenceAlarm(){
        //change the status of the barrack in not silenced
        axios
            .post(`${Environment.APPLICATION_BACKEND_HOST}/alarm/desilence/`+this.currentZone["code"])
            .then((response) => {
              this.isSilenced = false;
            })
            .catch((error) => {
              console.log(error);
            });
      },
      changeZone(){
        //change the value of the current zone with the selected zone in the dropdown
        this.changedZone = true;
        this.currentZone = this.selectedZone;
        this.selectedZoneInfo();
      },
      reset(){
        this.timer= null;
        this.numSensors= null;
        this.barrackStatus= null;
        this.zoneStatus= null;
        this.currentZone= null;
        this.selectedZone= null;
        this.myZone= null;
        this.zones= [];
        this.sensorsInfo= [];
        this.changedZone= false;
        this.partialData= null;
        this.loading = "";
      },
    },
  });
</script>

<template>

  <main>
    <h2>Caserma - Zona{{this.myZone}} di competenza</h2>
    <CanvasGrid/>

    <div class="p-float-label" v-if="this.myZone!==null && this.zones.length>0">
      <h3>Seleziona una Zona</h3>
      <div class="alignment">
        <DropDown v-model="selectedZone" :options="zones" optionLabel="name" placeholder="Seleziona una Zona" class="w-full md:w-14rem" />
        <ButtonComp type="button" icon="pi pi-search" aria-label="Cerca Zona" @click.prevent="changeZone()" style="margin-left: 10px"/>
      </div>
    </div>

    <div v-if="changedZone" class="card flex justify-content-center">
      <ProgressComp />
    </div>

    <div v-else-if="!changedZone&&numSensors!==null&&zoneStatus!==null&&barrackStatus!==null">

      <section v-if="this.currentZone['code'] == this.myZone">
        <ButtonComp v-if="this.isSilenced!==true" type="button" icon="pi pi-moon" label="SILENZIA ALLARMI" severity="help" class="full" @click.prevent="this.silenceAlarm()" :loading="loading==='SILENCED'"/>
        <ButtonComp v-if="this.isSilenced" type="button" icon="pi pi-sun" label="DE SILENZIA ALLARMI" severity="warning" class="full" @click.prevent="this.desilenceAlarm()"/>
      </section>

      <section>
        <div class="paragraph">
          <p>Status {{ this.currentZone["name"] }}</p>

          <ButtonComp v-if="this.currentZone['code'] == this.myZone && this.zoneStatus === 'ALLARME'" type="button" icon="pi pi-exclamation-triangle" label="GESTISCI ALLARME" @click.prevent="manageAlarm()" severity="danger" :loading="loading==='IN GESTIONE'"/>
          <ButtonComp v-if="this.currentZone['code'] == this.myZone && this.zoneStatus === 'IN GESTIONE'" type="button" icon="pi pi-check" label="ALLARME GESTITO" @click.prevent="stopAlarmManagement()" severity="success" :loading="loading==='OK'"/>

          <p>{{this.zoneStatus}}</p>
        </div>

        <div class="paragraph">
          <p>Status Caserma</p>
          <p>{{this.barrackStatus}}</p>
        </div>

        <div class="paragraph">
          <p># Pluviometri</p>
          <p>{{this.numSensors}}</p>
        </div>

        <div class="paragraph">
          <p>Dati Parziali</p>
          <p>{{this.partialData}}</p>
        </div>

        <DividerComp />
        <div v-if="this.sensorsInfo.length>0" style="margin-bottom: 10px;">
          <div class="paragraph">
            <p style="font-weight: bold">ID</p>
            <p style="font-weight: bold">VALORE</p>
            <p style="font-weight: bold">LIMITE</p>
          </div>

          <div class="paragraph" v-for="s in this.sensorsInfo">
            <p>{{s["id"]}}</p>
            <p>{{s["value"]}}</p>
            <p>{{s["limit"]}}</p>
          </div>
        </div>

        <div v-else>
          <p>Valori dei sensori attualmente non disponibili</p>
        </div>

      </section>
    </div>
  </main>

</template>

<style lang="scss">
  div.alignment{
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .full{
    width: 100%;
  }
</style>