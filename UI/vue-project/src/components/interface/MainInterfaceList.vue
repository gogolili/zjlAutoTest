<template>
<div>
   <el-row :gutter="80">
       <el-col :span="15"><h2 class="interface-title">全部接口共（52）个</h2></el-col>
       <el-col :span="5"><h5><el-button type="primary">添加接口</el-button></h5></el-col>
   </el-row>
               <el-table :data="tableData" border style="width: 100%">
              <el-table-column prop="name" label="接口名称">
              </el-table-column>
              <el-table-column prop="path" label="接口路径">
              </el-table-column>
              <el-table-column prop="classify" label="接口分类">
              </el-table-column>
              <el-table-column prop="status" label="状态">
              </el-table-column>
              <el-table-column prop="tag" label="tag">
              </el-table-column>
            </el-table>
</div>
</template>

<script>
export default {
data () {
            return {
                tableData: [{
                    name: 'preview',
                    path: '/format/priview',
                    classify: '客户管理',
                    status: '未完成',
                    tag: '未设置'
                }, {
                    name: 'preview',
                    path: '/format/priview',
                    classify: '客户管理',
                    status: '未完成',
                    tag: '未设置'
                }, {
                    name: 'preview',
                    path: '/format/priview',
                    classify: '客户管理',
                    status: '未完成',
                    tag: '未设置'
                }
                ]
                }
}
}
</script>

<style scope>
.interface-title {
    clear: both;
    font-weight: 400;
    border-left: 3px solid #2395f1;
    text-align: left;
    margin-left:20px;
    padding-left:8px;
    display: inline-block;

}
</style>
