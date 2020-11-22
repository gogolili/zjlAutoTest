/**
 * Created by gaoguoqing on 7/2/20.
 *
 */
const prefix = 'b-' // 公用前缀，根据项目调整
export { prefix }
// 判断参数是否是其中之一
export function oneOf (value, validList) {
    for (let i = 0; i < validList.length; i++) {
        if (value === validList[i]) {
            return true
        }
    }
    return false
}
/**
 * propsInit
 * @param {Obj} params
 * params.props {Array} props name
 * params.config {Obj} props config
 */
export function propsInit (params) {
    let propsInitObj = {}
    for (let val of params.props) {
        propsInitObj[val] = params.config
    }
    return propsInitObj
}
