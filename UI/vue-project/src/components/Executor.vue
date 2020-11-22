<template>
    <!-- children -->
    <div class="second">
      <div>
        <div align="right" style="margin-bottom:10px;">
          <el-button type="primary" @click="onNewTask">新建执行器</el-button>
        </div>
        <div>
          <template>
            <el-table :data="tableData" border style="width: 100%">
              <el-table-column prop="name" label="执行器名称">
              </el-table-column>
              <el-table-column prop="taskDesc" label="任务描述">
              </el-table-column>
              <el-table-column prop="failHandlePolicy" label="失败处理策略">
              </el-table-column>
              <el-table-column prop="status" label="状态">
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间">
              </el-table-column>
              <el-table-column prop="responsible" label="负责人">
              </el-table-column>
              <el-table-column label="操作" width="300" fixed="right">
                <template slot-scope="scope">
                  <el-button type="primary" size="mini" @click.native.prevent="editRow(scope.$index,tableData)">编辑</el-button>
                  <el-button type="primary" size="mini" @click.native.prevent="deleteRow(scope.$index,tableData)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </div>
        <div>
          <el-dialog title="新建执行器" :visible.sync="dialogFormVisible">
            <el-divider></el-divider>
            <el-form :model="form">
              <el-form-item label="执行器名称：" :label-width="formLabelWidth">
                <el-input v-model="form.name" autocomplete="off" placeholder="请输入执行器名称"></el-input>
              </el-form-item>
              <el-form-item label="任务描述：" :label-width="formLabelWidth">
               <el-input v-model="form.name" autocomplete="off" placeholder="请输入任务描述"></el-input>
              </el-form-item>
              <el-form-item label="失败处理策略：" :label-width="formLabelWidth" align="left">
                <el-select v-model="form.crontab" placeholder="请选择失败策略">
                  <el-option label="1" value="shanghai"></el-option>
                  <el-option label="2" value="beijing"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="报警邮件：" :label-width="formLabelWidth" align="left">
                <el-select v-model="form.jenkins" placeholder="请输入邮箱地址">
                  <el-option label="job01" value="shanghai"></el-option>
                  <el-option label="job02" value="beijing"></el-option>
                </el-select>
              </el-form-item>
                <el-form-item label="钉钉Token：" :label-width="formLabelWidth">
                <el-input v-model="form.name" autocomplete="off" placeholder="请输入钉钉Token"></el-input>
              </el-form-item>
              <el-form-item label="负责人：" :label-width="formLabelWidth">
               <el-input v-model="form.name" autocomplete="off" placeholder="请输入负责人"></el-input>
              </el-form-item>
            </el-form>
             <el-divider></el-divider>
            <div slot="footer" class="dialog-footer">
              <el-checkbox label="创建另一个" name="createAnother"></el-checkbox>
              <el-button @click="dialogFormVisible = false">取 消</el-button>
              <el-button type="primary" @click="dialogFormVisible = false">确 定</el-button>
            </div>
          </el-dialog>
        </div>
        <div class="block" align="right" style="margin-top:10px;">
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage4"
                         :page-sizes="[10, 20, 30, 40]" :page-size="10" layout="total, sizes, prev, pager, next, jumper" :total="40">
          </el-pagination>
        </div>
      </div>
    </div>
</template>
<script>
    export default {
        name: 'task',
        data () {
            return {
                tableData: [{
                    name: '扩展字段测试用例执行器',
                    taskDesc: '测试环境测试用例',
                    failHandlePolicy: '重试3次',
                    status: 'success',
                    createTime: Date.now(),
                    responsible: '李振华'

                }, {
                    name: '扩展字段测试用例执行器',
                    taskDesc: '测试环境测试用例',
                    failHandlePolicy: '重试3次',
                    status: 'success',
                    createTime: Date.now(),
                    responsible: '李振华'
                }, {
                    name: '扩展字段测试用例执行器',
                    taskDesc: '测试环境测试用例',
                    failHandlePolicy: '重试3次',
                    status: 'success',
                    createTime: Date.now(),
                    responsible: '李振华'
                }],
                currentPage1: 5,
                currentPage2: 5,
                currentPage3: 5,
                currentPage4: 4,
                dialogFormVisible: false,
                form: {
                    name: '',
                    enviroments: [],
                    crontab: '',
                    jenkins: '',
                    delivery: false,
                    resource: '',
                    desc: ''
                },
                formLabelWidth: '120px'
            }
        },
        methods: {
            onNewTask () {
                this.$data.dialogFormVisible = true
            },
            handleSizeChange (val) {
                console.log(`每页 ${val} 条`)
            },
            handleCurrentChange (val) {
                console.log(`当前页: ${val}`)
            },
            executeRow (index, rows) {
                console.log(index, rows.index)
            },
            pauseRow (index, rows) {
                console.log(index, rows.index)
            },
            editRow (index, rows) {
                console.log(index, rows.index)
            },
            deleteRow (index, rows) {
                console.log('ok')
                rows.splice(index, 1)
            }
        }
    }
</script>
