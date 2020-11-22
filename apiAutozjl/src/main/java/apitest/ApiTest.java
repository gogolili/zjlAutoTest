package apitest;

import beans.ApiDataBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import configs.ApiConfig;
import listeners.AutoTestListener;
import listeners.RetryListener;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.annotations.Optional;
import utils.FileUtil;
import utils.RandomUtil;
import utils.ReportUtil;
import utils.SSLClient;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Listeners({ AutoTestListener.class, RetryListener.class })
public class ApiTest extends TestBase {

	/**
	 * api请求跟路径
	 */
	private static String rootUrl;

	/**
	 * 跟路径是否以‘/’结尾
	 */
	private static boolean rooUrlEndWithSlash = false;


	/**
	 * 是否使用form-data传参 会在post与put方法封装请求参数用到
	 */
	private static boolean requestByFormData = false;

	/**
	 * 配置
	 */
	private static ApiConfig apiConfig;

	/**
	 * 所有api测试用例数据
	 */
	protected List<ApiDataBean> dataList = new ArrayList<ApiDataBean>();

	private static HttpClient client;

	/**
	 * 初始化测试数据
	 *
	 * @throws Exception
	 */
	@Parameters("envName")
	@BeforeSuite
	public void init(@Optional("api-config.xml") String envName) throws Exception {
		String configFilePath = Paths.get(System.getProperty("user.dir"), envName).toString();
		ReportUtil.log("api config path:" + configFilePath);
		apiConfig = new ApiConfig(configFilePath);
		// 获取基础数据
		rootUrl = apiConfig.getRootUrl();
		rooUrlEndWithSlash = rootUrl.endsWith("/");

		// 读取 param，并将值保存到公共数据map
		Map<String, String> params = apiConfig.getParams();
		setSaveDates(params);
		client = new SSLClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); // 请求超时
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000); // 读取超时
	}

	@Parameters({"excelPath", "sheetName"})
	@BeforeTest
	public void readData(@Optional("case/api-data.xls") String excelPath, @Optional("Sheet1") String sheetName) throws DocumentException {
		dataList = readExcelData(ApiDataBean.class, excelPath.split(";"),
				sheetName.split(";"));
	}

	/**
	 * 过滤数据，run标记为Y的执行。
	 *
	 * @return
	 * @throws DocumentException
	 */
	@DataProvider(name = "apiDatas")
	public Iterator<Object[]> getApiData(ITestContext context)
			throws DocumentException {
		List<Object[]> dataProvider = new ArrayList<Object[]>();
		for (ApiDataBean data : dataList) {
			if (data.isRun()) {
				dataProvider.add(new Object[]{data});
			}
		}
		return dataProvider.iterator();
	}



	@Test(dataProvider = "apiDatas")
	public void apiTest(ApiDataBean apiDataBean) throws Exception {
		ReportUtil.log(String.format("【%s】 测试开始",apiDataBean.getDesc()));
		if (apiDataBean.getSleep() > 0) {
			// sleep休眠时间大于0的情况下进行暂停休眠
			ReportUtil.log(String.format("暂停休眠 %s 秒",	apiDataBean.getSleep()));
			Thread.sleep(apiDataBean.getSleep() * 1000);
		}

		Header[] apiHeader = buildRequestHeader(apiDataBean);


		String apiParam = buildRequestParam(apiDataBean);
		// 封装请求方法
		HttpUriRequest method = parseHttpRequest(apiDataBean.getUrl(),apiDataBean.getMethod(),apiHeader, apiParam);

		HttpResponse response = client.execute(method);
		HttpEntity respEntity = response.getEntity();
		String responseData = EntityUtils.toString(respEntity, "UTF-8");;
		ReportUtil.log("接口返回:" + responseData);
		// 验证预期信息
		verifyResult(responseData, apiDataBean.getVerify(),apiDataBean.isContains());
//============================================
		String allSave = getCommonParam(apiDataBean.getSave());
		String[] saves = allSave.split(";");
		String key, value;

		for (String save : saves) {
			Pattern pattern = Pattern.compile("([^;=]*)=([^;]*)");
			Matcher m = pattern.matcher(save.trim());
			while (m.find()) {
				key = m.group(1);// m.group(1)为等号前面的值，即要存储的key
				if (m.group(2).startsWith("$.Response.")) {
					value = JSONPath.read(responseData, "$." + m.group(2).substring(11)).toString();//m.group(2)为等号之后的值，即要提取的值
				} else if (m.group(2).startsWith("$.Request")) {
					value = JSONPath.read(apiParam, "$." + m.group(2).substring(10)).toString();

				} else {
					value = m.group(2);//如果没有找到$就将=后面的值全赋值
				}
				ReportUtil.log(String.format("存储公共参数   %s值为：%s.", key, value));
				saveDatas.put(key, value);
			}
		}
		//=================================================================


		ReportUtil.log("全局变量:" + saveDatas);
	}


	/**
	 * 格式化参数，如果是from-data格式则将参数封装到MultipartEntity否则封装到StringEntity
	 *
	 * @param param    参数
	 * @param formData 是否使用form-data格式
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private HttpEntity parseEntity(String param, boolean formData) throws UnsupportedEncodingException {
		if (formData) {
			Map<String, String> paramMap = JSON.parseObject(param,
					HashMap.class);
			MultipartEntity multiEntity = new MultipartEntity();
			for (String key : paramMap.keySet()) {
				String value = paramMap.get(key);
				Matcher m = funPattern.matcher(value);
				if (m.matches() && m.group(1).equals("bodyfile")) {
					value = m.group(2);
					multiEntity.addPart(key, new FileBody(new File(value)));
				} else {
					multiEntity.addPart(key, new StringBody(paramMap.get(key)));
				}
			}
			return multiEntity;
		} else {
			return new StringEntity(param, "UTF-8");
		}
	}


	/**
	 * 格式化url,替换路径参数等。
	 *
	 * @param shortUrl
	 * @return
	 */
	private String parseUrl(String shortUrl) {
		// 替换url中的参数
		shortUrl = getCommonParam(shortUrl);
		if (shortUrl.startsWith("http")) {
			return shortUrl;
		}
		if (rooUrlEndWithSlash == shortUrl.startsWith("/")) {
			if (rooUrlEndWithSlash) {
				shortUrl = shortUrl.replaceFirst("/", "");
			} else {
				shortUrl = "/" + shortUrl;
			}
		}
		return rootUrl + shortUrl;
	}

	//格式化参数
	private String buildRequestParam(ApiDataBean apiDataBean) {
		// 分析处理预参数 （函数生成的参数）
		String preParam = buildParam(apiDataBean.getPreParam());
		savePreParam(preParam);// 保存预存参数 用于后面接口参数中使用和接口返回验证中
		// 处理参数
		String apiParam = buildParam(apiDataBean.getParam());
		return apiParam;
	}

	//格式化header
	private Header[] buildRequestHeader(ApiDataBean apiDataBean) {
		List<Header> headers = new ArrayList<Header>();
		Header[] apiheader;

		String preHeader = buildParam(apiDataBean.getHeader());

		Map<String,String> headerMap =savePreheader(preHeader);

		headerMap.forEach((key, value) -> {
			Header header = new BasicHeader(key, value);
			headers.add(header);
		});

		apiheader = headers.toArray(new Header[headers.size()]);
		ReportUtil.log(String.format("存储header，值为：%s", apiheader));

		return apiheader;
	}


	/**
	 * 封装请求方法
	 *
	 * @param url    请求路径
	 * @param method 请求方法
	 * @param param  请求参数
	 * @return 请求方法
	 * @throws UnsupportedEncodingException
	 */
	private HttpUriRequest parseHttpRequest(String url, String method,Header[] header, String param) throws UnsupportedEncodingException {
		// 处理url
		url = parseUrl(url);
		ReportUtil.log("方法:" + method);
		ReportUtil.log("接口地址:" + url);
		ReportUtil.log("参数:" + param.replace("\r\n", "").replace("\n", ""));


		//upload表示上传，也是使用post进行请求
		if ("post".equalsIgnoreCase(method)) {
			// 封装post方法
			HttpPost postMethod = new HttpPost(url);
			postMethod.setHeaders(header);
			HttpEntity entity  = parseEntity(param,requestByFormData || "upload".equalsIgnoreCase(method));
			postMethod.setEntity(entity);
			return postMethod;
		} else if ("put".equalsIgnoreCase(method)) {
			// 封装put方法
			HttpPut putMethod = new HttpPut(url);
			putMethod.setHeaders(header);
			HttpEntity entity = parseEntity(param, requestByFormData);
			putMethod.setEntity(entity);
			return putMethod;
		} else if ("delete".equalsIgnoreCase(method)) {
			// 封装delete方法
			HttpDelete deleteMethod = new HttpDelete(url);
			deleteMethod.setHeaders(header);
			return deleteMethod;
		} else {
			// 封装get方法
			HttpGet getMethod = new HttpGet(url);
			getMethod.setHeaders(header);
			return getMethod;
		}
	}

}
