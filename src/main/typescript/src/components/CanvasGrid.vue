<template>
  <canvas
    id="canvas"
    :width="gridWidth"
    :height="gridHeight"></canvas>
</template>

<script>
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'CanvasGrid',
  props: {
    gridWidth: {
      type: Number,
      default: 600,
    },
    gridHeight: {
      type: Number,
      default: 300,
    },
    gridColor: {
      type: String,
      default: '#000000',
    },
    numRows: {
      type: Number,
      default: 4,
    },
    numCols: {
      type: Number,
      default: 2,
    },
    cellSize: {
      type: Number,
      default: 20,
    },
  },
  data() {
    return {
      canvas: null,
      ctx: null,
      isBusy: false,
    };
  },
  mounted() {
    this.generateCanvas();
    this.generateGrid(this.gridWidth, this.gridHeight, this.numRows, this.numCols);
    //this.calculateGridSizes(this.gridWidth, this.gridHeight, this.cellSize);
  },
  methods: {
    generateCanvas() {
      this.canvas = document.getElementById('canvas');
      this.ctx = this.canvas.getContext('2d');
    },
    generateGrid(width, height, rows, columns) {
      this.ctx.beginPath();
      let columnSize = width/columns;
      for (var x = 0; x <= width; x += columnSize) {
        this.ctx.moveTo(x, 0);
        this.ctx.lineTo(x, height);
      }
      this.ctx.strokeStyle = this.gridColor;
      this.ctx.lineWidth = 1;
      this.ctx.stroke();
      this.ctx.beginPath();
      let rowSize = height/rows;
      for (var y = 0; y <= height; y += rowSize) {
        this.ctx.moveTo(0, y);
        this.ctx.lineTo(width, y);
      }
      this.ctx.lineWidth = 1;
      this.ctx.stroke();
    },
    getCellId(x, y) {
      return `${Math.floor(y / this.cellSize)}-${Math.floor(
        x / this.cellSize
      )}`;
    },
    calculateGridSizes(width, height, cellSize) {
      let xNodes, yNodes;
      if (!xNodes) {
        xNodes = Math.floor(width / cellSize);
      }

      if (!yNodes) {
        yNodes = Math.floor(height / cellSize);
      }

      for (let y = 0; y < yNodes; y++) {
        for (let x = 0; x < xNodes; x++) {
          this.cells[`${y}-${x}`] = {
            y,
            x,
            size: cellSize,
            color: null,
          };
        }
      }
    },
  },
});
</script>
