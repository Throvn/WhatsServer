//package com.throvn;
//
//
//import java.util.Arrays;
//
//public class WhatAServer extends Server {
//    private DatabaseConnector db = new DatabaseConnector("85.25.49.125", 3306, "louisstanko", "louisstanko", "abc123");
//    private List<User> users = new List<>(); // Users logged in at the moment.
//
//    public WhatAServer(int PORT) {
//        super(PORT);
//    }
//
//
//    public void processNewConnection(String ip, int port) {
//        System.out.println(ip + " " + port);
//    }
//
//    public void processMessage(String ip, int port, String msg) {
//        System.out.println("ARGUMENTS: "+msg);
//        String[] m = msg.split(";");
//
//        switch (m[0]) {
//            case "CREATE USER":
//                if (createUser(ip, port, m[1])) {
//                    send(ip, port, "SUCCESS;CREATED,USER");
//                    System.out.println("SUCCESS;CREATED,USER");
//                } else {
//                    send(ip, port, "ERROR;NOT_FOUND,USER");
//                    System.out.println("ERROR;NOT_FOUND,USER");
//                }
//                return;
//            case "LOG IN":
//                signInUser(ip, port, m[1]);
//                return;
//            case "JOIN":
//                if (joinGroup(ip, port, m[1])) {
//                    send(ip, port, "SUCCESS;JOINED");
//                    System.out.println("SUCCESS;JOINED");
//                } else {
//                    send(ip, port, "ERROR;NOT_FOUND");
//                    System.out.println("ERROR;NOT_FOUND");
//                }
//                return;
//            case "CREATE GROUP":
//                if (createGroup(ip, port, m[1])) {
//                    send(ip, port, "SUCCESS;CREATED");
//                    System.out.println("SUCCESS;CREATED");
//                } else {
//                    send(ip, port, "ERROR;NOT_FOUND,USER");
//                    System.out.println("ERROR;NOT_FOUND,USER");
//                }
//                return;
//            case "LEAVE GROUP":
//                leaveGroup(ip, port, m[1]);
//                return;
//            case "SEND TO":
//                if (sendMessage(ip, port, m[1])) {
//                    send(ip, port, "SUCCESS;SENT");
//                    System.out.println("SUCCESS;SENT");
//                } else {
//                    send(ip, port, "ERROR;NOT_SENT");
//                    System.out.println("ERROR;NOT_SENT");
//                }
//                return;
//            case "SEND GROUPS":
//                sendAllGroups(ip, port);
//                return;
//            case "SEND CHAT":
//                sendChat(ip, port, m[1]);
//                return;
//        }
//
//        send(ip, port, "SYNTAX_ERROR,UNKNOWN");
//        System.out.println("SYNTAX_ERROR,UNKNOWN");
//
//    }
//
//    public void processClosingConnection(String ip, int port) {
//        deleteUser(ip, port);
//    }
//
//
//    /*
//     * User section
//     */
//    private void signInUser(String ip, int port, String msg) {
//        String[] m = msg.split(",");
//        m[1] = Hash.getHashedValue(m[1]);
//        db.executeStatement("SELECT Name, Password FROM chat__User WHERE Name='" + m[0] + "' AND Password='" + m[1] + "'");
//        if (db.getErrorMessage() != null) {
//            System.err.println(db.getErrorMessage());
//            send(ip, port, "ERROR;DB_ERROR");
//            return;
//        } else if (db.getCurrentQueryResult().getRowCount() == 1) {
//            System.out.println("Credentials are valid.");
//            db.executeStatement("SELECT ID FROM `chat__User` WHERE Name='" + m[0] + "' AND PASSWORD='" + m[1] + "'");
//            if (db.getErrorMessage() != null) {
//                System.err.println(db.getErrorMessage());
//                send(ip, port, "ERROR;DB_ERROR");
//                return;
//            }
//            User possibleUser = getUser(ip, port);
//            if (possibleUser != null) {
//                possibleUser.id = Integer.parseInt(db.getCurrentQueryResult().getData()[0][0]);
//            } else {
//                users.append(new User(ip, port, Integer.parseInt(db.getCurrentQueryResult().getData()[0][0])));
//            }
//            send(ip, port, "SUCCESS;LOGGED_IN "+db.getCurrentQueryResult().getData()[0][0]);
//            return;
//        } else {
//            System.out.println("No entry in DB was found.");
//            send(ip, port, "ERROR;CREDENTIALS_WRONG");
//            return;
//        }
//    }
//
//    private boolean createUser(String ip, int port, String msg) {
//        String[] nA = msg.split(",");
//        nA[1] = Hash.getHashedValue(nA[1]);
//        if (nA.length != 2) {
//            System.err.println("String has not the right length.");
//            return false;
//        }
//
//        db.executeStatement("SELECT `Name` FROM `chat__User` WHERE `Name`='" + nA[0].trim() + "'");
//        if (db.getCurrentQueryResult().getRowCount() > 0) {
//            System.err.println("Name already exists in DB.");
//            return false;
//        }
//        db.executeStatement("INSERT INTO `chat__User`(`Name`,`Password`) VALUES ('" + nA[0].trim() + "', '" + nA[1] + "')");
//        if (db.getErrorMessage() != null) {
//            System.err.println(db.getErrorMessage());
//            return false;
//        }
//
//        db.executeStatement("SELECT ID FROM `chat__User` WHERE Name='" + nA[0].trim() + "' AND PASSWORD='" + nA[1] + "'");
//        if (db.getErrorMessage() != null) {
//            System.err.println(db.getErrorMessage());
//            return false;
//        } else {
//            users.append(new User(ip, port, Integer.parseInt(db.getCurrentQueryResult().getData()[0][0])));
//            System.out.println("User created.");
//            return true;
//        }
//    }
//
//    private boolean deleteUser(String ip, int port) {
//        if (getUser(ip, port) != null) {
//            users.remove();
//            return true;
//        }
//        return false;
//    }
//
//
//
//
//    private boolean createGroup(String ip, int port, String msg) {
//        String[] g = msg.split(",");
//
//        User u = getUser(ip, port);
//        if (u != null) {
//            db.executeStatement("INSERT INTO `chat__Group`(`Name`,`Description`) VALUES ('" + g[0].trim() + "', '" + g[1].trim() + "')");
//            if (db.getErrorMessage() != null) {
//                System.err.println(db.getErrorMessage());
//                return false;
//            }
//            db.executeStatement("SELECT GroupID FROM chat__Group WHERE Name='" + g[0].trim() + "' AND Description='" + g[1].trim() + "' ORDER BY Time ASC");
//            if (db.getErrorMessage() != null) {
//                System.err.println("SELECT ID: " + db.getErrorMessage());
//                return false;
//            }
//            int gID = Integer.parseInt(db.getCurrentQueryResult().getData()[0][0]);
//
//            db.executeStatement("INSERT INTO `chat__UsersInGroup`(`GroupID`,`UserID`) VALUES ('" + gID + "','" + u.id + "')");
//            if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return false;}
//            System.out.println("Group created.");
//
//        }
//        return true;
//    }
//
//    private boolean joinGroup(String ip, int port, String msg) {
//        String[] m = msg.split(","); //[0] = ID der Gruppe
//        //[1..] = ID des Users
//        db.executeStatement("SELECT GroupID FROM `chat__Group` WHERE GroupID='"+m[0]+"'");
//        if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return false;}
//        if (db.getCurrentQueryResult().getRowCount() > 0) {
//            for (int i = 1; i < m.length; i++) {
//                db.executeStatement("INSERT INTO `chat__UsersInGroup`(`GroupID`,`UserID`) VALUES ('" + db.getCurrentQueryResult().getData()[0][0] + "','" + m[i] + "')");
//            }
//        }
//        return true;
//    }
//
//    private boolean leaveGroup (String ip, int port, String msg) {
//        String [] m = msg.split(",");
//        if (m.length == 1) {
//            User u = getUser(ip, port);
//            db.executeStatement("DELETE FROM `chat__UsersInGroup` WHERE `UserID`='"+u.id+"' AND `GroupID`='"+m[0]+"'");
//            if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return false;}
//            send(ip, port, "SUCCESS;LEFT");
//            System.out.println("SUCCESS;LEFT");
//            return true;
//        }
//        return false;
//    }
//
//    private boolean sendMessage (String ip, int port, String msg) {
//        String [] m = msg.split(",");
//        String groupID = m[0];
//        m = Arrays.copyOfRange(m, 1, m.length);
//        String message = String.join(",", m).trim();
//        System.err.println(m.toString());
//        User u = getUser(ip, port);
//        if (u != null) {
//            db.executeStatement("INSERT INTO `chat__Messages`(`UserID`,`GroupID`,`Message`) VALUES ('"+u.id+"','"+groupID+"','"+message+"')");
//            if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return false;}
//            db.executeStatement("SELECT UserID FROM `chat__UsersInGroup` WHERE `GroupID`='"+groupID+"'");
//            if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return false;}
//
//            QueryResult user = db.getCurrentQueryResult();
//            for (int i = 0; i < user.getRowCount(); i++) {
//                User us = getUser(Integer.parseInt(user.getData()[i][0]));
//                if (us != null) {
//                    send(us.getIp(), us.getPort(), message);
//                }
//            }
//        }
//        return false;
//    }
//
//    private void sendAllGroups(String ip, int port) {
//        User u = getUser(ip, port);
//        db.executeStatement("SELECT GroupID FROM `chat__UsersInGroup` WHERE `UserID`='"+u.id+"'");
//        if (db.getCurrentQueryResult().getRowCount() > 0) {
//            String list = db.getCurrentQueryResult().getData()[0][0];
//            for (int i = 1; i < db.getCurrentQueryResult().getRowCount(); i++) {
//                list += ", "+db.getCurrentQueryResult().getData()[i][0];
//            }
//            db.executeStatement("SELECT * FROM `chat__Group` WHERE `GroupID` IN ("+list+")");
//            if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return;}
//            String groups = "";
//            for (int i = 0; i < db.getCurrentQueryResult().getRowCount(); i++) {
//                groups += db.getCurrentQueryResult().getData()[i][0]+"::::"+db.getCurrentQueryResult().getData()[i][1]+"::::"+db.getCurrentQueryResult().getData()[i][2]+"::::"+db.getCurrentQueryResult().getData()[i][3]+"::::";
//            }
//            send(ip, port, "GROUPS::::"+groups);
//            return;
//        }
//        send(ip, port, "NO_GROUPS");
//    }
//
//    private void sendChat(String ip, int port, String msg) {
//        db.executeStatement("SELECT UserID, Message, Time FROM chat__Message WHERE `GroupID`='"+msg+"'");
//        if (db.getErrorMessage() != null) {System.err.println(db.getErrorMessage()); return;}
//        if (db.getCurrentQueryResult().getRowCount() > 0) {
//            String chat = "GET CHAT::::";
//            for (int i = 0; i < db.getCurrentQueryResult().getRowCount(); i++) {
//                chat += db.getCurrentQueryResult().getData()[i][0]+"::::"+db.getCurrentQueryResult().getData()[i][1]+"::::"+db.getCurrentQueryResult().getData()[i][2]+"::::";
//            }
//            send(ip, port, chat);
//            return;
//        }
//        send(ip, port, "NO_CHAT");
//    }
//
//
//    /*
//     * Returns found user by ip and port if found. else returns null.
//     */
//    private User getUser(String ip, int port) {
//        users.toFirst();
//        while(users.hasAccess()) {
//            User u = users.getContent();
//            if (u.getPort() == port && u.getIp().equals(ip)) {
//                return u;
//            }
//            users.next();
//        }
//        return null;
//    }
//
//    private User getUser(int id) {
//        users.toFirst();
//        while(users.hasAccess()) {
//            if (users.getContent().id == id) return users.getContent();
//            users.next();
//        }
//        System.out.println("ERROR;USER_NOT_FOUND");
//        return null;
//    }
//}