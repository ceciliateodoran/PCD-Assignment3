<script lang="ts">
import axios from "axios";
import { defineComponent } from 'vue';
import {Environment} from "../environment.ts";

export default defineComponent({
  name: 'CanvasGrid',
  data() {
    return {
      timer: null,
      gridColor: '#000000',
      gridHeight: null,
      gridWidth: null,
      canvas: null,
      ctx: null,
      numRows: null,
      numCols: null,
      sensorsCoords: [],
      zonesCoords: [],
      offset: 3,
    };
  },
  beforeMount() {
    this.cityDimensions();
  },
  mounted() {
    this.timer = setInterval(() => {
      this.sensorsCoordinates();
    }, 3000);
  },
  methods: {
    cityDimensions(){
      //get all zones of the city
      axios
          .get(`${Environment.APPLICATION_BACKEND_HOST}/citysize`)
          .then((response) => {
            this.numRows = response.data.citysize.rows;
            this.numCols = response.data.citysize.columns;
            this.gridHeight = response.data.citysize.height * this.offset;
            this.gridWidth = response.data.citysize.width * this.offset;
          })
          .catch((error) => {
            console.log(error);
          });
    },
    sensorsCoordinates(){
      if(this.gridWidth===null&&this.gridHeight===null&&this.sensorsCoords.length<=0){
        this.cityDimensions();
      }else{
        if(this.sensorsCoords.length<=0){
          //get all sensors coordinates
          axios
              .get(`${Environment.APPLICATION_BACKEND_HOST}/sensorscoords`)
              .then((response) => {
                response.data.sensorscoords.forEach((coord: any) => {
                  let coordinates = coord.split("|");
                  this.sensorsCoords.push({
                    zone: `${coordinates[0]}`,
                    id: `${coordinates[1].replace("Sensor","")}`,
                    x: `${coordinates[2] * this.offset}`,
                    y: `${coordinates[3] * this.offset}`,
                  });
                });
                this.generateCanvas();
                this.drawZones(this.gridWidth, this.gridHeight, this.numRows, this.numCols);
              })
              .catch((error) => {
                console.log(error);
              });
        }
      }
    },
    generateCanvas() {
      this.canvas = document.getElementById('canvas');
      this.ctx = this.canvas.getContext('2d');
    },
    drawZones(width, height, rows, columns) {
      //render of zones in canvas
      this.ctx.beginPath();
      let numCols = columns == 0 ? 1 : columns;
      let columnSize = Math.trunc((width/numCols));
      for (var x = 0; x <= width; x += columnSize) {
        this.ctx.moveTo(x, 0);
        this.ctx.lineTo(x, height);
      }
      this.ctx.strokeStyle = this.gridColor;
      this.ctx.lineWidth = 1;
      this.ctx.stroke();

      this.ctx.beginPath();
      let numRows = rows == 0 ? 1 : rows;
      let rowSize = Math.trunc((height/numRows));
      for (var y = 0; y <= height; y += rowSize) {
        this.ctx.moveTo(0, y);
        this.ctx.lineTo(width, y);
      }
      this.ctx.lineWidth = 1;
      this.ctx.stroke();

      this.ctx.font = "18px Arial";
      this.ctx.fillStyle = "black";

      let numberZone = 0;
      for (var y = 0; y <= height-rowSize; y += rowSize) {
        for (var x = 0; x <= width-columnSize; x += columnSize) {
          this.ctx.fillText("Zona"+numberZone,x+10, y+30);
          numberZone++;
        }
      }

      this.drawSensors();
    },
    drawSensors(){
      //render of sensors in canvas
      let radius = 7;

      this.sensorsCoords.forEach((coord: any) => {
        this.ctx.beginPath();

        this.ctx.arc(coord["x"], coord["y"], radius, 0, 2 * Math.PI, false);
        this.ctx.fillStyle = "blue";
        this.ctx.fill();
        this.ctx.strokeStyle = "blue";
        this.ctx.stroke();
        this.ctx.font = "24px Arial";
        this.ctx.fillStyle = "red";

        this.ctx.fillText(coord["zone"]+"-"+coord["id"],coord["x"]-10, coord["y"]-10);
      });
    },
  },
});
</script>

<template>
  <div v-if="gridWidth===null&&gridHeight===null" class="card flex justify-content-center">
    <ProgressComp />
  </div>
  <canvas v-else
          id="canvas"
          :width="gridWidth"
          :height="gridHeight"></canvas>
</template>