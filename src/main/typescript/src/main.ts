import { createApp } from 'vue';
import App from './App.vue';
import PrimeVue from 'primevue/config';

import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';

import 'primeflex/primeflex.css';
import 'primeicons/primeicons.css';
import axios from "axios";

const app = createApp(App);

app.use(PrimeVue);

app.component('ButtonComp', Button);
app.component('DropDown', Dropdown);

app.mount("#app");