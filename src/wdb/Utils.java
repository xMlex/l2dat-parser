package wdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Utils {

	public static boolean downloadFile(String url,String save){
		
		CloseableHttpClient hc = HttpClients.custom().build();
		HttpGet get;
		
		try {
			get = new HttpGet(url);
			File file = new File(save);
			if(!file.exists())			
			    file.createNewFile();									
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			
			CloseableHttpResponse response = hc.execute(get);
			try {
				fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
	            fileOutputStream.close();
			} finally {
				response.close();
			}			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				hc.close();
			} catch (IOException e) {}
		}		
		return false;
	}
	
}
