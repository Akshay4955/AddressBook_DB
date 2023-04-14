package com.bridgelabz;

import java.util.InputMismatchException;
import java.util.Scanner;

import static com.bridgelabz.AddressBookConstants.*;

public class AddressBookMain {
    public static void main(String[] args) {
        System.out.println("Welcome to address book with database");
        boolean loop = true;
        while (loop) {
            System.out.println("Plz enter what you want to perform");
            System.out.println("Press 1 to create address book" + '\n' + "Press 2 to read from CSV file" + '\n' + "Press 3 to read from json" +
                    '\n' + "Press 4 to update contact" + '\n' + "Press 5 to delete contact" + '\n' + "Press 0 to exit");
            try {
                Scanner inputFromConsole = new Scanner(System.in);
                int choiceForDataRead = inputFromConsole.nextInt();
                switch (choiceForDataRead) {
                    case CREATE_ADDRESS_BOOK:
                        new AddressBook().createAddressBook();
                        break;
                    case READ_FROM_CSV:
                        new AddressBook().readFromCSV();
                        break;
                    case READ_FROM_JSON:
                        new AddressBook().readFromJSON();
                        break;
                    case UPDATE_CONTACT:
                        new AddressBook().updateContact();
                        break;
                    case DELETE_CONTACT:
                        new AddressBook().deleteContact();
                        break;
                    case EXIT:
                        loop = false;
                        break;
                    default:
                        System.out.println("You entered wrong input");
                }
            } catch (InputMismatchException e) {
                try {
                    throw new AddressBookCustomException(AddressBookCustomException.ExceptionType.INVALID_INPUT_FORMAT, "You entered wrong input. Please enter valid input");
                } catch (AddressBookCustomException ec) {
                    System.out.println(ec.getMessage());
                }
            }
        }
    }
}
