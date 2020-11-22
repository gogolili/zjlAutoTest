<template>
   <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
    <el-tab-pane label="项目列表" name="projectList" :key="'projectList'"><projectList></projectList></el-tab-pane>
    <el-tab-pane label="任务管理" name="task" :key="'task'"><task></task></el-tab-pane>
    <el-tab-pane label="执行器管理" name="executor" :key="'executor'"><executor></executor></el-tab-pane>
    <el-tab-pane label="报告" name="report" :key="'report'"><report></report></el-tab-pane>
  </el-tabs>

</template>

<script>
    import projectList from '../ProjectList'
    import task from '../Task'
    import executor from '../Executor'
    import report from '../Report'
    export default {
    data () {
            return {
                activeName: 'task'
            }
        },
    components: {
            projectList,
            task,
            executor,
            report
        }
}
</script>

<style>

</style>
