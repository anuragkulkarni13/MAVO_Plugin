package cache;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.Constants;

public class DependencyCache {
	//Cache Operations

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static String get_DB_URL_VUL(String originalPomPath)
	{
		String DB_DIRECTORY_VUL = originalPomPath;
		String DB_URL_VUL = "jdbc:sqlite:" + DB_DIRECTORY_VUL + "\\" + Constants.DB_NAME_VUL;
		return DB_URL_VUL;
	}
	
	public static String get_DB_URL_COMB(String originalPomPath)
	{
		String DB_DIRECTORY_COMB = originalPomPath;
		String DB_URL_COMB = "jdbc:sqlite:" + DB_DIRECTORY_COMB + "\\" + Constants.DB_NAME_COMB;
		return DB_URL_COMB;
	}
	
	public static void createCache(String originalPomPath)
	{
		
		String DB_DIRECTORY_VUL = originalPomPath;		
		String DB_DIRECTORY_COMB = originalPomPath;		
		String DB_URL_VUL = get_DB_URL_VUL(originalPomPath);
		String DB_URL_COMB = get_DB_URL_COMB(originalPomPath);
		
//		System.out.println("******* inside createCache");
		try
		{
			File dbDir = new File(DB_DIRECTORY_VUL);
			if (!dbDir.exists()) {
				dbDir.mkdirs();
//				System.out.println("Cache Directory created: " + dbDir.getAbsolutePath());
			}
			else {
//				System.out.println("Cache Directory already exist");
			}
			
			// Create or Connect to SQLite Database
			Connection connection = DriverManager.getConnection(DB_URL_VUL);

			// Create Cache Table
			String createTableSQL = "CREATE TABLE IF NOT EXISTS cache ("
					+ "key TEXT PRIMARY KEY,"
					+ "value TEXT NOT NULL)";
			try (Statement stmt = connection.createStatement()) {
				stmt.execute(createTableSQL);
			}
			connection.close();	
//			System.out.println("Vulnerability Cache table created successfully");
			
			File dbDirComb = new File(DB_DIRECTORY_COMB);
			if (!dbDirComb.exists()) {
				dbDirComb.mkdirs();
//				System.out.println("Cache Directory created: " + dbDirComb.getAbsolutePath());
			}
			else {
//				System.out.println("Cache Directory already exist");
			}
			
			// Create or Connect to SQLite Database
			Connection connectionComb = DriverManager.getConnection(DB_URL_COMB);

			// Create Cache Table
			String createTableSQLComb = "CREATE TABLE IF NOT EXISTS cache ("
					+ "key TEXT PRIMARY KEY,"
					+ "value TEXT NOT NULL)";
			try (Statement stmt = connectionComb.createStatement()) {
				stmt.execute(createTableSQLComb);
			}
			connectionComb.close();	
//			System.out.println("Combinations Cache table created successfully");
		}catch(Exception e) {
			System.out.println(e);
		}
	}

	public static boolean isKeyPresent(Connection connection, String key) throws SQLException {
		String checkSQL = "SELECT 1 FROM cache WHERE key = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(checkSQL)) {
			pstmt.setString(1, key);
			ResultSet rs = pstmt.executeQuery();
			return rs.next();  // Returns true if key exists
		}
	}

	public static void putCache(Connection connection, String key, String value) throws SQLException {
		String upsertSQL = "INSERT INTO cache (key, value) VALUES (?, ?) "
				+ "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
		try (PreparedStatement pstmt = connection.prepareStatement(upsertSQL)) {
			pstmt.setString(1, key);
			pstmt.setString(2, value);  // Convert Map to JSON string
			pstmt.executeUpdate();
		}
	}

	// Retrieve Data from Cache
	public static String getCache(Connection connection, String key) throws SQLException {
		String selectSQL = "SELECT value FROM cache WHERE key = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
			pstmt.setString(1, key);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("value");  // Convert JSON string to Map
			}
		}
		return null; // Return null if not found
	}
	
	
	
//	// Insert or Update Data in Cache
//	public static void putCombCache(Connection connection, Map<String, Integer> key, String value) throws SQLException {
//		String upsertSQL = "INSERT INTO cachecomb (key, value) VALUES (?, ?) "
//				+ "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
//		try (PreparedStatement pstmt = connection.prepareStatement(upsertSQL)) {
//			pstmt.setString(1, Utils.mapToJson(key));
//			pstmt.setString(2, value);  // Convert Map to JSON string
//			pstmt.executeUpdate();
//		}
//	}
//
//	// Retrieve Data from Cache
//	public static String getCombCache(Connection connection, Map<String, Integer> key) throws SQLException {
//		String selectSQL = "SELECT value FROM cachecomb WHERE key = ?";
//		try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
//			pstmt.setString(1, Utils.mapToJson(key));
//			ResultSet rs = pstmt.executeQuery();
//			if (rs.next()) {
//				return rs.getString("value");  // Convert JSON string to Map
//			}
//		}
//		return null; // Return null if not found
//	}


}
