import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../pages/Home.vue'

const importAsync = require('./importFiles')

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'home',
        component: Home
    },
    {
        path: '/about',
        name: 'about',
        component: importAsync('pages/About')
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

export default router
