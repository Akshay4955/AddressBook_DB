package com.bridgelabz;

public class AddressBook {

    public void readFromCSV() {
        new AddressBookDB().readFromCSV();
    }

    public void readFromJSON() {
        new AddressBookDB().readFromJSON();
    }

    public void createAddressBook() {
        new AddressBookDB().createAddressBook();
    }

    public void updateContact() {
        new AddressBookDB().updateContact();
    }

    public void deleteContact() {
        new AddressBookDB().deleteContact();
    }
}
