/**
 * Created by gaoguoqing on 2019/6/28.
 *
 */
import http from './https'

function init (Vue) {
  Vue.prototype.$http = http
}
export default init
