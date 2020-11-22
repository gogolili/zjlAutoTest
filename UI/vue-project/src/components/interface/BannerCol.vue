<template>
<div class="banner">
    <el-select class="my-select" v-model="value" placeholder="请选择测试用例运行环境">
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value">
    </el-option>
  </el-select>
   <el-button type="primary">服务端测试</el-button>
    <el-button type="primary">通用规则配置</el-button>
     <el-button type="primary">开始测试</el-button>
</div>
</template>

<script>
 export default {
    data () {
      return {
        options: [{
          value: '选项1',
          label: '测试环境：http：//100.0.3.2/'
        }, {
          value: '选项2',
          label: '预生产'
        }, {
          value: '选项3',
          label: '线上'
        }],
        value: ''
      }
    }
  }
</script>

<style>
.banner{
    padding-top:12.5px;
    text-align: right;
}
.my-select{
    padding-right: 10px;
    width:250px;
}
</style>
