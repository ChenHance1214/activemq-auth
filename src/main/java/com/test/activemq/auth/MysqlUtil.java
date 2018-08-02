package com.test.activemq.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class MysqlUtil {
	public static DataSource ds = null;
	
	static {
		// 1.获取DBCP数据源实现类对象
		BasicDataSource bds = new BasicDataSource();
		// 2.设置连接数据库需要的配置信息
		bds.setDriverClassName("com.mysql.jdbc.Driver");
		bds.setUrl("jdbc:mysql://192.168.1.128:3306/iot");
		bds.setUsername("root");
		bds.setPassword("stormorai");
		// 3.设置连接池的参数
		bds.setInitialSize(5);
		bds.setMaxActive(5);
		ds = bds;
	}

	public static void getData() throws SQLException {
		String sql;
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		sql = "SELECT user_name, password, topic FROM custom_mqtt_users";
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			// 展开结果集数据库
			while (rs.next()) {
				// 通过字段检索
				String user_name = rs.getString("user_name");
				String password = rs.getString("password");
				String topic = rs.getString("topic");
				
				RedisUtil.set(Constants.redis.login_auth_prex+user_name, password);
				RedisUtil.set(Constants.redis.topic_auth_prex+user_name, topic);
				// 输出数据
				System.out.print("user_name: " + user_name);
				System.out.print(", password: " + password);
				System.out.print(", topic: " + topic);
				System.out.print("\n");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 完成后关闭
			rs.close();
			stmt.close();
			conn.close();
		}
	}

	public static void main(String[] args) throws SQLException {
		getData();
//		// 4.获取数据库连接对象
//		Connection conn = ds.getConnection();
//		// 5.获取数据库连接信息
//		DatabaseMetaData metaData = conn.getMetaData();
//		// 6.打印数据库连接信息
//		System.out.println(metaData.getURL() + ",UserName=" + metaData.getUserName() + "," + metaData.getDriverName());
	}
}
