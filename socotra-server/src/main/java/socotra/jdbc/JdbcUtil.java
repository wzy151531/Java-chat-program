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
    private static ResultSet inquire(String sql) throws Exception {
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
     * @param username The user's username input.
     * @param password The user's password input.
     * @return The boolean indicates that whether the user is validated.
     * @throws Exception Exception when doing sql statement.
     */
    public static boolean validateUser(String username, String password) throws Exception {
        ResultSet resultSet = inquire("select * from test_user where username='" + username + "' and password='" + password + "'");
        return resultSet.next();
    }

    /**
     * Update clientsChatData.
     *
     * @param username The user who needs to update his chat history data.
     * @param chatData The chat history data of user.
     */
    public synchronized static void updateClientsChatData(String username, HashMap<ChatSession, List<ConnectionData>> chatData) {
        clientsChatData.put(username, chatData);
        storeClientsChatData(clientsChatData);
    }

    /**
     * Get certain chat history data of given user.
     *
     * @param username The given user name.
     * @return The certain chat history data of given user.
     */
    public static HashMap<ChatSession, List<ConnectionData>> getCertainChatData(String username) {
        return clientsChatData.get(username);
    }

    /**
     * Query chat history data of all clients from database.
     *
     * @return Chat history data of all clients.
     * @throws Exception The exception when query in database.
     */
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

    /**
     * Query userId and name of all clients from database.
     *
     * @return A map that contains all clients' userId and name.
     * @throws Exception The exception when query in database.
     */
    private static HashMap<Integer, String> queryUserIdNameMap() throws Exception {
        ResultSet resultSet = inquire("select id, username from test_user");
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
    private static List<ConnectionData> queryCertainChatData(String sessionName, String currentUser, ChatSession chatSession) throws Exception {
        List<ConnectionData> result = new ArrayList<>();
        ResultSet resultSet = inquire("select data_id, data_text, user_signature from test_connection_data where session_name='" + sessionName + "'");
        while (resultSet.next()) {
            result.add(new ConnectionData(resultSet.getString("data_text"), UUID.fromString(resultSet.getString("data_id")), resultSet.getString("user_signature"), chatSession));
        }
        return result;
    }

    /**
     * Store chat history data of all clients to database.
     *
     * @param clientsChatData Chat history data of all clients.
     */
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
                                if (n.getType() == 1) {
                                    storeChatHistory(n.getUuid().toString(), n.getTextData(), n.getUserSignature(), sessionName);
                                }
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

    /**
     * Insert chat history data of certain client.
     *
     * @param connectionData The connectionData needs to be inserted.
     */
    public static void insertClientChatData(ConnectionData connectionData) {
        new InsertClientChatDataThread(connectionData).start();
    }

    /**
     * Query userId according to the given user name.
     *
     * @param username The given user name.
     * @return The userId of given user name.
     * @throws Exception The exception when query in database.
     */
    static int queryUserId(String username) throws Exception {
        ResultSet resultSet = inquire("select id from test_user where username='" + username + "'");
        while (resultSet.next()) {
            return resultSet.getInt("id");
        }
        throw new IllegalArgumentException("Username does not exist.");
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
    static void storeChatHistory(String dataId, String dataText, String userSignature, String sessionName) throws Exception {
        ResultSet resultSet = inquire("select count(*) as count from test_connection_data where data_id='" + dataId + "'");
        int rowCount = 0;
        while (resultSet.next()) {
            rowCount = resultSet.getInt("count");
        }
        if (rowCount < 1) {
            insert("insert into test_connection_data(data_id, data_text, user_signature, session_name) values ('" + dataId + "', '" + dataText + "', '" + userSignature + "', '" + sessionName + "')");
        } else {
            System.out.println("'" + dataText + "' has already existed.");
        }
    }

}
