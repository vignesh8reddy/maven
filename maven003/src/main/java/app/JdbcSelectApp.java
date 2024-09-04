package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcSelectApp {

    public static void main(String[] args) throws Exception {

        String url = "jdbc:mysql:///student_schema";
        String username = "root";
        String password = "root";

        Connection connection = DriverManager.getConnection(url, username, password);
        System.out.println("Connection object is created:: " + connection);

        Statement statement = connection.createStatement();
        System.out.println("Statement object is created:: " + statement);

        String sqlSelectQuery = "SELECT EAGE,ENAME,EADDRESS FROM employee001";
        ResultSet resultSet = statement.executeQuery(sqlSelectQuery);
        System.out.println("ResultSet object is created:: " + resultSet);

        System.out.println("SID\tSNAME\tSAGE\tSADDR");
        while (resultSet.next()) {
            Integer age = resultSet.getInt(1);
            String name = resultSet.getString(2);
            String team = resultSet.getString(3);
            System.out.println(name + "\t" + age + "\t" + team);
        }

        // Close the Connection
        connection.close();
        System.out.println("Closing the connection...");

    }

}
