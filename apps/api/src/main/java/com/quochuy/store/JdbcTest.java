package com.quochuy.store;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
public class JdbcTest {
    public static void main(String[] args) {
        /**
         * KIỂM TRA VỚI CONNECTION POOLER (IPv4 & .COM DOMAIN)
         * Host: aws-1-ap-south-1.pooler.supabase.com
         * Port: 6543
         * User: postgres.your-project-id
         */

        String host = "aws-1-ap-south-1.pooler.supabase.com";
        int port = 6543;
        String database = "postgres";

        // BẮT BUỘC: Thêm prepareThreshold=0 khi dùng Transaction Pooler với JDBC
        String url = String.format("jdbc:postgresql://%s:%d/%s?prepareThreshold=0", host, port, database);

        Properties props = new Properties();
        // Lưu ý: Username của Pooler bắt buộc phải có format: postgres.[project-id]
        props.setProperty("user", "postgres.your-project-id");
        props.setProperty("password", "replace-with-your-db-password");
        props.setProperty("ssl", "true");
        props.setProperty("sslmode", "require");

        // Ép Java ưu tiên IPv4
        System.setProperty("java.net.preferIPv4Stack" , "true");

        System.out.println("=== KIỂM TRA KẾT NỐI POOLER (.COM) ===");
        System.out.println("Đang phân giải DNS cho: " + host);

        try {
            // Bước 1: Kiểm tra DNS
            java.net.InetAddress address = java.net.InetAddress.getByName(host);
            System.out.println("   ✅ DNS OK! Địa chỉ IP: " + address.getHostAddress());

            // Bước 2: Thử kết nối JDBC
            System.out.println("Đang thiết lập kết nối JDBC...");
            try (Connection conn = DriverManager.getConnection(url, props)) {
                System.out.println("   ✅ KẾT NỐI THÀNH CÔNG!");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT version()");
                if (rs.next()) {
                    System.out.println("   -> Thông tin DB: " + rs.getString(1));
                }
            }
        } catch (java.net.UnknownHostException e) {
            System.err.println("   ❌ VẪN LỖI DNS: Máy vẫn không tìm thấy host .com");
            System.err.println("   HÀNH ĐỘNG: Kiểm tra lại mạng hoặc thử phát Wifi từ điện thoại.");
        } catch (Exception e) {
            System.err.println("   ❌ LỖI KẾT NỐI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
