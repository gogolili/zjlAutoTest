<template>
  <el-menu router :default-active="$route.path"  class="el-menu-demo" mode="horizontal" @select="handleSelect">
  <el-menu-item index="/projectIndex/interface">接口</el-menu-item>
  <el-menu-item index="/projectIndex/settings">设置</el-menu-item>
  </el-menu>
</template>

<script>
export default {
    name: 'mybanner',
    data () {
      return {
        // activeIndex: '1'
      }
    },
    methods: {
      handleSelect (key, keyPath) {
        console.log(key, keyPath)
      }
    }
  }
</script>

<style>

</style>
