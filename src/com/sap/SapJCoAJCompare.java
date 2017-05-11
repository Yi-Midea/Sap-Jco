package com.sap;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/*引入sapjco2.0的包*/ 
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Structure;
import com.sap.mw.jco.JCO.Table;

/*引入sapjco3.0的包*/
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
/*SapJco2与SapJco3的使用上的差异*/
public class SapJCoAJCompare {

	/* Jco2 连接 */
	private JCO.Client mConnection;
	/* Jco2 资源库 */
	private JCO.Repository mRepository;
	/* Jco3 目的地终点，类似Jco2连接 */
	private JCoDestination jcoDestination; 

	/**
	 * JCo2.0连接方法,建立与sap连接和获取sap资源库
	 * 
	 * @throws Exception
	 */
	private void getConnection() throws Exception {

		String sapclient = "810";
		String sapuserid = "AJ";
		String sappwd = "ahao1983";
		String sapurl = "100.100.80.90";
		String sapsysnum = "00";

		mConnection = JCO.createClient(sapclient, // SAP client
				sapuserid, // userid
				sappwd, // password
				null, // language
				sapurl, // application server host name
				sapsysnum); // system number
		mConnection.connect();
		// 获取资源库
		mRepository = new JCO.Repository("ARAsoft", mConnection);
	}

	/**
	 * JCo3.0连接方法，
	 * 
	 * @throws Exception
	 */
	private void getDestination() throws Exception {
		jcoDestination = JCoDestinationManager.getDestination("SAP_CONF");
	}

	/**
	 * JCo2.0通过模板获取sap函数 
	 * @param funcionName RFC函数名
	 * @return JCO.Function
	 * @throws Exception 异常
	 */
	private JCO.Function createFunction(String funcionName) throws Exception {

		if (funcionName.trim() == null) {
			throw new Exception("传入的sap函数名为空");
		}
		/* 判断资料库 */
		if (mRepository == null) {
			throw new Exception("mRepository为空,没有获取到sap资源库");
		}

		IFunctionTemplate ft = null;

		ft = mRepository.getFunctionTemplate(funcionName.toUpperCase());
		if (ft == null) {
			throw new Exception("sap函数为空");
		}
		return ft.getFunction();
	}

	/**
	 * JCo3.0通过sap模板获取函数  
	 * @param funcionName 函数名
	 * @return JCoFunction
	 * @throws Exception
	 */
	private JCoFunction getSapFunction(String funcionName) throws Exception {
		return jcoDestination.getRepository().getFunction(
				funcionName.toUpperCase()); 
	}

	/**
	 * JCo2.0获取sap单位表数据
	 * 
	 * @param name
	 *            函数名
	 * @return
	 * @throws Exception
	 */
	@Test
	public void getSapUomByJCo2() throws Exception {
		
		
		JSONArray array = new JSONArray();
		try {
			
			getConnection();
			JCO.Function function = createFunction("ZRFC_SCM_R_UOM_IMPORT"); 
			JCO.ParameterList list = function.getTableParameterList();
			JCO.Table et_output = list.getTable("ET_OUTPUT");
            mConnection.execute(function);
            
            if (et_output.getNumRows() == 0)
				return;
            
            
			while (et_output.nextRow()) {
				JSONObject jsonObj = new JSONObject(); 
			    jsonObj.put("MSEH3", et_output.getString("MSEH3"));
				jsonObj.put("MSEH6", et_output.getString("MSEH6"));
				jsonObj.put("MSEHI", et_output.getString("MSEHI"));
				jsonObj.put("MSEHL", et_output.getString("MSEHL"));
				jsonObj.put("MSEHT", et_output.getString("MSEHT")); 
				array.add(jsonObj);
			}

			System.out.println("Method:getSapUomByJCo2:"+array.toJSONString());
		} catch (Exception e) {
			throw new Exception("Sap excute function error :" + e.getMessage());
		} finally {
			/*JC02.0需要释放连接*/
			if (mConnection != null) {
				mConnection.disconnect();
			}
		}
	}

	/**
	 * JCo3.0获取sap单位表数据
	 * 
	 * @param name
	 *            函数名
	 * @return
	 * @throws Exception
	 */
	@Test
	public void getSapUomByJCo3() throws Exception {
		try {
			getDestination();
			JCoFunction function = getSapFunction("ZRFC_SCM_R_UOM_IMPORT"); 
            JCoParameterList list = function.getTableParameterList();
			JCoTable outPut = list.getTable("ET_OUTPUT");
            function.execute(jcoDestination);

            if (outPut.getNumRows() == 0)
				return;
            
            JSONArray array = new JSONArray();
			while (outPut.nextRow()) {
				JSONObject jsonObj = new JSONObject();
				// 遍历每一列获取列名
				outPut.forEach(column -> {
					String columnName = column.getName();
					String value = outPut.getString(columnName);
					jsonObj.put(columnName, value);
				});
				array.add(jsonObj);
			}

			System.out.println("Method:getSapUomByJCo3:"+array.toJSONString());
       } catch (Exception e) {
			throw new Exception("Sap excute function error :" + e.getMessage());
		} 
	}
	
	/**
	 * JCo3.0获取sap供应商表数据
	 * 
	 * @param name
	 *            函数名
	 * @return
	 * @throws Exception
	 */
	@Test
	public void getSapVendorByJC02AndJCo3() throws Exception {
		try {
			JSONArray array = new JSONArray();
			/*JCo2.0连接*/
			getConnection();
			/*JCo3.0连接*/
			getDestination();
			
			/*JCo2.0 获取sap函数*/ 
			JCO.Function functionJCo2 = createFunction("ZRFC_SCM_MM_R_VENDOR_IMPORT"); 
			/*JCo3.0 获取sap函数*/ 
			JCoFunction functionJCo3 = getSapFunction("ZRFC_SCM_MM_R_VENDOR_IMPORT"); 
			
			/*JCo2.0 设置ParameterList*/ 
			JCO.ParameterList listJCo2 = functionJCo2.getTableParameterList();
			/*JCo3.0 设置ParameterList*/ 
			JCoParameterList list = functionJCo3.getTableParameterList();
			
			/*JCo2.0 设置输入Table*/ 
			JCO.Table it_ekorg = listJCo2.getTable("IT_EKORG");
			it_ekorg.appendRow();
			it_ekorg.setRow(0);
			it_ekorg.setValue("I", "SIGN");
			it_ekorg.setValue("EQ", "OPTION");
			it_ekorg.setValue("5500", "LOW"); 
			/*JCo2.0 设置Table*/ 
			
			JCO.Table et_outputJCo2 = listJCo2.getTable("ET_OUTPUT");
            mConnection.execute(functionJCo2);
            if (et_outputJCo2.getNumRows() == 0)
				return;
            while (et_outputJCo2.nextRow()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("编码", et_outputJCo2.getString("LIFNR")); 
				jsonObj.put("名称", et_outputJCo2.getString("NAME1")); 
				jsonObj.put("币种", et_outputJCo2.getString("WAERS"));  
				jsonObj.put("联系方式", et_outputJCo2.getString("TELF1")); 
				jsonObj.put("联系地址 ", et_outputJCo2.getString("STREET")); 
				jsonObj.put("采购组织", et_outputJCo2.getString("EKORG")); 
				jsonObj.put("邮件", et_outputJCo2.getString("SMTP_ADDR")); 
				array.add(jsonObj);
			}

			System.out.println("getSapVendorByJC02:"+array.toJSONString());
			
            mConnection.disconnect();
            
			
			
            
            JCoTable outPut = list.getTable("ET_OUTPUT");
            JCO.Table itEkorg = listJCo2.getTable("IT_EKORG");
            itEkorg.appendRow();
            itEkorg.setRow(0);
            it_ekorg.setValue("I", "SIGN");
			it_ekorg.setValue("EQ", "OPTION");
			it_ekorg.setValue("5500", "LOW");  
			functionJCo3.execute(jcoDestination);

            if (outPut.getNumRows() == 0)
				return;
            
            array = new JSONArray();
			while (outPut.nextRow()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("编码", outPut.getString("LIFNR")); 
				jsonObj.put("名称", outPut.getString("NAME1")); 
				jsonObj.put("币种", outPut.getString("WAERS"));  
				jsonObj.put("联系方式", outPut.getString("TELF1")); 
				jsonObj.put("联系地址 ", outPut.getString("STREET")); 
				jsonObj.put("采购组织", outPut.getString("EKORG")); 
				jsonObj.put("邮件", outPut.getString("SMTP_ADDR")); 
				array.add(jsonObj);
			}

			System.out.println("getSapVendorByJCo3:"+array.toJSONString());
       } catch (Exception e) {
			throw new Exception("Sap excute function error :" + e.getMessage());
		} 
	}

	
	@Before
	public void before() { 
		System.out.println("方法开始:"+new Date());
	}

	@After
	public void after() {
		System.out.println("方法结束:"+new Date());
	}
}
