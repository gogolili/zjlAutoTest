<template>
<div>
    <el-row :gutter="25">
       <el-col :span="4"><h2 class="interface-title">测试集合</h2></el-col>
       <el-col :span="20"><bannerCol></bannerCol></el-col>
    </el-row>
   <el-row class="interface-subtitle">output-api : 客户管理</el-row>
               <el-table :data="tableData" border style="width: 100%">
              <el-table-column prop="name" label="用例名称">
              </el-table-column>
              <el-table-column prop="key" label="Key">
              </el-table-column>
                <el-table-column prop="status" label="状态">
              </el-table-column>
              <el-table-column prop="path" label="接口路径">
              </el-table-column>
              <el-table-column prop="report" label="测试报告">
              </el-table-column>
            </el-table>
</div>
</template>

<script>
import bannerCol from './BannerCol'
export default {
components: {
    bannerCol
},
data () {
            return {
                tableData: [{
                    name: '查找客户信息',
                    key: '客户管理',
                    status: '未完成',
                    path: '/format/priview',
                    report: '未设置'
                }, {
                    name: '查找客户信息',
                    key: '客户管理',
                    status: '未完成',
                    path: '/format/priview',
                    report: '未设置'
                }, {
                    name: '查找客户信息',
                    key: '客户管理',
                    status: '未完成',
                    path: '/format/priview',
                    report: '未设置'
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
 .interface-subtitle {
    text-align: left;
    display: inline-block;
    padding-bottom: 20px;
    margin-left: 8px;
    width:100%;
}
</style>
