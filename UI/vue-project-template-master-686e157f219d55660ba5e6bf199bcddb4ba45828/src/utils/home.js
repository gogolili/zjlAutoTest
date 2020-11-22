import { deleteProtocol } from '@/utils/service/common'

function addPrefix (img) {
    let resourcePrefix = localStorage.getItem('resourcePrefix') || ''
    if (img && resourcePrefix) { // 根据项目调整路径
        return `//static3.baiwang.com/static-resource/${resourcePrefix}/mainsite-mobile/img/${img}`
    }
}
function imgPath (imgurl, options) {
    let img = deleteProtocol(imgurl)
    if (options && options.isCdn) {
        img = addPrefix(img)
    }
    const sorce = ['//', 'data:']
    let imgData = ''
    let isLocal = true
    let defaultImg = require('@/assets/logo.png')
    sorce.forEach((item) => {
        if (img && img.length > 0 && img.indexOf(item) === 0) {
            isLocal = false
        }
    })
    try {
        isLocal ? imgData = require('@/assets/img/' + img) : imgData = img
    } catch (error) {
        console.log(error, 'imgerror')
        return defaultImg
    }
    if (imgData) {
        return imgData
    } else {
        return defaultImg
    }
}
/**
 * @description: 获取图片地址
 * @param {imgurl} string
 * @param {type} string
 * @return:  本地或远程图片
 */
export function getImg (imgurl, options) {
    return imgPath(imgurl, options)
}
export function getCdnImg (imgurl) {
    return imgPath(imgurl, { isCdn: true })
}
export function toPath (that, path) {
    if (path === '' || path === undefined || that.$route.path === path) return false
    if (typeof path === 'object') {
        that.$router.push(path)
        return
    }
    if (path.indexOf('http') !== -1 || path.indexOf('https') !== -1) {
        // app内置浏览器不兼容处理
        var ua = navigator.userAgent.toLowerCase()
        let pdf = path && path.slice(path.length - 3)
        let def = ua.match(/DingTalk/i) === 'dingtalk' || ua.match(/MicroMessenger/i) === 'micromessenger'
        if (pdf === 'pdf' && def) {
            that.$Modal('请跳转至默认浏览器中下载文件')
            return false
        }
        window.open(path, '_blank')
        return false
    }
    console.log(path)
    that.$router.push(path)
}
