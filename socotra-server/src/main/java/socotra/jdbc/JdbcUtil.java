package socotra.jdbc;

import com.jcraft.jsch.*;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.util.Util;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * This file is about jdbc.
 */

public class JdbcUtil {

    /**
     * SshIP used to connect to school's ssh service.
     */
    private static String sshIP = "tinky-winky.cs.bham.ac.uk";
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

    public static HashMap<String, HashMap<ChatSession, List<ConnectionData>>> getClientsChatData() {
        return clientsChatData;
    }

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

    private synchronized static void insert(String sql) throws Exception {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
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
        ResultSet resultSet = inquire("select * from test_user where username='" + username + "' and password='" + password + "'");
        return resultSet.next();
    }

    public synchronized static void updateClientsChatData(String username, HashMap<ChatSession, List<ConnectionData>> chatData) {
        clientsChatData.put(username, chatData);
        storeClientsChatData(clientsChatData);
    }

    public static HashMap<ChatSession, List<ConnectionData>> getCertainChatData(String username) {
        return clientsChatData.get(username);
    }

    public static HashMap<String, HashMap<ChatSession, List<ConnectionData>>> queryClientsChatData() throws Exception {
        HashMap<String, HashMap<ChatSession, List<ConnectionData>>> result = new HashMap<>();
        HashMap<Integer, String> userIdNameMap = queryUserIdNameMap();
        userIdNameMap.forEach((k, v) -> {
            try {
                HashMap<ChatSession, List<ConnectionData>> chatData = generateChatData(k, v);
                result.put(v, chatData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return result;
    }

    private static HashMap<Integer, String> queryUserIdNameMap() throws Exception {
        ResultSet resultSet = inquire("select id, username from test_user");
        HashMap<Integer, String> result = new HashMap<>();
        while (resultSet.next()) {
            result.put(resultSet.getInt("id"), resultSet.getString("username"));
        }
        return result;
    }

    private static TreeSet<String> generateSessionMembers(String sessionName, String currentUser) {
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

    private static HashMap<ChatSession, List<ConnectionData>> generateChatData(int userId, String username) throws Exception {
        HashMap<ChatSession, List<ConnectionData>> result = new HashMap<>();
        HashSet<String> sessionNames = querySessionNames(userId);
        sessionNames.forEach(n -> {
            try {
                TreeSet<String> sessionMembers = generateSessionMembers(n, username);
                ChatSession chatSession = new ChatSession(sessionMembers, true);
                List<ConnectionData> certainChatData = queryCertainChatData(n, username, chatSession);
                result.put(chatSession, certainChatData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return result;
    }

    private static HashSet<String> querySessionNames(int userId) throws Exception {
        HashSet<String> result = new HashSet<>();
        ResultSet resultSet = inquire("select session_name from test_user_session where id=" + userId);
        while (resultSet.next()) {
            result.add(resultSet.getString("session_name"));
        }
        return result;
    }

    private static List<ConnectionData> queryCertainChatData(String sessionName, String currentUser, ChatSession chatSession) throws Exception {
        List<ConnectionData> result = new ArrayList<>();
        ResultSet resultSet = inquire("select data_id, data_text from test_chat_history where session_name='" + sessionName + "'");
        while (resultSet.next()) {
            result.add(new ConnectionData(resultSet.getString("data_text"), UUID.fromString(resultSet.getString("data_id")), currentUser, chatSession));
        }
        return result;
    }

    public static void storeClientsChatData(HashMap<String, HashMap<ChatSession, List<ConnectionData>>> clientsChatData) {

        clientsChatData.forEach((k, v) -> {
            try {
                int userId = queryUserId(k);
                v.forEach((k1, v1) -> {
                    try {
                        String sessionName = Util.generateChatName(k1.getToUsernames());

                        storeSession(userId, sessionName);
                        v1.forEach(n -> {
                            try {
                                storeChatHistory(n.getUuid().toString(), n.getTextData(), sessionName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static int queryUserId(String username) throws Exception {
        ResultSet resultSet = inquire("select id from test_user where username='" + username + "'");
        while (resultSet.next()) {
            return resultSet.getInt("id");
        }
        throw new IllegalArgumentException("Username does not exist.");
    }

    private static void storeSession(int userId, String sessionName) throws Exception {
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

    private static void storeChatHistory(String dataId, String dataText, String sessionName) throws Exception {
        ResultSet resultSet = inquire("select count(*) as count from test_chat_history where data_id='" + dataId + "'");
        int rowCount = 0;
        while (resultSet.next()) {
            rowCount = resultSet.getInt("count");
        }
        if (rowCount < 1) {
            insert("insert into test_chat_history(data_id, data_text, session_name) values ('" + dataId + "', '" + dataText + "', '" + sessionName + "')");
        } else {
            System.out.println("'" + dataText + "' has already existed.");
        }
    }

}
