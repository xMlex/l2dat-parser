package wdb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
	
	public static String clearIcon(String s){
		return s.toLowerCase().replaceAll("branchsys.icon.", "").replaceAll("branchsys2.icon2.", "").
				replaceAll("branchsys2.icon.", "").replaceAll("br_cashtex.item.", "")
				.replaceAll("icon.", "").replaceAll("br_cashtex.item.", "").
				replaceAll("branchsys.", "").replaceAll("branchsys2.", "");
	}
	public static String clearStr(String s){
		return s.replaceAll("a,", "").replace("\\"+"0", "");
	}
	
	public static void validFileIcon(String icon){
		icon = clearIcon(icon);
		File f = new File("./data/icons/"+icon+".png");
		
		if(!f.exists() || f.length() == 0){
			try {
				if(!Utils.downloadFile("http://l2kc.ru/icons/"+URLEncoder.encode(icon,"UTF8")+"_0.png", "./data/icons/"+icon+".png"))
					log("ICON not load: "+icon);
			} catch (UnsupportedEncodingException e) {
				log("ICON not load: "+icon+" Error: "+e.getMessage());
			}
		}
	}
	
	public static void dumpStrings(String[] x){
			for (int i = 0; i < x.length; i++) 
				log(i+" ["+x[i]+"]");
	}
	
	private static void log(String s){
		System.out.println(s);
	}
	
}
