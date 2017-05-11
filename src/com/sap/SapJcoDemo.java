package com.sap; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class SapJcoDemo
{
	/*sap 配置信息*/
    private Properties setProperties()
    {        
        // logon parameters and other properties
        Properties connProps = new Properties();
        connProps.setProperty(DestinationDataProvider.JCO_ASHOST, "100.100.80.90");
        connProps.setProperty(DestinationDataProvider.JCO_SYSNR, "00");
        connProps.setProperty(DestinationDataProvider.JCO_USER, "AJ");
        connProps.setProperty(DestinationDataProvider.JCO_PASSWD, "ahao1983");
        connProps.setProperty(DestinationDataProvider.JCO_CLIENT, "810");
        connProps.setProperty(DestinationDataProvider.JCO_LANG, "EN");
        return connProps;
    }

    /*创建sap jco3.0配置文件通用方法*/
    private void doCreateFile(String fName, String suffix, Properties props) throws IOException
    {
        /**
         * Write contents of properties into a text file
         * which was named [fName+suffix.jcodestination]
         */

        File cfg = new File(fName+"."+suffix);
        if (!cfg.exists()){ // file not exists
            // Create file output stream, not using append mode
            FileOutputStream fOutputStream = new FileOutputStream(cfg, false); 

            // store the properties in file output stream
            // and also add comments
            props.store(fOutputStream, "SAP logon parameters:"); 

            fOutputStream.close();
        }else{
            throw new RuntimeException("File alreay exists.");
        }
    }

    /*配置文件生成方法*/
    //@Test 
    public void createConfigFile() throws IOException
    {        
        Properties props = this.setProperties();
        String fileName = "SAP_CONF"; // sap application server

        // jcodestination suffix is required by JCoDestinationManager
        this.doCreateFile(fileName, "jcodestination", props);
    }
    
    
    @Test
    public void doTest() {

		JSONArray array = new JSONArray();
		try {
			JCoDestination destination = JCoDestinationManager
					.getDestination("SAP_CONF");

			JCoFunction sapFunction = destination.getRepository().getFunction(
					"ZRFC_SCM_R_UOM_IMPORT");

			/*
			 * JCoParameterList input = sapFunction.getImportParameterList();
			 * input.setValue("IV_FLAG","X");
			 * JCoStructure input.getStructure("IS_CONT_HEADER")
			 * JCoStructure input.getStructure("IS_CONT_HEADER")
			 */
			sapFunction.execute(destination);
			JCoParameterList list = sapFunction.getTableParameterList();

			JCoTable outPut = list.getTable("ET_OUTPUT");

			if (outPut.getNumRows() == 0)
				return;
			
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

			System.out.println("Function doTest:"+array.toJSONString());

		} catch (JCoException je) {
			je.printStackTrace();
		}

	}

}