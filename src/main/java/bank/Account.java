package bank;


public class Account {
	private static final double IGNORE_LIMIT = 0.00001;
	private String name;
	private double balance;
	
	public Account(String name, double startBalance) {
		super();
		this.name = name;
		this.balance = startBalance;
	}
	
	public String getName() {
		return name;
	}
	
	public synchronized double getBalance() {
		return balance;
	}
	
	/**
	 * 
	 * @param addSum if credit - positive number
	 *               else is a debit
	 * @return if could do action
	 */
	public synchronized boolean updateBalance(Double addSum) {
		if (balance + addSum > 0 + IGNORE_LIMIT) {
			balance += addSum;
			return true;
		}
		
		return false;
	}
}
