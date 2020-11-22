/**
 * Created by gaoguoqing on 2019/12/24.
 *
 */
function AfterBuildCb () {
    setTimeout(() => {
        process.stdout.write('fe project build success')
    }, 100)
}
class AfterBuildCbPlugin {
    apply (compiler) {
        compiler.plugin('done', AfterBuildCb)
    }
}
module.exports = AfterBuildCbPlugin

