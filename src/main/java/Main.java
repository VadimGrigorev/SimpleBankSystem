import java.sql.*;
import java.util.*;

public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static HashMap<String, Card> cards = new LinkedHashMap<>();
    private static Random random = new Random();
    private static boolean exit = false;
    private static String fileName = "defaultSBS.db";
    private static Dao dao;

    public static void main(String[] args) throws SQLException {

        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-fileName")){
                fileName = args[i+1];
            }
        }
        dao = new Dao(fileName);

        createTableAndLoadCards();

        while(true) {
            printMenu();

            String action = "";
            while(action.equals("")) {
                action = scanner.nextLine();
            }

            if(action.equals("1")){
                createCard();
            }
            else if(action.equals("2")){
                logAccount();
                if(exit){
                    break;
                }
            }
            else if(action.equals("0")){
                break;
            }
        }
    }

    private static void logAccount() throws SQLException {
        System.out.println("\nEnter your card number:");
        String card = scanner.nextLine();

        System.out.println("\nEnter your PIN:");
        String pin = scanner.nextLine();

        if(cards.keySet().contains(card)){
            if(cards.get(card).getPin().equals(pin)){
                System.out.println("\nYou have successfully logged in!\n");
                inAccount(cards.get(card));
            }
            else{
                System.out.println("\nWrong card number or PIN!\n");
            }
        }
        else{
            System.out.println("\nWrong card number or PIN!\n");
        }
    }

    private static void inAccount(Card card) throws SQLException {

        while(true){
            System.out.println("1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Delete account\n" +
                    "5. Log out\n" +
                    "0. Exit");
            String input = "";
            while(input.equals("")){
                input = scanner.nextLine();
            }
            int action = Integer.parseInt(input);
            if(action == 1){
                System.out.println("Balance: "+card.getBalance()+"\n");
            }
            else if(action == 2){
                System.out.println("\nhow much money do you want to deposit?");
                int deposit = Integer.parseInt(scanner.nextLine());
                card.setBalance(card.getBalance()+deposit);
                dao.updateBalance(card.getCardNumber(), card.getBalance());
                System.out.println("\noperation successful\n");
            }
            else if(action == 3){
                Card receiver = null;

                while(true) {
                    System.out.println("\nenter the card number you want your money to get transferred to(enter '0' to cancel)\n");
                    String cardToTransfer = scanner.nextLine();
                    if(cardToTransfer.equals("0")){
                        break;
                    }
                    if (cardToTransfer.equals(card.getCardNumber())) {
                        System.out.println("\nYou can't transfer money to the same account!\n");
                        continue;
                    }
                    if (!checkLuhn(cardToTransfer)) {
                        System.out.println("\nProbably you made a mistake in card number. Please try again!\n");
                        continue;
                    }
                    if(!cards.keySet().contains(cardToTransfer)){
                        System.out.println("\nSuch a card does not exist.\n");
                        continue;
                    }
                    System.out.println("\nenter the amount of money you want to transfer\n");

                    int moneyToTransfer = 999999;
                    try {
                        moneyToTransfer = Integer.parseInt(scanner.nextLine());
                    }
                    catch(Exception exc){
                        System.out.println("\nInvalid Value\n");
                        break;
                    }
                    if(card.getBalance() < moneyToTransfer){
                        System.out.println("\nyou don't have enough money to complete operation\n");
                        break;
                    }
                    receiver = cards.get(cardToTransfer);
                    int changedValueReceiver = receiver.getBalance() + Math.abs(moneyToTransfer);
                    int changedValueSender = card.getBalance() - Math.abs(moneyToTransfer);

                    receiver.setBalance(changedValueReceiver);
                    card.setBalance(changedValueSender);

                    dao.updateBalance(card.getCardNumber(), card.getBalance());
                    dao.updateBalance(receiver.getCardNumber(), receiver.getBalance());

                    System.out.println("transaction complete\n");
                    break;
                }

            }
            else if(action == 4){
                dao.deleteCard(card.getCardNumber());
                cards.remove(card.getCardNumber());
                System.out.println("\nyour card was successfully removed");
                break;
            }
            else if(action == 5){
                System.out.println("\nYou have successfully logged out!\n");
                break;
            }
            else if(action == 0){
                exit = true;
                break;
            }

        }
    }
    private static void createTableAndLoadCards(){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS card " +
                    "(id INTEGER, " +
                    "number TEXT, " +
                    "pin TEXT," +
                    "balance INTEGER," +
                    "UNIQUE (number));");
            ResultSet result = statement.executeQuery("SELECT number,pin,balance FROM card");
            while(result.next()){
                String card = result.getString(1);
                String pin = result.getString(2);
                int balance = result.getInt(3);
                if(!cards.keySet().contains(card)){
                    Card account = new Card(card, pin, balance);
                    cards.put(card, account);
                }
            }


        }
        catch(Exception exc){
            System.out.println(exc);
        }
    }

    private static void printMenu(){
        System.out.println("1. Create account\n" +
                "2. Log into account\n" +
                "0. Exit");
    }

    private static String createCard() throws SQLException {
        int[] card = {4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] pin = new int[4];
        String cardAsString;

        //generate card number
        while(true) {
            int sum = 0;
            for (int i = 0; i < card.length - 1; i++) {
                if (i >= 6) {
                    card[i] = random.nextInt(10);
                }
                sum += card[i];
            }
            int sum1 = sum;
            while (true) {
                if (sum1 % 10 == 0) {
                    break;
                }
                sum1++;
            }
            card[15] = sum1 - sum;

            //convert card number to String
            StringBuilder sb = new StringBuilder();
            for(int i : card){
                sb.append(i);
            }
            cardAsString = sb.toString();
            if(checkLuhn(cardAsString)){
                break;
            }
        }

        //generate pin
        for(int i = 0; i < pin.length; i++){
            if(i == 0){
                int firstDigit = 0;
                while(firstDigit == 0){
                    firstDigit = random.nextInt(10);
                }
                pin[i] = firstDigit;
                continue;
            }
            pin[i] = random.nextInt(10);
        }

        //convert pin to String
        StringBuilder sb2 = new StringBuilder();
        for(int i : pin){
            sb2.append(i);
        }
        String pinAsString = sb2.toString();

        //generate account
        Card newAccount = new Card(cardAsString, pinAsString, 0);
        cards.put(cardAsString, newAccount);
        System.out.println("\nYour card has been created\n" +
                "Your card number:\n" +
                cardAsString + "\n" +
                "\nYour card PIN:\n" +
                pinAsString+ "\n");

        dao.saveCardToDb(cards.get(cardAsString));

        return cardAsString;
    }

    private static boolean checkLuhn(String number){
        char[] chars = number.toCharArray();
        int count = 1;
        int sum = 0;
        for(int i = 0; i < chars.length; i++){
            String digit = String.valueOf(chars[i]);
            int digit2 = Integer.parseInt(digit);

            if(count % 2 == 1){
                digit2 = digit2 * 2;
                if(digit2 > 9){
                    digit2 -= 9;
                }
            }

            sum += digit2;
            count++;
        }
        if(sum % 10 == 0){
            return true;
        }
        return false;
    }
}
