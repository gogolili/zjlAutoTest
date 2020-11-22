<!-- home -->
<template>
  <div id="myform">
    <el-container>
      <el-main>
        <div class="login-card-body">

        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="登录" name="login">
            <el-form ref="form" :model="form">
              <el-form-item style="text-align: left;">
                <el-radio  v-model="radio" label="1">LDAP</el-radio>
                <el-radio v-model="radio" label="2">普通登录</el-radio>
              </el-form-item>
              <el-form-item>
                <el-input prefix-icon="el-icon-user" v-model="form.username" placeholder="Username"></el-input>
              </el-form-item>
              <el-form-item>
                <el-input prefix-icon="el-icon-lock"  v-model="form.password" placeholder="Password"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="onLogin"  class="button-style">登录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form ref="form" :model="form">
              <el-form-item>
                <el-input prefix-icon="el-icon-user" v-model="form.username" placeholder="Username"></el-input>
              </el-form-item>
              <el-form-item>
                <el-input prefix-icon="el-icon-message" v-model="form.email" placeholder="Email"></el-input>
              </el-form-item>
              <el-form-item>
                <el-input prefix-icon="el-icon-lock" v-model="form.password" placeholder="Password"></el-input>
              </el-form-item>
              <el-form-item>
                <el-input prefix-icon="el-icon-lock"  v-model="form.confirmPassword" placeholder="Confirm Password"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="button-style">注册</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
        </div>
      </el-main>
       <el-footer><my-footer></my-footer></el-footer>
    </el-container>
  </div>
</template>
<script>
     import myFooter from '../components/common/Footer'
    export default {
        data () {
            return {
                radio: '1',
                activeName: 'login',
                form: {
                    username: '',
                    password: '',
                    email: '',
                    confirmPassword: ''
                }
            }
        },
        components: { myFooter },
        methods: {
            onLogin () {
                this.$router.push({ name: 'home', params: { username: this.$username, password: this.$password } })
                // this.$alert('3秒后跳转到首页...', '登录成功', {
                //     confirmButtonText: '确定',
                //     callback: action => {
                //         this.$message({
                //             type: 'info',
                //             message: `action: ${action}`
                //         })
                //     }
                // })
            }
        }
    }
</script>
<style>
  .login-card-body{
    margin:0 auto;
    width:33.33333333%;
  }
  .button-style{
    width:100%;
  }

</style>
