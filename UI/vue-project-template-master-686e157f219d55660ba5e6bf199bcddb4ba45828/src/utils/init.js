/**
 * Created by gaoguoqing on 2019/11/29.
 *
 */
import http from './service/https'
import BwButton from '@/components/Button/index'
import { getImg } from '@/utils/home.js'

function init (Vue) {
    Vue.prototype.$http = http
    Vue.prototype.$getImg = getImg
    Vue.component('BwButton', BwButton)
}
export default init
