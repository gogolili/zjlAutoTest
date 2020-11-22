/**
 * Created by gaoguoqing on 7/2/20.
 *
 */
function deleteProtocol (url) {
    return url && url.replace(/(http:|https:){0,6}/, '')
}
export {
    deleteProtocol
}
