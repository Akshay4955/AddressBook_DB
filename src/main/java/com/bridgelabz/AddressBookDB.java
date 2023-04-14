package com.bridgelabz;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.bridgelabz.AddressBookConstants.*;

public class AddressBookDB {

    private Connection getConnection() throws SQLException {
        String jdbcURL = "jdbc:mysql://localhost:3306/addressbook_database_service?useSSL=false";
        String userName = "root";
        String password = "akshay@11";
        Connection connection;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            try {
                throw new AddressBookCustomException(AddressBookCustomException.ExceptionType.SQL_EXCEPTION, "SQL error occurred");
            } catch (AddressBookCustomException ec) {
                System.out.println(ec.getMessage());
            }
        }
        connection = DriverManager.getConnection(jdbcURL, userName, password);
        return connection;
    }

    private int getAddressBookType() {
        try (Connection connection = this.getConnection()) {
            String sql = String.format("select type_id, type from addressbook_type;");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.print(resultSet.getInt("type_id"));
                System.out.print(" ");
                System.out.print(resultSet.getString("type"));
                System.out.println();
            }
            System.out.println("please enter type_id in which address book contact to be added");
            return new Scanner(System.in).nextInt();
        } catch (SQLException e) {
            try {
                throw new AddressBookCustomException(AddressBookCustomException.ExceptionType.SQL_EXCEPTION, "SQL error occurred");
            } catch (AddressBookCustomException ec) {
                System.out.println(ec.getMessage());
            }
        }
        return 0;
    }

    private boolean checkAddressBookExist(String addressBookName) {
        String sql = String.format("select type_id from addressbook_type where type = '%s';", addressBookName);
        try (Connection connection = getConnection();) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) return true;
        } catch (SQLException e) {
            try {
                throw new AddressBookCustomException(AddressBookCustomException.ExceptionType.SQL_EXCEPTION, "SQL error occurred");
            } catch (AddressBookCustomException ec) {
                System.out.println(ec.getMessage());
            }
        }
        return false;
    }

    private int getContactsWithName() {
        System.out.println("Plz enter name and last name of contact for which you want to update data");
        Scanner input = new Scanner(System.in);
        String firstName = input.nextLine();
        String lastName = input.nextLine();
        try (Connection connection = this.getConnection()) {
            String sql = String.format("select contact_id, first_name, last_name, address_id, type_id from contact where first_name = '%s' and last_name = '%s';", firstName, lastName);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.print(resultSet.getInt("contact_id"));
                System.out.print(" ");
                System.out.print(resultSet.getString("first_name"));
                System.out.print(" ");
                System.out.print(resultSet.getString("last_name"));
                System.out.print(" ");
                System.out.print(resultSet.getInt("address_id"));
                System.out.print(" ");
                System.out.print(resultSet.getInt("type_id"));
                System.out.println();
            }
            System.out.println("Plz Enter contact id of contact for which you want perform");
            return input.nextInt();
        } catch (SQLException e) {
            try {
                throw new AddressBookCustomException(AddressBookCustomException.ExceptionType.SQL_EXCEPTION, "SQL error occurred");
            } catch (AddressBookCustomException ec) {
                System.out.println(ec.getMessage());
            }
        }
        return 0;
    }

    private String getFieldForUpdate(int contactId) {
        Scanner input = new Scanner(System.in);
        System.out.println("Plz enter value to be update");
        String value = input.nextLine();
        System.out.println("Plz enter field for which you want to update");
        System.out.println("1: First name, 2: Last name, 3: Address, 4: City, 5: State, 6: ZipCode, 7: Phone Number, 8: Email");
        int fieldChoice = input.nextInt();
        switch (fieldChoice) {
            case FIRST_NAME:
                return String.format("update contact set first_name = '%s' where contact_id = '%s';", value, contactId);
            case LAST_NAME:
                return String.format("update contact set last_name = '%s' where contact_id = '%s';", value, contactId);
            case ADDRESS:
                return String.format("update address inner join contact on contact.address_id = address.address_id set address = '%s' where contact_id = '%s';", value, contactId);
            case CITY:
                return String.format("update address inner join contact on contact.address_id = address.address_id set city = '%s' where contact_id = '%s';", value, contactId);
            case STATE:
                return String.format("update address inner join contact on contact.address_id = address.address_id set state = '%s' where contact_id = '%s';", value, contactId);
            case ZIPCODE:
                return String.format("update address inner join contact on contact.address_id = address.address_id set zip = '%s' where contact_id = '%s';", value, contactId);
            case PHONE_NUMBER:
                return String.format("update contact set phone_number = '%s' where contact_id = '%s';", value, contactId);
            case EMAIL:
                return String.format("update contact set email = '%s' where contact_id = '%s';", value, contactId);
            default:
                System.out.println("Entered wrong input");
        }
        return "";
    }

    public void readFromCSV() {
        int addressId = -1;
        Connection connection;
        try {
            connection = this.getConnection();
            connection.setAutoCommit(false);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (Reader reader = Files.newBufferedReader(Paths.get("src/main/resources/AddressBookContact.csv"))) {
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                try (Statement statement = connection.createStatement()) {
                    String sql = String.format("insert into address (address, city, state, zip) values " +
                            "('%s', '%s', '%S', '%s');", nextRecord[0], nextRecord[1], nextRecord[6], nextRecord[7]);
                    int rowsAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
                    if (rowsAffected == 1) {
                        ResultSet resultSet = statement.getGeneratedKeys();
                        if (resultSet.next()) addressId = resultSet.getInt(1);
                    }
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    System.out.println("Please enter address book for " + nextRecord[3] + " " + nextRecord[4]);
                    int typeID = this.getAddressBookType();
                    String sql = String.format("insert into contact (first_name, last_name, address_id, phone_number, email, type_id) values" +
                            "('%s', '%s', '%s', '%s', '%s', '%s');", nextRecord[3], nextRecord[4], addressId, nextRecord[5], nextRecord[2], typeID);
                    statement.executeUpdate(sql);
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                try {
                    connection.commit();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void readFromJSON() {
        int addressId = -1;
        Connection connection;
        try {
            connection = this.getConnection();
            connection.setAutoCommit(false);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Path filePath = Paths.get("src/main/resources/AddressBookContact.json");
        try (Reader reader = Files.newBufferedReader(filePath)) {
            Gson gson = new Gson();
            List<Contact> contactList = Arrays.asList(gson.fromJson(reader, Contact[].class));
            for (Contact contact : contactList) {
                try (Statement statement = connection.createStatement()) {
                    String sql = String.format("insert into address (address, city, state, zip) values " +
                            "('%s', '%s', '%S', '%s');", contact.getAddress(), contact.getCity(), contact.getState(), contact.getZipCode());
                    int rowsAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
                    if (rowsAffected == 1) {
                        ResultSet resultSet = statement.getGeneratedKeys();
                        if (resultSet.next()) addressId = resultSet.getInt(1);
                    }
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    System.out.println("Please enter address book for " + contact.getFirstName() + " " + contact.getLastName());
                    int typeID = this.getAddressBookType();
                    String sql = String.format("insert into contact (first_name, last_name, address_id, phone_number, email, type_id) values" +
                            "('%s', '%s', '%s', '%s', '%s', '%s');", contact.getFirstName(), contact.getLastName(), addressId, contact.getPhoneNumber(), contact.getEmail(), typeID);
                    statement.executeUpdate(sql);
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                try {
                    connection.commit();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createAddressBook() {
        try (Connection connection = this.getConnection()) {
            System.out.println("Plz enter address book name you want to add");
            Scanner inputFromConsole = new Scanner(System.in);
            String addressBookName = inputFromConsole.nextLine();
            boolean addressBookExistStatus = this.checkAddressBookExist(addressBookName);
            if (!addressBookExistStatus) {
                String sql = String.format("insert into addressbook_type (type) values ('%s');", addressBookName);
                Statement statement = connection.createStatement();
                statement.execute(sql);
            } else {
                System.out.println("AddressBook already exist");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateContact() {
        int contactId = this.getContactsWithName();
        String sql = this.getFieldForUpdate(contactId);
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteContact() {
        int contactId = this.getContactsWithName();
        String sql = String.format("delete contact,address from contact inner join address on contact.address_id = address.address_id " +
                " where contact_id = '%s';", contactId);
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
