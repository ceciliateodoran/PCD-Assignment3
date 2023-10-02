import { createApp } from 'vue';
import App from './App.vue';
import PrimeVue from 'primevue/config';
import router from "./router";
import mitt from 'mitt';

import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';
import Divider from 'primevue/divider';
import TabMenu from 'primevue/tabmenu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

import 'primeflex/primeflex.css';
import 'primeicons/primeicons.css';
import axios from "axios";
axios.default.withCredentials = true;

const emitter = mitt();
const app = createApp(App);
app.config.globalProperties.emitter = emitter;

app.use(PrimeVue);
app.use(router);

app.component('ButtonComp', Button);
app.component('DropDown', Dropdown);
app.component('ProgressComp', ProgressSpinner);
app.component('DividerComp', Divider);
app.component('MenuComp', TabMenu);
app.component('DataTableComp', DataTable);
app.component('ColumnComp', Column);

app.mount("#app");