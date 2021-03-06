package no.hioa.sentiment.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseUtilities
{
	public static JdbcTemplate getMySqlTemplate(String url, String database, String username, String password) throws UnsupportedEncodingException
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(DatabaseUtilities.getMySqlConnectionString(url, database, username, password));
		
		return new JdbcTemplate(dataSource);
	}

	public static JdbcTemplate getMsSqlTemplate(String url, String username, String password)
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
		dataSource.setUrl(DatabaseUtilities.getMsSqlConnectionString(url, username, password));
		
		return new JdbcTemplate(dataSource);
	}

	public static String getMySqlConnectionString(String url, String database, String username, String password) throws UnsupportedEncodingException
	{
		return "jdbc:mysql://" + url + "/" + database + "?user=" + URLEncoder.encode(username, "utf-8") + "&password="
				+ URLEncoder.encode(password, "utf-8");
	}

	public static String getMsSqlConnectionString(String url, String username, String password)
	{
		return "jdbc:jtds:sqlserver://" + url + "/master;user=" + username + ";password=" + password;
	}
}
