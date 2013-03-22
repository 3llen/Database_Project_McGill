import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class simpleJDBC {
	static List<Integer> pidList = new ArrayList<Integer>();

	public static void main(String[] args) throws SQLException {
		// Unique table names. Either the user supplies a unique identifier as a
		// command line argument, or the program makes one up.
		String tableName = "";

		if (args.length > 0) {
			tableName += args[0];
		} else {
			tableName += "NewTable";
		}

		// Register the driver. You must register the driver before you can use
		// it.
		try {
			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());
		} catch (Exception cnfe) {
			System.out.println("Class not found");
		}

		// This is the url you must use for DB2.
		// Note: This url may not valid now !
		String url = "jdbc:db2://db2.cs.mcgill.ca:50000/cs421";
		Connection con = DriverManager.getConnection(url, "cs421g01",
				"A4n3i2k1a");
		Statement statement = con.createStatement();

		updateOverdueFees(statement);

		// Finally but importantly close the statement and connection
		statement.close();
		con.close();
	}

	
	public static void updateOverdueFees(Statement statement) {

		// Querying a table

		try {
			String querySQL = "SELECT pid from Borrowed_by"
					+ " WHERE CURRENT_DATE > return_date";
			System.out.println(querySQL);
			java.sql.ResultSet rs = statement.executeQuery(querySQL);
			while (rs.next()) {
				int id = rs.getInt(1);
				System.out.println("id:  " + id);
				pidList.add(id);
			}

		} catch (SQLException e) {
			System.out.println("Code: " + e.getErrorCode() + "  sqlState: "
					+ e.getSQLState() + "error message" + e.getMessage());
		}

		String memberType = null;
	
		// Updating a table
		for (int id : pidList) {

			// select the type of student
			try {
				String querySQL = "SELECT type from Member WHERE pid = " + id;
				System.out.println(querySQL);
				java.sql.ResultSet rs = statement.executeQuery(querySQL);
				while (rs.next()) {
					memberType = rs.getString(1);
					System.out.println("studentType:  " + memberType);
				}
			} catch (SQLException e) {
				System.out.println("Code: " + e.getErrorCode() + "  sqlState: "
						+ e.getSQLState() + "error message" + e.getMessage());
			}

			double amountFee = getAmountFee(memberType);
			
			try {
				String updateSQL = "UPDATE Member"
						+ " SET overdue_fees = overdue_fees + " + amountFee + " WHERE pid = "
						+ id;
				System.out.println(updateSQL);
				statement.executeUpdate(updateSQL);

			} catch (SQLException e) {
				// Handle errors comes here;
				System.out.println("Code: " + e.getErrorCode() + "  sqlState: "
						+ e.getSQLState() + "error message" + e.getMessage());
			}
		}
	}


	private static double getAmountFee(String studentType) {
		double fee;
		if("student".equals(studentType.toLowerCase().trim())){
			fee = 0.25;
		}else if("teacher".equals(studentType.toLowerCase().trim())){
			fee = 0.5;
		}else{
			fee = 1.5;
		}
		return fee; 
	}

}
