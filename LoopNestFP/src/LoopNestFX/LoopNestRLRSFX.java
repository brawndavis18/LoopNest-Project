package LoopNestFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
public class LoopNestRLRSFX extends Application {
   static Connection conn;
   
// Interface representing the common functionality for user classes
   interface UserInterface {
       String getPassword();
       String getSecurityQuestion();
       String getSecurityAnswer();
       void addToDatabase(String firstName, String lastName, String address, String zipCode, String state,
                          String username, String password, String email, String ssn,
                          String securityQuestion, String securityAnswer, String userType);
   }
// Base User class implementing the UserInterface
   private static class User implements UserInterface {
       protected String password;
       protected String securityQuestion;
       protected String securityAnswer;
  
       // Constructor for creating a User object with initial data
       public User(String firstName, String lastName, String address, String zip, String state,
                   String username, String password, String email, String ssn,
                   String securityQuestion, String securityAnswer) {
    	    // Initialize user-specific data
    	   this.password = password;
           this.securityQuestion = securityQuestion;
           this.securityAnswer = securityAnswer;
           addToDatabase(firstName, lastName, address, zip, state, username, password, email, ssn, securityQuestion, securityAnswer, "Customer" );
       }
       public String getPassword() {
           return password;
       }
       public String getSecurityQuestion() {
           return securityQuestion;
       }
       public String getSecurityAnswer() {
           return securityAnswer;
       }
    
       // Method to add user data to the database
       @Override
       public void addToDatabase(String firstName, String lastName, String address, String zipCode, String state,
               String username, String password, String email, String ssn,
               String securityQuestion, String securityAnswer, String userType) {
           Connection conn = null;
           try {
               Class.forName("org.postgresql.Driver");
               String url = "jdbc:postgresql://localhost:5432/LoopNest";
               String user = "postgres";
               String password1 = "bd199316";
               conn = DriverManager.getConnection(url, user, password1);
               conn.setAutoCommit(true);
            // SQL query to insert user data into the LoopNestUser table
               String insertUserQuery = "INSERT INTO LoopNestUser (FirstName, LastName, Address, ZipCode, State, Username, Password, Email, SSN, SecurityQuestion, SecurityAnswer, UserType) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
               try (PreparedStatement pstmt = conn.prepareStatement(insertUserQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            	// Set parameters for the prepared statement
            	   pstmt.setString(1, firstName);
                   pstmt.setString(2, lastName);
                   pstmt.setString(3, address);
                   pstmt.setString(4, zipCode);
                   pstmt.setString(5, state);
                   pstmt.setString(6, username);
                   pstmt.setString(7, password);
                   pstmt.setString(8, email);
                   pstmt.setString(9, ssn);
                   pstmt.setString(10, securityQuestion);
                   pstmt.setString(11, securityAnswer);
                   pstmt.setString(12, userType);
               
                   // Execute the insert query
                   pstmt.executeUpdate();
                   registerUserHelper(conn, firstName, lastName, address, zipCode, state, username, password, email, ssn, securityQuestion, securityAnswer, userType);
               }
           } catch (ClassNotFoundException | SQLException e) {
               System.out.println(e.getMessage());
           } finally {
               try {
                   if (conn != null) {
                       System.out.println("Closing connection");
                       conn.close();
                   }
               } catch (SQLException ex) {
                   System.out.println(ex.getMessage());
               }
           }
       }
// Helper method, works in conjunction with registerUser()
       private void registerUserHelper(Connection conn, String firstName, String lastName, String address, String zipCode, String state,
                                       String username, String password, String email, String ssn,
                                       String securityQuestion, String securityAnswer, String userType) {
           try {
               int userId = displayUserID(username);
               if (userId != -1) {
                   System.out.println("User ID: " + userId);
                   System.out.println("Registration Successful!");
               } else {
                   System.out.println("Failed to retrieve user ID. User registration may not be successful.");
               }
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
       }
   }
   static class Customer extends User {
       public Customer(String firstName, String lastName, String address, String zip, String state,
                       String username, String password, String email, String ssn,
                       String securityQuestion, String securityAnswer, String userType) {
           super(firstName, lastName, address, zip, state, username, password, email, ssn, securityQuestion, securityAnswer);
       }
   }
   static class Admin extends User {
       public Admin(String firstName, String lastName, String address, String zip, String state,
                    String username, String password, String email, String ssn,
                    String securityQuestion, String securityAnswer, String userType) {
           super(firstName, lastName, address, zip, state, username, password, email, ssn, securityQuestion, securityAnswer);
       }
   }
   static Map<String, UserInterface> registeredUsers = new HashMap<>();
   public static void main(String[] args) {
       launch(args);
   }
   @Override
   public void start(Stage primaryStage) {
       conn = initializeDatabaseConnection();
       GridPane grid = new GridPane();
       grid.setPadding(new Insets(10, 10, 10, 10));
       grid.setVgap(8);
       grid.setHgap(10);
       Button loginButton = new Button("Login");
       Button registerButton = new Button("Register");
       Button recoveryButton = new Button("Password Recovery");
       Button exitButton = new Button("Exit");
       GridPane.setConstraints(loginButton, 1, 0);
       GridPane.setConstraints(registerButton, 1, 1);
       GridPane.setConstraints(recoveryButton, 1, 2);
       GridPane.setConstraints(exitButton, 1, 3);
       grid.getChildren().addAll(loginButton, registerButton, recoveryButton, exitButton);
       loginButton.setOnAction(e -> loginUser());
       registerButton.setOnAction(e -> registerUser(primaryStage));
       recoveryButton.setOnAction(e -> passwordRecovery());
       exitButton.setOnAction(e -> System.exit(0));
       Scene scene = new Scene(grid, 300, 200);
       primaryStage.setScene(scene);
       primaryStage.setTitle("Main Menu");
       primaryStage.show();
   }
// Stores connection to LoopNestDB
   protected static Connection initializeDatabaseConnection() {
       Connection connection = null;
       try {
           Class.forName("org.postgresql.Driver");
           String url = "jdbc:postgresql://localhost:5432/LoopNest";
           String user = "postgres";
           String password = "bd199316";
           connection = DriverManager.getConnection(url, user, password);
           connection.setAutoCommit(true);
       } catch (ClassNotFoundException | SQLException e) {
           e.printStackTrace();
       }
       return connection;
       }
   static void registerUser(Stage primaryStage) {
	// Initialize variables to store user registration information
	   String username = null;
       System.out.println("Registration:");
  
       // Prompt user for various registration details using TextInputDialog
       TextInputDialog firstNameDialog = new TextInputDialog();
       firstNameDialog.setHeaderText(null);
       firstNameDialog.setTitle("Registration");
       firstNameDialog.setContentText("Enter First Name:");
       String firstName = firstNameDialog.showAndWait().orElse("");
       TextInputDialog lastNameDialog = new TextInputDialog();
       lastNameDialog.setHeaderText(null);
       lastNameDialog.setTitle("Registration");
       lastNameDialog.setContentText("Enter Last Name:");
       String lastName = lastNameDialog.showAndWait().orElse("");
       TextInputDialog addressDialog = new TextInputDialog();
       addressDialog.setHeaderText(null);
       addressDialog.setTitle("Registration");
       addressDialog.setContentText("Enter Address:");
       String address = addressDialog.showAndWait().orElse("");
       TextInputDialog zipCodeDialog = new TextInputDialog();
       zipCodeDialog.setHeaderText(null);
       zipCodeDialog.setTitle("Registration");
       zipCodeDialog.setContentText("Enter Zip Code:");
       String zipCode = zipCodeDialog.showAndWait().orElse("");
       TextInputDialog stateDialog = new TextInputDialog();
       stateDialog.setHeaderText(null);
       stateDialog.setTitle("Registration");
       stateDialog.setContentText("Enter State:");
       String state = stateDialog.showAndWait().orElse("");
       TextInputDialog usernameDialog = new TextInputDialog();
       usernameDialog.setHeaderText(null);
       usernameDialog.setTitle("Registration");
       usernameDialog.setContentText("Enter Username:");
       username = usernameDialog.showAndWait().orElse("");
       TextInputDialog passwordDialog = new TextInputDialog();
       passwordDialog.setHeaderText(null);
       passwordDialog.setTitle("Registration");
       passwordDialog.setContentText("Enter Password:");
       String password = passwordDialog.showAndWait().orElse("");
       TextInputDialog emailDialog = new TextInputDialog();
       emailDialog.setHeaderText(null);
       emailDialog.setTitle("Registration");
       emailDialog.setContentText("Enter Email:");
       String email = emailDialog.showAndWait().orElse("");
       TextInputDialog ssnDialog = new TextInputDialog();
       ssnDialog.setHeaderText(null);
       ssnDialog.setTitle("Registration");
       ssnDialog.setContentText("Enter SSN:");
       String ssn = ssnDialog.showAndWait().orElse("");
       TextInputDialog securityQuestionDialog = new TextInputDialog();
       securityQuestionDialog.setHeaderText(null);
       securityQuestionDialog.setTitle("Registration");
       securityQuestionDialog.setContentText("Enter Security Question:");
       String securityQuestion = securityQuestionDialog.showAndWait().orElse("");
       TextInputDialog securityAnswerDialog = new TextInputDialog();
       securityAnswerDialog.setHeaderText(null);
       securityAnswerDialog.setTitle("Registration");
       securityAnswerDialog.setContentText("Enter Security Answer:");
       String securityAnswer = securityAnswerDialog.showAndWait().orElse("");
    
       // Display a dialog to choose the account type (Customer or Admin)
       Alert typeDialog = new Alert(Alert.AlertType.CONFIRMATION);
       typeDialog.setHeaderText(null);
       typeDialog.setTitle("Account Type");
       typeDialog.setContentText("Is this a Customer or Admin account?");
       ButtonType customerButton = new ButtonType("Customer");
       ButtonType adminButton = new ButtonType("Admin");
       typeDialog.getButtonTypes().setAll(customerButton, adminButton);
       
       UserInterface newUser;
       Optional<ButtonType> result = typeDialog.showAndWait();
       
       // Set a default value for userType
       String userType = "Customer";
   
       // Check the chosen account type and create a new user accordingly
       if (result.isPresent() && result.get() == customerButton) {
           userType = "Customer";
           newUser = new Customer(firstName, lastName, address, zipCode, state, username, password, email, ssn, securityQuestion, securityAnswer, userType);
           newUser.addToDatabase(firstName, lastName, address, zipCode, state, username, password, email, ssn, securityQuestion, securityAnswer, "Customer");
       } else if (result.isPresent() && result.get() == adminButton) {
           userType = "Admin";
           newUser = new Admin(firstName, lastName, address, zipCode, state, username, password, email, ssn, securityQuestion, securityAnswer,userType);
           newUser.addToDatabase(firstName, lastName, address, zipCode, state, username, password, email, ssn, securityQuestion, securityAnswer, "Admin");
       } else {
    	// Display error alert for an invalid choice
           Alert invalidChoiceAlert = new Alert(Alert.AlertType.ERROR);
           invalidChoiceAlert.setHeaderText(null);
           invalidChoiceAlert.setTitle("Invalid Choice");
           invalidChoiceAlert.setContentText("Invalid choice. User registration failed.");
           invalidChoiceAlert.showAndWait();
           return;
       }
    
       // Add the registered user to a collection (registeredUsers)
       registeredUsers.put(username, newUser);
       

       // After newUser.addToDatabase, call displayUserID to show the user ID
       try {
           displayUserID(username);
       } catch (SQLException e) {
           e.printStackTrace();
       }
  
       // Display success alert for a successful user registration
       Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
       successAlert.setHeaderText(null);
       successAlert.setTitle("Registration Successful");
       successAlert.setContentText("User registered successfully!");
       successAlert.showAndWait();
   }
   protected static void loginUser() {
	    // Display prompt for username input
	    System.out.println("\nLogin:");
	    TextInputDialog usernameDialog = new TextInputDialog();
	    usernameDialog.setHeaderText(null);
	    usernameDialog.setTitle("Login");
	    usernameDialog.setContentText("Enter Username:");
	    
	    // Retrieve the entered username or an empty string if canceled
	    String loginUsername = usernameDialog.showAndWait().orElse("");

	    // Display prompt for password input
	    TextInputDialog passwordDialog = new TextInputDialog();
	    passwordDialog.setHeaderText(null);
	    passwordDialog.setTitle("Login");
	    passwordDialog.setContentText("Enter Password:");
	    
	    // Retrieve the entered password or an empty string if canceled
	    String loginPassword = passwordDialog.showAndWait().orElse("");

	    // Check user credentials and display appropriate alert
	    if (checkUserCredentials(loginUsername, loginPassword)) {
	        // Display success alert for a successful login
	        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
	        successAlert.setHeaderText(null);
	        successAlert.setTitle("Login Successful");
	        successAlert.setContentText("Login successful!");
	        successAlert.showAndWait();
	    } else {
	        // Display error alert for invalid username or password
	        Alert failureAlert = new Alert(Alert.AlertType.ERROR);
	        failureAlert.setHeaderText(null);
	        failureAlert.setTitle("Login Failed");
	        failureAlert.setContentText("Invalid username or password. Login failed.");
	        failureAlert.showAndWait();
	    }
	}
   private static boolean checkUserCredentials(String username, String password) {
       try {
           String checkCredentialsQuery = "SELECT * FROM LoopNestUser WHERE Username = ? AND Password = ?";
           try (PreparedStatement pstmt = conn.prepareStatement(checkCredentialsQuery)) {
               pstmt.setString(1, username);
               pstmt.setString(2, password);
               try (ResultSet resultSet = pstmt.executeQuery()) {
                   if (resultSet.next()) {
                       return true;  // Credentials are correct
                   }
               }
           }
       } catch (SQLException e) {
           System.out.println("Error checking credentials: " + e.getMessage());
       }
      
       return false;  // Credentials are incorrect or an error occurred
   }
   static void passwordRecovery() {
	    // Display prompt for username input
	    System.out.println("\nPassword Recovery:");
	    TextInputDialog usernameDialog = new TextInputDialog();
	    usernameDialog.setHeaderText(null);
	    usernameDialog.setTitle("Password Recovery");
	    usernameDialog.setContentText("Enter Username:");
	    
	    // Retrieve the entered username or an empty string if canceled
	    String recoveryUsername = usernameDialog.showAndWait().orElse("");

	    try {
	        // SQL query to select user information based on the provided username
	        String selectUserQuery = "SELECT Password, SecurityQuestion, SecurityAnswer FROM LoopNestUser WHERE Username = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(selectUserQuery)) {
	            // Set the parameter for the prepared statement (username)
	            pstmt.setString(1, recoveryUsername);

	            // Execute the query and process the result set
	            try (ResultSet resultSet = pstmt.executeQuery()) {
	                // Check if a result is found
	                if (resultSet.next()) {
	                    // Retrieve user information from the result set
	                    String storedPassword = resultSet.getString("Password");
	                    String storedSecurityQuestion = resultSet.getString("SecurityQuestion");
	                    String storedSecurityAnswer = resultSet.getString("SecurityAnswer");

	                    // Display prompt for security answer input
	                    TextInputDialog securityAnswerDialog = new TextInputDialog();
	                    securityAnswerDialog.setHeaderText(null);
	                    securityAnswerDialog.setTitle("Password Recovery");
	                    securityAnswerDialog.setContentText("Security Question: " + storedSecurityQuestion + "\nEnter Security Answer:");

	                    // Retrieve the entered security answer or an empty string if canceled
	                    String userAnswer = securityAnswerDialog.showAndWait().orElse("");

	                    // Check if the provided security answer matches the stored answer
	                    if (storedSecurityAnswer.equalsIgnoreCase(userAnswer)) {
	                        // Display success alert with the recovered password
	                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
	                        successAlert.setHeaderText(null);
	                        successAlert.setTitle("Password Recovery Successful");
	                        successAlert.setContentText("Your Password is: " + storedPassword);
	                        successAlert.showAndWait();
	                    } else {
	                        // Display error alert for invalid security answer
	                        Alert failureAlert = new Alert(Alert.AlertType.ERROR);
	                        failureAlert.setHeaderText(null);
	                        failureAlert.setTitle("Password Recovery Failed");
	                        failureAlert.setContentText("Invalid security answer. Password recovery failed.");
	                        failureAlert.showAndWait();
	                    }
	                } else {
	                    // Display error alert for username not found
	                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR);
	                    notFoundAlert.setHeaderText(null);
	                    notFoundAlert.setTitle("Password Recovery Failed");
	                    notFoundAlert.setContentText("Username not found. Password recovery failed.");
	                    notFoundAlert.showAndWait();
	                }
	            }
	        }
	    } catch (SQLException e) {
	        // Handle SQLException by printing the error message
	        System.out.println(e.getMessage());
	    }
	}
   private static int displayUserID(String username) throws SQLException {
	    // Initialize user ID to a default value of -1
	    int userId = -1;

	    // SQL query to select user ID based on the provided username
	    String selectUserIdQuery = "SELECT UserID FROM LoopNestUser WHERE Username = ?";

	    // Use try-with-resources to automatically close resources (PreparedStatement and ResultSet)
	    try (PreparedStatement pstmt = conn.prepareStatement(selectUserIdQuery)) {
	        // Set the parameter for the prepared statement (username)
	        pstmt.setString(1, username);

	        // Execute the query and process the result set
	        try (ResultSet resultSet = pstmt.executeQuery()) {
	            // Check if a result is found
	            if (resultSet.next()) {
	                // Retrieve the user ID from the result set
	                userId = resultSet.getInt("UserID");

	                // Display an informational alert with the user ID
	                Alert userIdAlert = new Alert(Alert.AlertType.INFORMATION);
	                userIdAlert.setHeaderText(null);
	                userIdAlert.setTitle("User ID");
	                userIdAlert.setContentText("User ID: " + userId);
	                userIdAlert.showAndWait();
	            }
	        }
	    }

	    // Return the retrieved user ID or the default value if not found
	    return userId;
	}
}