package socotra.jdbc;

import com.jcraft.jsch.*;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.util.Util;

import java.io.*;
import java.security.Key;
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
     * Chat history data of all clients.
     */
    private static HashMap<String, HashMap<ChatSession, List<ConnectionData>>> clientsChatData;

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
            clientsChatData = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Getter for clientsChatData.
     *
     * @return The chat history data of all clients.
     */
    public static HashMap<String, HashMap<ChatSession, List<ConnectionData>>> getClientsChatData() {
        return clientsChatData;
    }

    /**
     * Setter for clientsChatData.
     *
     * @param clientsChatData The chat history data of all clients.
     */
    public static void setClientsChatData(HashMap<String, HashMap<ChatSession, List<ConnectionData>>> clientsChatData) {
        JdbcUtil.clientsChatData = clientsChatData;
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
    public synchronized static void insert(String sql) throws Exception {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    /**
     * Validate user's identity.
     *
     * @param user     The user's information.
     * @param password The user's password input.
     * @return The boolean indicates that whether the user is validated.
     * @throws Exception Exception when doing sql statement.
     */
    public static boolean validateUser(User user, String password) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + user.getUsername() + "' and password='" + password + "' and deviceid=" + user.getDeviceId());
        return resultSet.next();
    }

    private static boolean validateUserExist(String username) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + username + "'");
        return resultSet.next();
    }

    private static boolean validateUserPwd(String username, String password) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + username + "' and password='" + password + "'");
        return resultSet.next();
    }

    /**
     * Return previous active status of user and set new active status of user.
     *
     * @param user
     * @return
     * @throws Exception
     */
    public static boolean isActive(User user) throws Exception {
        ResultSet resultSet = inquire("select active from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        insert("update users SET active=" + false + " where username='" + user.getUsername() + "'");
//        String sql = "update users SET active = ? WHERE username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId();
//        PreparedStatement ps = connection.prepareStatement(sql);
//        ps.setBoolean(1, user.isActive());
//        ps.executeUpdate();
        insert("update users SET active=" + user.isActive() + " where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        if (resultSet.next()) {
            return resultSet.getBoolean("active");
        }
        throw new IllegalArgumentException("User does not exist.");
    }

    public static boolean isFresh(User user) throws Exception {
        ResultSet resultSet = inquire("select count(*) as count from users where username='" + user.getUsername() + "'");
        int count = 0;
        if (resultSet.next()) {
            count = resultSet.getInt("count");
        }
        if (count == 0) {
            throw new IllegalStateException("Bad user.");
        }
        return count == 1;
    }

    private static boolean userExists(User user) throws Exception {
        ResultSet resultSet = inquire("select * from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        return resultSet.next();
    }

    /**
     * Update clientsChatData.
     *
     * @param username The user who needs to update his chat history data.
     * @param chatData The chat history data of user.
     */
//    public synchronized static void updateClientsChatData(String username, HashMap<ChatSession, List<ConnectionData>> chatData) {
//        clientsChatData.put(username, chatData);
//        storeClientsChatData(clientsChatData);
//    }

    /**
     * Get certain chat history data of given user.
     *
     * @param username The given user name.
     * @return The certain chat history data of given user.
     */
//    public static HashMap<ChatSession, List<ConnectionData>> getCertainChatData(String username) {
//        return clientsChatData.get(username);
//    }

    /**
     * Query chat history data of all clients from database.
     *
     * @return Chat history data of all clients.
     * @throws Exception The exception when query in database.
     */
//    public static HashMap<String, HashMap<ChatSession, List<ConnectionData>>> queryClientsChatData() throws Exception {
//        HashMap<String, HashMap<ChatSession, List<ConnectionData>>> result = new HashMap<>();
//        HashMap<Integer, String> userIdNameMap = queryUserIdNameMap();
//        userIdNameMap.forEach((k, v) -> {
//            try {
//                HashMap<ChatSession, List<ConnectionData>> chatData = generateChatData(k, v);
//                result.put(v, chatData);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        return result;
//    }

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
     * Generate chatData according to the userId and username from database.
     *
     * @param userId   The userId of given user.
     * @param username The username of given user.
     * @return Chat history data of given user.
     * @throws Exception The exception when query in database.
     */
//    private static HashMap<ChatSession, List<ConnectionData>> generateChatData(int userId, String username) throws Exception {
//        HashMap<ChatSession, List<ConnectionData>> result = new HashMap<>();
//        HashSet<String> sessionNames = querySessionNames(userId);
//        sessionNames.forEach(n -> {
//            try {
//                TreeSet<String> sessionMembers = generateSessionMembers(n, username);
//                // TODO: fix group chat.
//                ChatSession chatSession = new ChatSession(sessionMembers, true, true, ChatSession.PAIRWISE);
//                List<ConnectionData> certainChatData = queryCertainChatData(n, username, chatSession);
//                result.put(chatSession, certainChatData);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        return result;
//    }

    /**
     * Query all session names related to the given userId.
     *
     * @param userId The given userId.
     * @return All related session names.
     * @throws Exception The exception when query in database.
     */
    private static HashSet<String> querySessionNames(int userId) throws Exception {
        HashSet<String> result = new HashSet<>();
        ResultSet resultSet = inquire("select session_name from test_user_session where id=" + userId);
        while (resultSet.next()) {
            result.add(resultSet.getString("session_name"));
        }
        return result;
    }

    /**
     * Query chat history data of certain session.
     *
     * @param sessionName The session name of given chatSession.
     * @param currentUser The user who needs this info.
     * @param chatSession The given chatSession;
     * @return The chat history data of given session.
     * @throws Exception The exception when query in database.
     */
//    private static List<ConnectionData> queryCertainChatData(String sessionName, String currentUser, ChatSession chatSession) throws Exception {
//        List<ConnectionData> result = new ArrayList<>();
//        ResultSet resultSet = inquire("select data_id, data_text, user_signature from test_connection_data where session_name='" + sessionName + "'");
//        while (resultSet.next()) {
//            result.add(new ConnectionData(resultSet.getString("data_text"), UUID.fromString(resultSet.getString("data_id")), resultSet.getString("user_signature"), chatSession));
//        }
//        return result;
//    }

    /**
     * Store chat history data of all clients to database.
     *
     * @param clientsChatData Chat history data of all clients.
     */
//    public static void storeClientsChatData(HashMap<String, HashMap<ChatSession, List<ConnectionData>>> clientsChatData) {
//        clientsChatData.forEach((k, v) -> {
//            try {
//                int userId = queryUserId(k);
//                v.forEach((k1, v1) -> {
//                    try {
//                        String sessionName = Util.generateChatName(k1.getMembers());
//
//                        storeSession(userId, sessionName);
//                        v1.forEach(n -> {
//                            try {
//                                if (n.getType() == 1) {
//                                    storeChatHistory(n.getUuid().toString(), n.getTextData(), n.getUserSignature(), sessionName);
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }

    /**
     * Insert chat history data of certain client.
     *
     * @param connectionData The connectionData needs to be inserted.
     */
//    public static void insertClientChatData(ConnectionData connectionData) {
//        new InsertClientChatDataThread(connectionData).start();
//    }

    /**
     * Query userId according to the given user name.
     *
     * @param user The given user.
     * @return The userId of given user name.
     * @throws Exception The exception when query in database.
     */
    static int queryUserId(User user) throws SQLException, IllegalArgumentException {
        ResultSet resultSet = inquire("select id from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        if (resultSet.next()) {
            return resultSet.getInt("id");
        }
        throw new IllegalArgumentException("User does not exist.");
    }

    /**
     * Store chat sessions of given userId.
     *
     * @param userId      The given userId.
     * @param sessionName Chat sessions of given userId.
     * @throws Exception The exception when query in database.
     */
    static void storeSession(int userId, String sessionName) throws Exception {
        ResultSet resultSet = inquire("select count(*) as count from test_user_session where id=" + userId + " and session_name='" + sessionName + "'");
        int rowCount = 0;
        while (resultSet.next()) {
            rowCount = resultSet.getInt("count");
        }
        if (rowCount < 1) {
            insert("insert into test_user_session(id, session_name) values (" + userId + ", '" + sessionName + "')");
        } else {
            System.out.println("'" + sessionName + "' has already existed.");
        }
    }

    /**
     * Store chat history data of given chat session and given connectionData.
     *
     * @param dataId      The dataId of given connectionData.
     * @param dataText    The dataText of given connectionData.
     * @param sessionName The session name of given chat session.
     * @throws Exception The exception when query in database.
     */
//    static void storeChatHistory(String dataId, String dataText, String userSignature, String sessionName) throws Exception {
//        ResultSet resultSet = inquire("select count(*) as count from test_connection_data where data_id='" + dataId + "'");
//        int rowCount = 0;
//        while (resultSet.next()) {
//            rowCount = resultSet.getInt("count");
//        }
//        if (rowCount < 1) {
//            insert("insert into test_connection_data(data_id, data_text, user_signature, session_name) values ('" + dataId + "', '" + dataText + "', '" + userSignature + "', '" + sessionName + "')");
//        } else {
//            System.out.println("'" + dataText + "' has already existed.");
//        }
//    }

    /**
     * @param user
     * @param password
     * @return
     */
    // TODO
    synchronized public static int signUp(User user, String password) throws Exception {
        if (userExists(user)) {
            throw new IllegalArgumentException("User already exists.");
        }
        if (validateUserExist(user.getUsername()) && !validateUserPwd(user.getUsername(), password)) {
            throw new IllegalArgumentException("Password is not correct.");
        }
        insert("insert into users(username, password, deviceid, active) values ('" + user.getUsername() + "', '" + password + "', " + user.getDeviceId() + ", " + false + ")");
        ResultSet resultSet = inquire("select id from users where username='" + user.getUsername() + "' and deviceid=" + user.getDeviceId());
        if (resultSet.next()) {
            return resultSet.getInt("id");
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
