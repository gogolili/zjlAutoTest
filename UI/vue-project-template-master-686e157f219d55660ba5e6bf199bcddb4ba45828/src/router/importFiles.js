/**
 * Created by gaoguoqing on 2019/11/29.
 *
 */
module.exports = file => () => import('@/' + file + '.vue')
