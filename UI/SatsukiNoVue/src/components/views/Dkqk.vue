<template>
	<el-container style="height: 500px; border: 1px solid #eee">
  <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
    <el-menu :default-openeds="['1', '3']">
      <el-submenu index="1">
        <template slot="title"><i class="el-icon-message"></i>zjl导航</template>
        <el-menu-item-group>
          <el-menu-item index="1-1">抵扣情况表</el-menu-item>
        </el-menu-item-group>
      </el-submenu>
    </el-menu>
  </el-aside>
  
  <el-container>
    <!-- <el-header style="text-align: right; font-size: 12px">
      <el-dropdown>
        <i class="el-icon-setting" style="margin-right: 15px"></i>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>查看</el-dropdown-item>
          <el-dropdown-item>新增</el-dropdown-item>
          <el-dropdown-item>删除</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <span>王小虎</span>
    </el-header> -->
    
    <el-main>
		<el-form :inline="true" :model="formInline" class="demo-form-inline">
			<el-form-item label="发票代码">
				<el-input v-model="formInline.invoiceCode" placeholder="发票代码"></el-input>
			</el-form-item>
			<el-form-item label="发票号码">
				<el-input v-model="formInline.invoiceNum" placeholder="发票号码"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="onSubmit">查询</el-button>
			</el-form-item>
		</el-form>
		
      <el-table :data="tableData">
        <el-table-column prop="fpdm" label="发票代码" width="140">
        </el-table-column>
        <el-table-column prop="fphm" label="发票号码" width="120">
        </el-table-column>
        <el-table-column prop="updated" label="取数状态">
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>
</el-container>
</template>


<style>
  .el-header {
    background-color: #B3C0D1;
    color: #333;
    line-height: 60px;
  }
  
  .el-aside {
    color: #333;
  }
</style>

<script>
import axios from 'axios'

  export default {
    data() {
      const item = {
        fpdm: '',
        fphm: '',
        updated: ''
      };
      return {
		tableData: [],
		formInline: {
          invoiceCode: '',
          invoiceNum: ''
        }
      }
    },
    methods: {
      onSubmit() {
		console.log('submit!');
		this.tableData=[];
		console.log('submit!',this.tableData);
		console.log('submit!',this.formInline);
		axios.post( '/api/queryJxDkqkPage', 
		{
			"pageLength":"1",
			"pageSize":"10",
			"fpdm": this.formInline.invoiceCode,
			"fphm":this.formInline.invoiceNum
		}).then((res) => {
			console.log('res', res)
			this.tableData=res.data.data.source
		})
      }
    }
  };
</script>