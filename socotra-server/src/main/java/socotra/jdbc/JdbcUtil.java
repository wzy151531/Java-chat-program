package socotra.jdbc;

import com.jcraft.jsch.*;
import socotra.common.KeyBundle;
import socotra.common.User;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * This file is about jdbc.
 */

public class JdbcUtil {

    /**
     * SshIP used to connect to school's ssh service.
     */
    private static String sshIP = "tinky-winky.cs.bham.ac.uk";
    //    private static String dbHost = "mod-msc-sw1.cs.bham.ac.uk";
    private static String dbHost = "dbteach2";
    /**
     * SshUser used to connect to school's ssh service.
     */
    private static String sshUser;
    /**
     * SshPassword used to connect to school's ssh service.
     */
    private static String sshPassword;
    /**
     * Forward port to forward database information.
     */
    private static int forwardPort;
    /**
     * DbUser used to connect to database.
     */
    private static String dbUser;
    /**
     * DbPassword used to connect to database.
     */
    private static String dbPassword;
    /**
     * Ssh session.
     */
    private static Session session;
    /**
     * Database connection.
     */
    private static Connection connection;

    /**
     * Load ssh connection information from jdbc.properties file.
     */
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
        forwardPort = session.setPortForwardingL(50001, dbHost, 5432);
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
    public static void connect() throws Exception {
        String url = "jdbc:postgresql://localhost:" + forwardPort + "/" + dbUser;
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, dbUser, dbPassword);
        if (connection != null) {
            System.out.println("Database connected.");
        }
    }

    /**
     * Send query sql statement to database for results.
     *
     * @param sql The sql wants to be executed.
     * @return The results from database.
     * @throws Exception Exception thrown while executing the sql.
     */
    private static ResultSet inquire(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    /**
     * Send insert sql statement to database.
     *
     * @param sql The sql wants to be executed.
     * @throws Exception Exception thrown while executing the sql.
     */
    public synchronized static void insert(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    public static Set<User> loadUsers() throws SQLException {
        System.out.println("Load users.");
        ResultSet resultSet = inquire("select username, deviceid, active from users");
        Set<User> result = new HashSet<>();
        while (resultSet.next()) {
            result.add(new User(resultSet.getString("username"), resultSet.getInt("deviceid"), resultSet.getBoolean("active")));
        }
        return result;
    }

    public static boolean validateUser(User user, String password) throws IllegalArgumentException, SQLException {
        ResultSet resultSet = inquire("select active from users where username='" + user.getUsername() + "' and password='" + password + "' and deviceid=" + user.getDeviceId());
        while (resultSet.next()) {
            boolean isActive = resultSet.getBoolean("active");
            boolean result = !isActive && user.isActive();
            if (result) {
                insert("update users SET active=" + false + " where username='" + user.getUsername() + "'");
                insert("update users SET active=" + true + " where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
            }
            return result;
        }
        throw new IllegalArgumentException("Invalidated user.");
    }

    private static boolean validateUserExist(String username) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + username + "'");
        return resultSet.next();
    }

    private static boolean validateUserPwd(String username, String password) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + username + "' and password='" + password + "'");
        return resultSet.next();
    }

    public static User queryBackUpReceiver(User user) throws Exception {
        ResultSet resultSet = inquire("select username, deviceid from users where username='" + user.getUsername() + "' and active=" + true);
        if (resultSet.next()) {
            int deviceId = resultSet.getInt("deviceid");
            if (deviceId == user.getDeviceId()) {
                return null;
            }
            return new User(resultSet.getString("username"), deviceId, true);
        }
        throw new IllegalArgumentException("Bad user.");
    }

    private static boolean userExists(User user) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        return resultSet.next();
    }

    /**
     * Query userId and name of all clients from database.
     *
     * @return A map that contains all clients' userId and name.
     * @throws Exception The exception when query in database.
     */
    private static HashMap<Integer, String> queryUserIdNameMap() throws Exception {
        ResultSet resultSet = inquire("select id, username from users");
        HashMap<Integer, String> result = new HashMap<>();
        while (resultSet.next()) {
            result.put(resultSet.getInt("id"), resultSet.getString("username"));
        }
        return result;
    }

    /**
     * Generate chat session members according to the sessionName and currentUser.
     *
     * @param sessionName The session name of chat session.
     * @param currentUser The user who wants this session info.
     * @return Chat session members.
     */
    public static TreeSet<String> generateSessionMembers(String sessionName, String currentUser) {
        String[] usernames = sessionName.split(",");
        TreeSet<String> result = new TreeSet<>();
        for (String username : usernames) {
            result.add(username);
        }
        if (!sessionName.equals("all")) {
            result.add(currentUser);
        }
        return result;
    }

    /**
     * Query userId according to the given user name.
     *
     * @param user The given user.
     * @return The userId of given user name.
     * @throws Exception The exception when query in database.
     */
    private static int queryUserId(User user) throws SQLException, IllegalArgumentException {
        ResultSet resultSet = inquire("select id from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        if (resultSet.next()) {
            return resultSet.getInt("id");
        }
        throw new IllegalArgumentException("User does not exist.");
    }

    /**
     * @param user
     * @param password
     * @return
     */
    // TODO
    synchronized public static TwoTuple<Integer, Boolean> signUp(User user, String password) throws Exception {
        if (userExists(user)) {
            throw new IllegalArgumentException("User already exists.");
        }
        boolean isFresh = !validateUserExist(user.getUsername());
        if (!isFresh && !validateUserPwd(user.getUsername(), password)) {
            throw new IllegalArgumentException("Password is not correct.");
        }
        if (!isFresh) {
            insert("update users SET active=" + false + " where username='" + user.getUsername() + "'");
        }
        insert("insert into users(username, password, deviceid, active) values ('" + user.getUsername() + "', '" + password + "', " + user.getDeviceId() + ", " + true + ")");
        ResultSet resultSet = inquire("select id from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        if (resultSet.next()) {
            return new TwoTuple<>(resultSet.getInt("id"), isFresh);
        }
        throw new IllegalStateException("Insertion not success.");
    }

    private static byte[] flattenPreKeys(List<byte[]> preKeys) {
        byte[] result = new byte[33 * preKeys.size()];
        for (int i = 0; i < preKeys.size(); i++) {
            System.arraycopy(preKeys.get(i), 0, result, 33 * i, 33);
        }
        return result;
    }

    public static void storeKeyBundle(int userId, KeyBundle keyBundle) throws Exception {
        String sql = "insert into key_bundle(userId, registrationId, identityKey, preKeysId, preKeys, signedPreKeyId, signedPreKey, signedPreKeySignature) values (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, keyBundle.getRegistrationId());
        ps.setBytes(3, keyBundle.getIdentityKey());
        ps.setInt(4, 0);
        ps.setBytes(5, flattenPreKeys(keyBundle.getPreKeys()));
        ps.setInt(6, keyBundle.getSignedPreKeyId());
        ps.setBytes(7, keyBundle.getSignedPreKey());
        ps.setBytes(8, keyBundle.getSignedPreKeySignature());
        ps.executeUpdate();
    }

    public synchronized static KeyBundle queryKeyBundle(User user) throws SQLException, IllegalArgumentException {
        int userId = queryUserId(user);
        ResultSet resultSet = inquire("select * from key_bundle where userId='" + userId + "'");
        while (resultSet.next()) {
            int registrationId = resultSet.getInt("registrationId");
            byte[] identityKey = resultSet.getBytes("identityKey");
            int preKeysId = resultSet.getInt("preKeysId");
            byte[] preKeys = resultSet.getBytes("preKeys");
            int signedPreKeyId = resultSet.getInt("signedPreKeyId");
            byte[] signedPreKey = resultSet.getBytes("signedPreKey");
            byte[] signedPreKeySignature = resultSet.getBytes("signedPreKeySignature");

            byte[] preKey = new byte[33];
            int preKeysLength = preKeys.length;
            if (preKeysLength > 33) {
                System.arraycopy(preKeys, 0, preKey, 0, 33);
                byte[] updatedPreKeys = new byte[preKeysLength - 33];
                System.arraycopy(preKeys, 33, updatedPreKeys, 0, preKeysLength - 33);
                String sql = "update key_bundle SET preKeys = ?, preKeysId = ? WHERE userId='" + userId + "'";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setBytes(1, updatedPreKeys);
                ps.setInt(2, (preKeysId + 1) % 10);
                ps.executeUpdate();
            } else {
                preKey = null;
            }
////            System.out.println(new String(preKey));
            return new KeyBundle(registrationId, identityKey, preKeysId, preKey, signedPreKeyId, signedPreKey, signedPreKeySignature);

        }
        throw new IllegalArgumentException("UserId does not exist.");
    }

}
