<template>
    <div id="app">
  <el-container>
    <el-header><my-header></my-header></el-header>
       <router-view/>
    <el-footer><my-footer></my-footer></el-footer>
</el-container>
    </div>
</template>
<script>

import myHeader from '../src/components/common/Header'
import myFooter from '../src/components/common/Footer'

    export default {
        name: 'app',
        data () {
            return {
                name: ''
            }
        },
        components: {
    myHeader,
    myFooter },
        mounted () {
            this.$http.post({
                url: '/einvoicecorp/einvoice/corp/invoice/qrcode',
                data: {
                    name: 'test'
                }
            }, res => {
                if (res.success) {
                    console.log('测试 mock 数据：', res)
                }
            })
        }
    }
</script>
<style>
    #app {
        margin: 0 auto;
        text-align: center;
        color: #2c3e50;
        font-size: 16px;
    }
</style>
