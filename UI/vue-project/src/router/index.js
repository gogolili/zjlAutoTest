import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

const Home = () => import('../page/Home.vue')
const About = () => import('../page/About.vue')
const Login = () => import('../page/Login.vue')
const ProjectIndex = () => import('../page/project/projectIndex.vue')
const Interface = () => import('../page/project/interface/Interface.vue')

export default new Router({
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/', // 默认主页
      name: 'login',
      component: Login
    },
    {
      path: '/about',
      name: 'about',
      component: About
    },
    {
      path: '/home',
      name: 'home',
      component: Home
    },
    {
      path: '/projectIndex',
      name: 'projectIndex',
      component: ProjectIndex
    },
    {
      path: '/projectIndex/interface',
      name: 'interface',
      component: Interface
    }
  ]
})
