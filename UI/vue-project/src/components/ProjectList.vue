<template>
  <div>
    <div class="banner">
        销项2.0分组共4个项目
   <el-button type="primary" class="button-right">新建项目</el-button>
    </div>
    <el-divider></el-divider>
    <div>
<el-row class="button-left">
        <el-button class="button-left" icon="el-icon-notebook-2" type="primary" size="medium" circle @click="onGroupClick"></el-button>
        <el-button class="button-left" icon="el-icon-notebook-2" type="success" size="medium" circle @click="onGroupClick"></el-button>
        <el-button class="button-left" icon="el-icon-notebook-2" type="info" size="medium" circle @click="onGroupClick"></el-button>
        <el-button class="button-left" icon="el-icon-notebook-2" type="warning" size="medium" circle @click="onGroupClick"></el-button>
</el-row>
    </div>
  </div>
</template>
<script>
export default {
        methods: {
            onGroupClick () {
                this.$router.push({ name: 'projectIndex' })
            }
        }
}
</script>

<style>
.banner{
    text-align: left;
}
.button-left{
    text-align: left;
    line-height: 100px;
    font-size: 100px;
    /* font-weight: bold; */
}
.button-right{
    text-align: right;
}
</style>
