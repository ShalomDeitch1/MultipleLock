package bank;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import static bank.MultiLocker.*;

public class Bank {
	private Account ownBalance; //guarded by ownBalance
	private ConcurrentHashMap<String, Account>clients = new ConcurrentHashMap<>();
	private TransactionLog log = new TransactionLog();
	
	public Bank(Double startAmount) {
		Account ownAccount = new Account("Bank", startAmount);
		this.ownBalance = ownAccount;
		log.add(new Transaction(ownAccount, null, startAmount, true));
	}

	public void addAccount(String name, Double startBalance) {
		Account newAccount = new Account(name, startBalance);
		synchronized (newAccount) { //needed for the log :-(
			clients.put(name, newAccount);
			log.add(new Transaction(newAccount, null, startBalance, true));
		}
	}
	
	public Account getAccount(String name) {
		return clients.get(name);
	}
	
	public double getBanksAssets() {
		synchronized (ownBalance) {
			return ownBalance.getBalance();
		}
	}
	
	//see note on payLoan
	public boolean borrowLoan(Account account, double amount) {
		return  lock(ownBalance, account,
						() -> {
							if ((amount <= 0) || (ownBalance.getBalance() < amount)) {
								log.add(new Transaction(ownBalance, account, amount, false));
								return false;
							}
							ownBalance.updateBalance(-amount);
							account.updateBalance(amount);
							log.add(new Transaction(ownBalance, account, amount, true));
							return true;
						});
	}	
	
	//this can be silly, the payment may be more then owned, to fix must record how much borrowed
	public boolean payLoan(Account account, double amount) {
		return lock(ownBalance, account, ()-> {
			if ((amount <= 0) || (account.getBalance() < amount)) {
				log.add(new Transaction(account, ownBalance, amount, false));
				return false;
			}
			account.updateBalance(-amount);
			ownBalance.updateBalance(amount);
			log.add(new Transaction(account, ownBalance, amount, true));
			return true;
		});
	}
	
	public boolean transfere(Account from, Account to, double amount) {
		return lock(from, to, ()-> {
			if ((amount <= 0) || (from.getBalance() < amount)) {
				log.add(new Transaction(from, to, amount, false));
				return false;
			}
			from.updateBalance(-amount);
			to.updateBalance(amount);
			log.add(new Transaction(from, to, amount, true));
			return true;
		});		
	}

	public Iterable<Transaction> getLog() {
		return new Iterable<Transaction>() {
			
			@Override
			public Iterator<Transaction> iterator() {
				return log.iterateValues();
			}
		};
	}
}
