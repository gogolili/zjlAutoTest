/**
 * vue-cli3的全局配置文件 详情见https://cli.vuejs.org/zh/config/#%E5%85%A8%E5%B1%80-cli-%E9%85%8D%E7%BD%AE
 * Created by gaoguoqing on 2019/6/27.
 *
 */
const isProduction = process.env.NODE_ENV === 'production'
const path = require('path')
function resolve (dir) {
    return path.join(__dirname, dir)
}

module.exports = {
    publicPath: isProduction ? './' : '/',
    devServer: {
        hot:true,
        overlay: { //Show eslink error information on Browser
            warnings: true,
            errors: true
        },
        open: true,
        proxy: {
            '/login': {
                target: 'http://yapi.51baiwang.com:3000/mock/74', //代理地址地址
                changeOrigin: true,
                pathRewrite: {
                    '^/login': '/'
                }
            }
        },
        before (app) {
            if (!isProduction) {
                let mockPlugin = require('./mock')
                app.use(mockPlugin({
                    mockConf: './mock/mock.config.js'
                }))
            }
        }
    },
    productionSourceMap: false,
    configureWebpack: config => {
        config.resolve = {
            extensions: ['.js', '.vue', '.json', '.css'],
            alias: {
                'vue$': 'vue/dist/vue.esm.js',
                '@': resolve('src')
            }
        }
    }
}
