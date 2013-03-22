/*MAIN MENU
 * 1. BOOK SEARCH
 * 2. RETURN BOOK
 * 3. BORROW BOOK
 * 4. VIEW ACCOUNT
 * 5. EXIT
 * 
 * VIEW ACCOUNT:
 * ENTER PID:
 * 
 * OVERDUE FEES:
 * MEMBERSHIP FEES:
 * 
 * 1. PAY MEMBERSHIP FEES
 * 2. PAY OVERDUE FEES
 * 3. VIEW BOOKS BORROWED
 * 4. MAIN MENU
 * 5. EXIT
 * 
 * */

import java.util.Scanner;
import java.sql.* ;
import java.util.Date;

public class UserInterface {
	
	static Scanner in = new Scanner(System.in);
	
public static void main(String[] args){
	mainMenu();
}
	
public static void mainMenu(){
		
		System.out.println();
		System.out.println("MAIN MENU");
		System.out.println("1. BOOK SEARCH");
		System.out.println("2. RETURN BOOK");
		System.out.println("3. BORROW BOOK");
		System.out.println("4. VIEW ACCOUNT");
		System.out.println("5. EXIT");
		System.out.println();
		System.out.print("ENTER A NUMBER: ");
		
		int num = in.nextInt();
			
		if(num == 1)
			searchBook();
		else if(num == 2)
			returnBook();
		else if(num == 3)
			borrowBook();
		else if (num == 4)
			viewAccountMenu(-1);
		else if(num == 5){
			in.close();
			System.exit(0);
		}
		else{
			System.out.println("Not an option. Please try again");
			mainMenu();
		}
	}
	
	public static boolean isMember(int pid){
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		boolean isMember = false;
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String selectMembers = "SELECT * FROM member WHERE pid = " + pid;
			java.sql.ResultSet result = statement.executeQuery(selectMembers);
			while(result.next()){
				isMember = true;
				break;
			}
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		return isMember;
		
	}

	public static void searchBook(){
		
		//allow search by genre and search by author
		System.out.print("Would you like to search by genre or author? (a/g)");
		String choice = in.next();
		
		if(choice.equalsIgnoreCase("a")){
			searchAuthor();
		}else if(choice.equalsIgnoreCase("g")){
			searchGenre();
		}else{
			System.out.println();
			System.out.println("'" + choice + "'" + " is not an option.");
			System.out.println();
			mainMenu();
		}
	}
	
	public static void searchAuthor(){
		System.out.print("ENTER AUTHOR: ");
		String author = in.next();
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		boolean auth_exists = false;
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String authorQuery = "SELECT title, sid, books.bid FROM books, written_by WHERE books.bid = written_by.bid AND auth_id IN (SELECT auth_id FROM author WHERE auth_name = '" + author + "')";
			
			java.sql.ResultSet result = statement.executeQuery(authorQuery);
			while(result.next()){
				
				String title = result.getString("title");
				String sid = result.getString("sid");
				int bid = result.getInt("bid");
				
				System.out.println();
				System.out.println("Title: " + title);
				System.out.println("Section: " + sid);
				System.out.println("Book ID: " + bid);
				
				Statement statement_copies = con.createStatement();
				
				String books_borrowed = "CREATE VIEW books_borrowed AS SELECT book_instance.bid, book_instance.copy_no, return_date, boolean_returned FROM book_instance, borrowed_by WHERE borrowed_by.bid = book_instance.bid AND boolean_returned = 0 AND borrowed_by.copy_no = book_instance.copy_no AND book_instance.bid = " + bid;
				String books_not_borrowed = "CREATE VIEW books_not_borrowed AS SELECT bid, copy_no, CURRENT_DATE AS return_date, 1 AS boolean_returned FROM book_instance WHERE bid = " + bid + " AND copy_no NOT IN (SELECT copy_no FROM books_borrowed)";
				
				statement_copies.executeUpdate (books_borrowed);
				statement_copies.executeUpdate (books_not_borrowed);
				
				String getCopies = "SELECT * FROM books_borrowed UNION SELECT * FROM books_not_borrowed ORDER BY copy_no";
				java.sql.ResultSet result_copies = statement_copies.executeQuery(getCopies);
				
				while(result_copies.next())
				{
					auth_exists = true;
					
					int copy_no = result_copies.getInt("copy_no");
					int boolean_returned = result_copies.getInt("boolean_returned");
					Date return_date = result_copies.getDate("return_date");
					
					if(boolean_returned == 0){
						
						Date current = new Date();
						if(return_date.after(current)){
							System.out.println("COPY: " + copy_no + ", STATUS: out, RETURN DATE: " + return_date);
						}else{
							System.out.println("COPY: " + copy_no + ", STATUS: overdue");
						}
					}else{
						System.out.println("COPY: " + copy_no + ", STATUS: in");
					}
				}
				System.out.println();
				String dropView = "DROP VIEW books_borrowed";
				String dropView_not = "DROP VIEW books_not_borrowed";
				statement_copies.executeUpdate (dropView);
				statement_copies.executeUpdate (dropView_not);
			}
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		if(!auth_exists){
			System.out.println("Sorry, our library does not contain books by " + author);
			System.out.println();
		}
		
		mainMenu();
	}
	
	public static void searchGenre(){
		
		System.out.print("ENTER GENRE: ");
		String genre = in.next();
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		boolean genre_exists = false;
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String genreQuery = "SELECT * FROM books WHERE genre = '" + genre + "'";
			java.sql.ResultSet result = statement.executeQuery(genreQuery);
			
			while (result.next()){
				
				genre_exists = true;
				
				String title = result.getString("title");
				String sid = result.getString("sid");
				int bid = result.getInt("bid");
				
				System.out.println("Title: " + title);
				System.out.println("Section: " + sid);
				System.out.println("Book ID: " + bid);
				
				Statement statement_copies = con.createStatement();
				
				String books_borrowed = "CREATE VIEW books_borrowed AS SELECT book_instance.bid, book_instance.copy_no, return_date, boolean_returned FROM book_instance, borrowed_by WHERE borrowed_by.bid = book_instance.bid AND boolean_returned = 0 AND borrowed_by.copy_no = book_instance.copy_no AND book_instance.bid = " + bid;
				String books_not_borrowed = "CREATE VIEW books_not_borrowed AS SELECT bid, copy_no, CURRENT_DATE AS return_date, 1 AS boolean_returned FROM book_instance WHERE bid = " + bid + " AND copy_no NOT IN (SELECT copy_no FROM books_borrowed)";
				
				statement_copies.executeUpdate (books_borrowed);
				statement_copies.executeUpdate (books_not_borrowed);
				
				String getCopies = "SELECT * FROM books_borrowed UNION SELECT * FROM books_not_borrowed ORDER BY copy_no";
				java.sql.ResultSet result_copies = statement_copies.executeQuery(getCopies);
				while(result_copies.next())
				{
					int copy_no = result_copies.getInt("copy_no");
					int boolean_returned = result_copies.getInt("boolean_returned");
					Date return_date = result_copies.getDate("return_date");
					
					if(boolean_returned == 0){
						Date current = new Date();
						if(return_date.after(current)){
							System.out.println("COPY: " + copy_no + ", STATUS: out, RETURN DATE: " + return_date);
						}else{
							System.out.println("COPY: " + copy_no + ", STATUS: overdue");
						}
					}else{
						System.out.println("COPY: " + copy_no + ", STATUS: in");
					}
				}
				
				System.out.println();
				
				String dropView = "DROP VIEW books_borrowed";
				String dropView_not = "DROP VIEW books_not_borrowed";
				statement_copies.executeUpdate (dropView);
				statement_copies.executeUpdate (dropView_not);
			}
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		if(!genre_exists) System.out.println("Sorry, our library does not have any books about " + genre);
		mainMenu();
	}
	
	public static void returnBook(){
		
		System.out.println();
		System.out.println("RETURN BOOK");
		System.out.print("ENTER YOUR ID: ");
		int pid = in.nextInt();
		
		if(!isMember(pid)){
			System.out.println("You are not a member. Only members can return books.");
			mainMenu();
		}
		
		System.out.print("ENTER BOOK ID: ");
		int bid = in.nextInt();
		
		System.out.print("ENTER COPY NUMBER: ");
		int copy_no = in.nextInt();
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		boolean tupleExists = false; //to check if pid/bid/copy_no combination is a tuple
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			
			/* check if pid/bid/copy_no combination is a tuple in borrowed_by (check that this person actually borrowed this book) */
			String checkQuery = "SELECT * FROM borrowed_by";
			java.sql.ResultSet result = statement.executeQuery(checkQuery);
			while(result.next()){
				int this_bid = result.getInt("bid");
				int this_copy_no = result.getInt("copy_no");
				int boolean_returned = result.getInt("boolean_returned");
				
				if(this_bid == bid && this_copy_no == copy_no && boolean_returned == 0)
				{
					tupleExists = true;
					break;
				}
			}
			
			if(tupleExists){
				String updateBorrow = "UPDATE borrowed_by SET boolean_returned = 1 WHERE pid = " + pid + " AND bid = " + bid + " AND copy_no = " + copy_no;
				statement.executeUpdate(updateBorrow);
				System.out.println("Book successfully returned");
			}else{
				System.out.println("Either you already returned this book, or you never took it out.");
			}
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		mainMenu();
	}
	
	public static void borrowBook(){
		
		System.out.println();
		System.out.println("BORROW BOOK");
		System.out.print("ENTER YOUR ID: ");
		int pid = in.nextInt();
		
		if(!isMember(pid)){
			System.out.println("You are not a member. Only members can borrow books.");
			mainMenu();
		}
		
		System.out.print("ENTER BOOK ID: ");
		int bid = in.nextInt();
		
		System.out.print("ENTER COPY NUMBER: ");
		int copy_no = in.nextInt();
		
		System.out.println();
		
		/* check if book is in library */
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		boolean in_library = false;
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			
			String books_borrowed = "CREATE VIEW books_borrowed AS SELECT book_instance.bid, book_instance.copy_no, return_date, boolean_returned FROM book_instance, borrowed_by WHERE borrowed_by.bid = book_instance.bid AND boolean_returned = 0 AND borrowed_by.copy_no = book_instance.copy_no AND book_instance.bid = " + bid;
			String books_not_borrowed = "CREATE VIEW books_not_borrowed AS SELECT bid, copy_no, CURRENT_DATE AS return_date, 1 AS boolean_returned FROM book_instance WHERE bid = " + bid + " AND copy_no NOT IN (SELECT copy_no FROM books_borrowed)";
			
			statement.executeUpdate (books_borrowed);
			statement.executeUpdate (books_not_borrowed);
			
			String getCopies = "SELECT * FROM books_borrowed UNION SELECT * FROM books_not_borrowed ORDER BY copy_no";
			java.sql.ResultSet result = statement.executeQuery(getCopies);
			
			while(result.next())
			{
				int this_bid = result.getInt("bid");
				int this_copy_no = result.getInt("copy_no");
				int boolean_returned = result.getInt("boolean_returned");
				
				if(this_bid == bid && this_copy_no == copy_no && boolean_returned == 1){
					in_library = true;
				}
				
			}
			
			String dropView = "DROP VIEW books_borrowed";
			String dropView_not = "DROP VIEW books_not_borrowed";
			statement.executeUpdate (dropView);
			statement.executeUpdate (dropView_not);
			
			//if it is add it to borrowed_by
			if(in_library){
				
				String insertBorrow = "INSERT INTO Borrowed_by VALUES (" + pid + ", " + bid + ", " + copy_no + ", CURRENT_DATE, (CURRENT_DATE + 10 DAYS), 0)";
				statement.executeUpdate(insertBorrow);
				System.out.println("Successfully borrowed.");
				
			}else{
				System.out.println("Sorry, that book is not currently in the library.");
				System.out.println();
			}
			
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		mainMenu();
	}
	
	public static void viewAccountMenu(int pid){
		
		
		if(pid < 0){
			System.out.print("ENTER PID: ");
			pid = in.nextInt();
		}
		
		if(!isMember(pid)){
			System.out.println("You are not a member. Only members have accounts");
			mainMenu();
		}
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		double membership_fees = 0;
		double overdue_fees = 0;
		String fname = "";
		String lname = "";
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String getFees = "SELECT * FROM Member WHERE pid = " + pid;
			java.sql.ResultSet result = statement.executeQuery(getFees);
			
			while (result.next()){
				membership_fees = result.getDouble ("membership_fees");
				overdue_fees = result.getDouble("overdue_fees");
			}
			
			String getName = "SELECT fname, lname FROM person WHERE pid = " + pid;
			result = statement.executeQuery(getName);
			
			while (result.next()){
				fname = result.getString("fname");
				lname = result.getString("lname");
			}
			
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		System.out.println();
		System.out.println(fname + " " + lname + "'s ACCOUNT: ");
		System.out.println();
		System.out.println ("OVERDUE FEES:  " + overdue_fees);
		System.out.println ("MEMBERSHIP FEES:  " + membership_fees);
		System.out.println();
		System.out.println("1. PAY OVERDUE FEES");
		System.out.println("2. PAY MEMBERSHIP FEES");
		System.out.println("3. VIEW BOOKS BORROWED");
		System.out.println("4. MAIN MENU");
		System.out.println("5. EXIT");
		System.out.println();
		System.out.print("ENTER A NUMBER: ");
		
		int num = in.nextInt();
		
		if(num == 1)
			payOverdueFees(pid, overdue_fees);
		else if(num == 2)
			payMembershipFees(pid, membership_fees);
		else if(num == 3)
			viewBooksBorrowed(pid);
		else if (num == 4)
			mainMenu();
		else if(num == 5){
			in.close();
			System.exit(0);
		}
		else{
			System.out.println("Not an option. Please try again");
			mainMenu();
		}
	}
	
	public static void payOverdueFees(int pid, double fees){
		
		System.out.println("PAY OVERDUE FEES:");
		System.out.println();
		
		double pay = 0;
		double change = 0;
		double owed = fees;
		
		if(fees <= 0){
			System.out.println("You don't have any overdue fees to pay!");
		}else{
			
			System.out.println("You owe: " + fees);
			System.out.print("How much would you like to pay?");
			
			pay = in.nextDouble();
			
			if(pay>fees){
				change = pay - fees;
				owed = 0;
			}else if(pay<fees){
				change = 0;
				owed = fees - pay;
			}else{
				pay = 0;
				change = 0;
				owed = 0;
			}
			
			System.out.println("Your change: " + change);
			System.out.println("You owe: " + owed);
		}
	
	
		/* change amount owed in data base */
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String updateFees = "UPDATE member SET overdue_fees = " + owed + " WHERE pid = " + pid;
			statement.executeUpdate(updateFees);
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		viewAccountMenu(pid);
	}
	
	public static void payMembershipFees(int pid, double fees){
		
		System.out.println("PAY MEMBERSHIP FEES:");
		System.out.println();
		
		double pay = 0;
		double change = 0;
		double owed = fees;
		
		if(fees <= 0){
			System.out.println("You don't have any membership fees to pay!");
		}else{
			
			System.out.println("You owe: " + fees);
			System.out.print("How much would you like to pay?");
			
			pay = in.nextDouble();
			
			if(pay>fees){
				change = pay - fees;
				owed = 0;
			}else if(pay<fees){
				change = 0;
				owed = fees - pay;
			}else{
				pay = 0;
				change = 0;
				owed = 0;
			}
			
			System.out.println("Your change: " + change);
			System.out.println("You owe: " + owed);
		}
	
	
		/* change amount owed in data base */
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String updateFees = "UPDATE member SET membership_fees = " + owed + " WHERE pid = " + pid;
			statement.executeUpdate(updateFees);
			
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		viewAccountMenu(pid);
		
	}
	
	public static void viewBooksBorrowed(int pid){
		
		int sqlCode = 0; // Variable to hold SQLCODE
		String sqlState = "00000"; // Variable to hold SQLSTATE
		
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;

		} catch (Exception cnfe){
			System.out.println("Class not found");
		}
		
		boolean has_books = false;
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		try {
			Connection con = DriverManager.getConnection(url, "cs421g01", "A4n3i2k1a");
			Statement statement = con.createStatement();
			String createView = "CREATE VIEW books_borrowed AS SELECT title, return_date, copy_no, borrowed_by.bid, boolean_returned FROM books, borrowed_by WHERE books.bid = borrowed_by.bid AND pid = " + pid;
			statement.executeUpdate (createView);
			
			String query = "SELECT * FROM books_borrowed";
			java.sql.ResultSet result = statement.executeQuery(query);
			
			while (result.next()){
				String title = result.getString("title");
				Date return_date = result.getDate("return_date");
				int copy_no = result.getInt("copy_no");
				int boolean_returned = result.getInt("boolean_returned");
				
				if(boolean_returned == 0)
				{
					has_books = true;
					
					System.out.println();
					System.out.println(title);
					System.out.println("RETURN DATE: " + return_date);
					System.out.println("COPY NUMBER: " + copy_no);
					Date current = new Date();
					
					if(current.after(return_date)){
						System.out.println("OVERDUE !!");
					}
				}
			}
			
			String dropView = "DROP VIEW books_borrowed";
			statement.executeUpdate (dropView);
		}catch (SQLException e) {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
			System.out.println("Code: " + sqlCode + " sqlState: " + sqlState);
		}
		
		if(!has_books) System.out.println("You don't have any books out.");
		viewAccountMenu(pid);
	}
}
