<template>
<div class="parent">
<div class="title">销项2.0的测试报告</div>
<div class="block">
   <span class="demonstration">开始时间：</span>
   <el-date-picker v-model="value1"  align="right" type="date" placeholder="选择日期" :picker-options="pickerOptions"></el-date-picker>
   <span class="demonstration">结束时间：</span>
   <el-date-picker v-model="value2"  align="right" type="date" placeholder="选择日期" :picker-options="pickerOptions"></el-date-picker>
   <el-button type="primary">搜索</el-button>
</div>
</div>
</template>

<script>
 export default {
    data () {
      return {
        pickerOptions: {
          disabledDate (time) {
            return time.getTime() > Date.now()
          },
          shortcuts: [{
            text: '今天',
            onClick (picker) {
              picker.$emit('pick', new Date())
            }
          }]
        },
        value1: '',
        value2: ''
      }
    }
  }
</script>

<style>
.title{
    line-height: 60px;
    /* font-weight: 100px; */
    font-size: 30px;
}
.parent{
    text-align: left;
}

</style>
