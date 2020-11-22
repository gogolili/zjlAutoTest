<template>
<div>
    <div>
   <el-row :gutter="70">
     <el-col :span="14" class="project-title"><h2>我的主页</h2></el-col>
     <el-col :span="10"><h5><el-button type="primary" size="small">添加分组</el-button></h5></el-col>
  </el-row>
  <el-row>
      <el-col :span="5">简介:</el-col>
  </el-row>
  <el-row><el-input placeholder="搜索分类"></el-input></el-row>
    </div>
    <div>
<el-menu>
<el-menu-item index="0">
<i class="el-icon-location"></i>
<span slot="title">我的主页</span>
</el-menu-item>
<el-menu-item index="1">
<i class="el-icon-menu"></i>
<span slot="title">销项2.0</span>
</el-menu-item>
</el-menu>
    </div>
</div>
</template>

<script>
export default {

}
</script>

<style>
.project-title{
text-align:left;
}
</style>
