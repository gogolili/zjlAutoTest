<!-- home -->
<template>
<el-container>
  <el-header><my-header></my-header></el-header>
    <el-container class="container">
    <el-aside><my-aside></my-aside></el-aside>
    <el-main><my-main></my-main></el-main>
  </el-container>
  <el-footer><my-footer></my-footer></el-footer>
</el-container>
</template>
<script>

    import myHeader from '../components/common/Header'
     import myFooter from '../components/common/Footer'
    import myMain from '../components/myhome/MyMain'
    import myAside from '../components/myhome/Aside'
    export default {
        name: 'home',
        data () {
            return {

            }
        },
        components: {
            myHeader,
            myFooter,
            myMain,
            myAside

        },
        methods: {
            handleClick (tab, event) {
                console.log(tab, event)
            }
        }
    }
</script>
<style>
.button-right{float:right;margin-right: 20px;}
.container{
 padding: 25px;
}

</style>
