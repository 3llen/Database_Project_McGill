CREATE TABLE Person (pid INTEGER NOT NULL, fname VARCHAR(20), lname VARCHAR(20), phone CHAR(10), PRIMARY KEY (pid))

CREATE TABLE Employee (pid INTEGER NOT NULL, sin CHAR(9) NOT NULL UNIQUE, salary DOUBLE, address VARCHAR(200), PRIMARY KEY (pid), FOREIGN KEY (pid) REFERENCES Person)

CREATE TABLE Member (pid INTEGER NOT NULL, registration_date_memebership DATE DEFAULT CURRENT_DATE, expire_date_memebership DATE DEFAULT CURRENT_DATE, membership_fees DOUBLE, overdue_fees DOUBLE, PRIMARY KEY(pid), FOREIGN KEY (pid) REFERENCES Person)

CREATE TABLE Sections (sid VARCHAR(10) NOT NULL, pid INTEGER NOT NULL, name VARCHAR(50), PRIMARY KEY (sid), FOREIGN KEY (pid) REFERENCES Employee)

CREATE TABLE Books (bid INTEGER NOT NULL, sid VARCHAR(10) NOT NULL, Edition  VARCHAR (100), Genre  VARCHAR (100), Title VARCHAR (100), ISBN CHAR(10) UNIQUE NOT NULL, shelf_no INTEGER, PRIMARY KEY (bid), FOREIGN KEY (sid) REFERENCES Sections)

CREATE TABLE Book_instance (copy_no INTEGER NOT NULL, bid INTEGER NOT NULL, PRIMARY KEY (copy_no, bid), FOREIGN KEY (bid) REFERENCES Books)

CREATE TABLE Author (auth_id INTEGER NOT NULL, auth_name VARCHAR(30) NOT NULL, PRIMARY KEY(auth_id))

CREATE TABLE Written_by (auth_id INTEGER NOT NULL, bid INTEGER NOT NULL, PRIMARY KEY(auth_id, bid), FOREIGN KEY (auth_id) REFERENCES author, FOREIGN KEY (bid) REFERENCES Books)

CREATE TABLE Publisher (pub_id CHAR(9) NOT NULL, name CHAR (20), address CHAR(100), phone NUM(15,15), PRIMARY KEY (pub_id))

CREATE TABLE Published_by (pub_id CHAR(9) NOT NULL, bid INTEGER NOT NULL, PRIMARY KEY (pub_id, bid), FOREIGN KEY (pub_id) REFERENCES Publisher, FOREIGN KEY (bid)REFERENCES Books)

CREATE TABLE Borrowed_by (pid INTEGER NOT NULL, bid INTEGER NOT NULL, copy_no INTEGER NOT NULL, checkout_date DATE DEFAULT CURRENT_DATE NOT NULL, return_date DATE DEFAULT CURRENT_DATE, PRIMARY KEY (pid, bid, copy_no, checkout_date), FOREIGN KEY (bid, copy_no) REFERENCES Book_instance(bid, copy_no), FOREIGN KEY (pid) REFERENCES Member)















