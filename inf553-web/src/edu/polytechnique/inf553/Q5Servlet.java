package edu.polytechnique.inf553;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Given a number of tracks and a country name, find all releases in this
 * country that have more tracks than the input number. Return the id of the
 * release, sorted in ascending order.
 */
@WebServlet(urlPatterns = { "/Q5" }, initParams = {
		@WebInitParam(name = "number", value = "", description = "The number of tracks"),
		@WebInitParam(name = "countryname", value = "", description = "The country name") })
public final class Q5Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Q5Servlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			String host = "localhost";
			String dbName = "arnaudstiegler";
			int port = 5432;
			String serverURL = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
			String userName = "arnaudstiegler";
			String password = "mypass";
			connection = DriverManager.getConnection(serverURL, userName, password);

			DatabaseMetaData dmd = connection.getMetaData();
			String serverName = dmd.getDatabaseProductName();
			String productVersion = dmd.getDatabaseProductVersion();

			String countryname = request.getParameter("countryname");
			int number = Integer.parseInt(request.getParameter("number"));

			// STEP 4: Execute a query
			System.out.println("Creating statement...");

			PreparedStatement stat = connection.prepareStatement(
					"SELECT release.id FROM country, release  INNER JOIN release_country  ON release_country.release=release.id  WHERE release_country.country=country.id AND country.name=? AND (SELECT COUNT(*)  FROM track WHERE track.release = release.id)>? ORDER BY release.id ASC;");
			stat.setString(1, countryname);
			stat.setInt(2, number);
			ResultSet rs = stat.executeQuery();

			response.setContentType("text/xml;charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.append("<result>");

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("country.id");

				// Display values
				writer.append("<row>");
				writer.append("<name>");
				writer.append(StringEscapeUtils.escapeXml11(Integer.toString(id)));
				writer.append("</name>");
				writer.append("</row>");

			}
			writer.append("</result>");

		} catch (ClassNotFoundException e) {
			System.out.println("Driver not found!");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

}
