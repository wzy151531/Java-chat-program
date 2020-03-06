package socotra.jdbc;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JdbcUtil {

    private static String sshIP = "tinky-winky.cs.bham.ac.uk";
    private static String sshUser;
    private static String sshPassword;
    private static int forwardPort;
    private static String dbUser;
    private static String dbPassword;
    private static Session session;
    private static Connection connection;

    static {
        try {
            Properties properties = new Properties();
            InputStream in = JdbcUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
            properties.load(in);
            sshUser = properties.getProperty("sshUser");
            sshPassword = properties.getProperty("sshPassword");
            dbUser = properties.getProperty("dbUser");
            dbPassword = properties.getProperty("dbPassword");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Create ssh session.
     *
     * @throws Exception
     */
    public static void init() throws Exception {

        JSch jsch = new JSch();
        session = jsch.getSession(sshUser, sshIP, 22);
        session.setPassword(sshPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        System.out.println(session.getServerVersion());
        forwardPort = session.setPortForwardingL(50001, "mod-msc-sw1.cs.bham.ac.uk", 5432);
        System.out.println("localhost:" + forwardPort);
    }

    /**
     * End ssh session.
     *
     * @throws Exception
     */
    public static void end() throws Exception {
        session.delPortForwardingL(forwardPort);
        session.disconnect();
        System.out.println("Session disconnected.");
    }

    /**
     * Connect to database.
     *
     * @return The connection to database.
     * @throws Exception Exception thrown while connecting to database.
     */
    public static Connection connect() throws Exception {
        String url = "jdbc:postgresql://localhost:" + forwardPort + "/" + dbUser;
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, dbUser, dbPassword);
        if (connection != null) {
            System.out.println("Database connected.");
        }
        return connection;
    }

    /**
     * Send some sql statement to database for results.
     *
     * @param sql The sql wants to be executed.
     * @return The results from database.
     * @throws Exception Exception thrown while executing the sql.
     */
    private static ResultSet inquire(String sql) throws Exception {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    /**
     * Validate user's identity.
     *
     * @param username The user's username input.
     * @param password The user's password input.
     * @return The boolean indicates that whether the user is validated.
     * @throws Exception Exception when doing sql statement.
     */
    public static boolean validateUser(String username, String password) throws Exception {
        ResultSet resultSet = JdbcUtil.inquire("select * from test_user where username='" + username + "' and password='" + password + "'");
        return resultSet.next();
    }

}
