<template>
<el-container>
    <el-container class="container">
    <el-aside class="el-aside">
          <el-tabs v-model="activeName" type="border-card" @tab-click="handleClick">
    <el-tab-pane label="接口列表" name="first" :key="'first'"><asideInterfaceList></asideInterfaceList></el-tab-pane>
    <el-tab-pane label="测试集合" name="second" :key="'second'"><asideInterfaceCol></asideInterfaceCol></el-tab-pane>
  </el-tabs>
    </el-aside>
    <el-main class="el-main">
      <mainInterfaceList v-if="isFirst">></mainInterfaceList>
      <mainInterfaceCol v-if="isSecond"></mainInterfaceCol>
    </el-main>
  </el-container>
</el-container>

</template>

<script>
    import asideInterfaceCol from '../../../components/interface/AsideInterfaceCol'
    import asideInterfaceList from '../../../components/interface/AsideInterfaceList'
    import mainInterfaceCol from '../../../components/interface/MainInterfaceCol'
    import mainInterfaceList from '../../../components/interface/MainInterfaceList'
    export default {
    name: 'interface',
     data () {
      return {
        activeName: 'first',
         isFirst: true,
         isSecond: false
      }
    },

components: {
    asideInterfaceCol,
    asideInterfaceList,
    mainInterfaceCol,
    mainInterfaceList
},
methods: {
      handleClick (tab, event) {
        if (tab.name === 'first') {
        this.isFirst = true
        this.isSecond = false
      } else if (tab.name === 'second') {
        this.isFirst = false
        this.isSecond = true
      }
      }
    }
}
</script>

<style>
.el-aside {
    background-color: #D3DCE6;
  }
   .el-main {
    background-color: #E9EEF3;
  }
</style>
