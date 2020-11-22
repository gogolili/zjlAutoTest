/**
 * Created by gaoguoqing on 2019/6/28.
 *
 */
import axios from 'axios'

const BASE_URL = process.env.VUE_APP_BASIC_URL // 全局变量
const TIME_OUT_MS = 60 * 1000 // 默认请求超时时间
/*
 * @param response 返回数据列表
 */
function handleResults (response) {
    return response.data
}

function handleUrl (url) {
    url = url.startsWith('/') ? url : ('/' + url)
    return url.indexOf('http') > 0 ? url : (BASE_URL + url)
}

/*
 * @param data 参数列表
 * @return
 */
function handleParams (data) {
    return data
}
/*
 * @param type
 * @param options
 *        url
 *        data
 *        timeout
 * @param response 请求成功时的回调函数
 * @param exception 异常的回调函数
 */
function axiosRequest (type, options, response, exception) {
    let axiosOptions = {
        method: type,
        url: handleUrl(options.url),
        data: handleParams(options.data),
        timeout: options.timeout || TIME_OUT_MS,
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        }
    }
    if (type === 'post') axiosOptions.data = handleParams(options.data)
    axios(axiosOptions).then((result) => {
        response(handleResults(result, options))
    }).catch((error) => {
        if (axios.isCancel(error)) return new Promise(() => {})
        if (exception) {
            exception(error)
        } else {
            console.log(error)
        }
    })
}

function post (options, response, exception) {
    axiosRequest('post', options, response, exception)
}
function get (options, response, exception) {
    axiosRequest('get', options, response, exception)
}

export default {
    post,
    get,
    /*
     * 导入文件
     * @param url
     * @param data
     * @param response 请求成功时的回调函数
     * @param exception 异常的回调函数
     */
    uploadFile (url, data, response, exception) {
        axios({
            method: 'post',
            url: handleUrl(url),
            data: handleParams(data),
            dataType: 'json',
            processData: false,
            contentType: false
        }).then(
            (result) => {
                response(handleResults(result))
            }
        ).catch(
            (error) => {
                if (exception) {
                    exception(error)
                } else {
                    console.log(error)
                }
            }
        )
    },
    /*
     * 下载文件用，导出 Excel 表格可以用这个方法
     * @param url
     * @param param
     * @param fileName 如果是导出 Excel 表格文件名后缀最好用.xls 而不是.xlsx，否则文件可能会因为格式错误导致无法打开
     * @param exception 异常的回调函数
     */
    downloadFile (url, data, fileName, exception) {
        axios({
            method: 'post',
            url: handleUrl(url),
            data: handleParams(data),
            responseType: 'blob'
        }).then(
            (result) => {
                const excelBlob = result.data
                if ('msSaveOrOpenBlob' in navigator) {
                    // Microsoft Edge and Microsoft Internet Explorer 10-11
                    window.navigator.msSaveOrOpenBlob(excelBlob, fileName)
                } else {
                    const elink = document.createElement('a')
                    elink.download = fileName
                    elink.style.display = 'none'
                    const blob = new Blob([excelBlob])
                    elink.href = URL.createObjectURL(blob)
                    document.body.appendChild(elink)
                    elink.click()
                    document.body.removeChild(elink)
                }
            }
        ).catch(
            (error) => {
                if (exception) {
                    exception(error)
                } else {
                    console.log(error)
                }
            }
        )
    }
}
