import java.sql.Connection;
import org.postgresql.Driver;
import java.sql.DriverManager;

public class PostgresTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://db.nrhsoabqeskybrznxfyi.supabase.co:5432/postgres";
        String user = "postgres";
        String password = "Sreeja1608!@";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (conn != null) {
                System.out.println("Connected to PostgreSQL successfully!");
            } else {
                System.out.println("Failed to connect to PostgreSQL.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
