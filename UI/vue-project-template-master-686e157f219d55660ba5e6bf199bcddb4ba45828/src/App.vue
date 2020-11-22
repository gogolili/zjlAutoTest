<template>
    <div id="app">
        <div id="nav">
            <router-link to="/">Home</router-link>
            <router-link to="/about">About</router-link>
            <div>
                <BwButton>测试</BwButton>
            </div>
        </div>
        <router-view/>
    </div>
</template>
<script>
export default {
    name: 'app',
    data () {
        return {
            name: '',
            theme1: 'primary'
        }
    },
    mounted () {
        this.$http.get({
            url: '/sys/user/test',
            data: {
                name: 'test'
            }
        }, res => {
            if (res.success) {
                console.log('测试 yapi 数据：', res)
            }
        })
    }
}
</script>

<style lang="scss">
    @import "./assets/scss/reset.scss";

    #app {
        background: $white;
    }

    #nav {
        padding: 30px;
    }
</style>
