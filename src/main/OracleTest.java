import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class OracleTest {
    public static void main(String[] args) {
        // Set Oracle Wallet location
        String walletPath = "/Users/arunvaidhyanathan/Developer/Daatabase/Oracle/Wallet_workflow";
        System.setProperty("oracle.net.tns_admin", walletPath);
        System.setProperty("oracle.net.wallet_location", walletPath);
        
        // Register Oracle JDBC driver
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("Oracle JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC Driver not found");
            e.printStackTrace();
            return;
        }
        
        // Connection string using wallet configuration
        String url = "jdbc:oracle:thin:@workflow_high";
        String user = "cads";
        String password = "Password1234";
        
        // Set connection properties
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.ssl_server_dn_match", "true");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            if (conn != null) {
                System.out.println("Connected to Oracle successfully!");
            } else {
                System.out.println("Failed to connect to Oracle.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
