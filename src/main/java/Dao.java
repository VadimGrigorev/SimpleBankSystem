import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class Dao{
    private Statement statement;

    public Dao(String fileName){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" +fileName);
            Statement statement = conn.createStatement();
            this.statement = statement;
        }
        catch(Exception exc){
            System.out.println(exc);
        }
    }

    public void updateBalance(String cardNumber, int change) throws SQLException {
        String query = String.format("UPDATE card " +
                "SET balance = %d " +
                "WHERE number = '%s'", change, cardNumber);
        statement.executeUpdate(query);
    }

    public void deleteCard(String cardNumber) throws SQLException {
        String query = String.format("DELETE FROM card " +
                "WHERE number LIKE '%s';", cardNumber);
        statement.executeUpdate(query);
    }

    public void saveCardToDb(Card card) throws SQLException {
        statement.executeUpdate("INSERT INTO card(number, pin, balance) " +
                "VALUES (" + card.getCardNumber() + ", " + card.getPin() + ", " + card.getBalance() +");");
    }
}