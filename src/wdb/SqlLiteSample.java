package wdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SqlLiteSample {
	
	private static Connection con;
	private static Statement stmt;
	private static long start_time = 0l;
	
	private static String getEndTime(){
		long l = System.currentTimeMillis()-start_time;
		String r = "";
		try {
			if(l <= 1000)
				r =  l+" ms.";
			else{
				double s = l/1000;
				r =  s+" s.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			start_time = System.currentTimeMillis();
		}
		return r;		
	}
	private static void log(String s){
		System.out.println(s);
	}

	public static void main(String[] args) throws Exception {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		con = null;
		try {
			// create a database connection
			con = DriverManager.getConnection("jdbc:sqlite:sample.db");
			stmt = con.createStatement();
			stmt.setQueryTimeout(30); // set timeout to 30 sec.
			stmt.executeUpdate("PRAGMA synchronous=OFF");
			stmt.executeUpdate("PRAGMA count_changes=OFF");
			stmt.executeUpdate("PRAGMA journal_mode=MEMORY");
			stmt.executeUpdate("PRAGMA temp_store=MEMORY");

			log("***  START ***");
			start_time = System.currentTimeMillis();
			
			loadEtcItemGrp();
			if(true) return;
			loadIcons();
			loadNpc();

			// statement.executeUpdate("drop table if exists person");
			// statement.executeUpdate("create table person (id integer, name string)");
			// statement.executeUpdate("insert into person (`name`) values('leo')");
			// statement.executeUpdate("insert into person values('yui')");
			//ResultSet rs = stmt.executeQuery("select * from icons limit 1");
			//while (rs.next()) {
				// read the result set
			//	System.out.println("name = " + rs.getString("name"));
				// System.out.println("id = " + rs.getInt("id"));
			//}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
		log("***  END ***");
	}

	private static void loadEtcItemGrp() throws Exception {
		List<String> parse_list = Files.readAllLines(new File("./data/dat/etcitemgrp.txt").toPath(), StandardCharsets.UTF_8);	
		stmt.executeUpdate("DROP TABLE IF EXISTS etcitemgrp");
		
		String[] x = parse_list.get(0).split("\t");
		for (int i = 0; i < x.length; i++) 
			log(i+" ["+x[i]+"]");
		String icon;
		
		
		
		for (int i = 1; i < parse_list.size(); i++) {
			String[] s = parse_list.get(i).split("\t");
			icon = s[22].toLowerCase().replaceAll("branchsys.icon.", "").replaceAll("branchsys2.icon2.", "").
					replaceAll("branchsys2.icon.", "").replaceAll("br_cashtex.item.", "")
					.replaceAll("icon.", "").replaceAll("br_cashtex.item.", "").
					replaceAll("branchsys.", "").replaceAll("branchsys2.", "");
			
			File f = new File("./data/icons/"+icon+".png");
			
			if(!f.exists() || f.length() == 0 || f.length() == 45){
				if(!Utils.downloadFile("http://l2kc.ru/icons/"+URLEncoder.encode(icon,"UTF8")+"_0.png", "./data/icons/"+icon+".png"))
					log("ICON not load: "+icon);
			}
		}
		
		
	}
	private static void loadNpc() throws Exception {
		List<String> parse_list = Files.readAllLines(new File("./data/dat/NpcName-e.txt").toPath(), StandardCharsets.UTF_8);	

		stmt.executeUpdate("DROP TABLE IF EXISTS npc");
		stmt.executeUpdate("CREATE TABLE npc (id integer, name string, description string)");
		PreparedStatement t;
		for (int i = 1; i < parse_list.size(); i++) {
			String[] s = parse_list.get(i).split("\t");
			if(s.length < 3)
				continue;
			t = con.prepareStatement("INSERT INTO npc VALUES(?,?,?)");
			t.setInt(1, Integer.valueOf(s[0]));
			t.setString(2, s[1].replaceAll("a,", "").replace("\\"+"0", ""));
			t.setString(3, s[2].replaceAll("a,", "").replace("\\"+"0", ""));
			t.execute();
			t.close();
		}
		log("NPC load: "+getEndTime()+" Count: "+parse_list.size());
		stmt.executeUpdate("CREATE INDEX index_npc_id ON npc (id)");
		log("NPC create index: "+getEndTime());
	}
	
	private static void loadIcons() throws Exception {				
		String icon_list[] = new File("./data/icons").list();
		stmt.executeUpdate("DROP TABLE IF EXISTS icons");
		stmt.executeUpdate("CREATE TABLE icons (name string, icon blob)");
		PreparedStatement pstmt;
		File blob;
		for (String icon: icon_list) {
			blob = new File("./data/icons/" + icon);
			pstmt = con.prepareStatement("INSERT INTO icons VALUES(?,?)");
			pstmt.setString(1, icon.replaceAll(".png", ""));
			pstmt.setBytes(2, Files.readAllBytes(blob.toPath()));
			pstmt.execute();
			pstmt.close();
		}	
		log("Icons load: "+getEndTime()+" Count: "+icon_list.length);		
		stmt.executeUpdate("CREATE INDEX index_icon_name ON icons (name)");
		log("Icons create index: "+getEndTime());	
	}

}
