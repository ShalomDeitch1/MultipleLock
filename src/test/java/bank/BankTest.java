package bank;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

public class BankTest {

	@Test
	public void singleThreadTest() {
		Bank bank = new Bank(1000.0);
		bank.addAccount("fred", 100.0);
		assertEquals(bank.getAccount("fred").getBalance(), 100.0, 0.0001);
		
		bank.addAccount("tim", 50.0);
		assertEquals(bank.getAccount("tim").getBalance(), 50.0, 0.0001);
		
		Account fredAccount = bank.getAccount("fred");
		bank.borrowLoan(fredAccount, 25.0);
		assertEquals(bank.getBanksAssets(), 975.0, 0.0001);
		assertEquals(fredAccount.getBalance(), 125.0, 0.0001);
		
		Account timAccount = bank.getAccount("tim");
		bank.transfere(fredAccount, timAccount, 25.0);
		assertEquals(fredAccount.getBalance(), 100.0, 0.0001);
		assertEquals(timAccount.getBalance(), 75.0, 0.0001);
		
		//have not dealt with cases where there is not enough in the account
		for ( Transaction transaction : bank.getLog()) {
			System.out.println(transaction);
		}
	}

	///---------------------------------------------------------------------
	@Test
	public void multithreaded() {
		TransactionFactory factory = new TransactionFactory();
		
		Bank bank = new Bank(1_000_000.0);
		for (String name : factory.getAccountNames()) {
			bank.addAccount(name, 1_000.0);
		}

		ArrayList<Future<?>>submitted = new ArrayList<Future<?>>();
		
		ExecutorService pool = Executors.newCachedThreadPool();
		
		for (int i = 0; i < 1 /*Runtime.getRuntime().availableProcessors()*/; i++) {
			Future<?> future = pool.submit(new Runnable() {
				
				@Override
				public void run() {
				    for (int i = 0; i < 800_000; i++) {
				    	TransactionDetails transactionDetails = factory.create();		    	
				    	processTransaction(bank, transactionDetails);
				    }//for
				}
			});
			submitted.add(future);
		}
		
		for (Future<?>future : submitted) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
/*			
	    System.out.println("FROM: multithreaded");
	    int count = 0;
		for ( Transaction transaction : bank.getLog()) {
			count++;
       	 System.out.println(count + ") " + transaction);
		}

	    System.out.println("till here FROM: multithreaded");
*/
	}

	private void processTransaction(Bank bank,
			TransactionDetails transactionDetails) {
		switch(transactionDetails.getActionName()) {
		case "BORROW": {
			Account account = bank.getAccount(transactionDetails.getFromName());
			bank.borrowLoan(account, transactionDetails.getSum());
			break;
			}
		
		case "REPAY": {
			Account account = bank.getAccount(transactionDetails.getFromName());
			bank.payLoan(account, transactionDetails.getSum());
			break;
		}
		
		case "TRANSFER": {
			Account fromAccount = bank.getAccount(transactionDetails.getFromName());
			Account toAccount = bank.getAccount(transactionDetails.getToName());
			bank.transfere(fromAccount, toAccount, transactionDetails.getSum());
			break;
		}
		
		}//switch
	}
	
	//------------------------
	private class TransactionFactory {
		//private final String[] accountNames = {"John", "Tim", "Ian", "Martin"};
		private final String[] accountNames;
		private final String[] actions = {"BORROW", "REPAY", "TRANSFER", "TRANSFER", "TRANSFER"}; //give transfer a higher probability
		
		
		public TransactionFactory() {
			ArrayList<String>names = new ArrayList<String>();
			for (int i = 0; i < 1000; i++) {
				names.add("Account_" + i);
			}
			accountNames = names.toArray(new String[names.size()]);
		}

		public TransactionDetails create() {
			ThreadLocalRandom random = ThreadLocalRandom.current();

			int next = random.nextInt(0, accountNames.length);
			String fromName = accountNames[next];
			String toName = "";
			do {
				next = random.nextInt(0, accountNames.length);
				toName = accountNames[next];
			} while (fromName.equals(toName));
			
			next = random.nextInt(0, actions.length);
			String actionName = actions[next];
			
			Double sum = random.nextDouble(0, 150);			

			return new TransactionDetails(fromName, toName, actionName, sum);
		}
		
		public String[] getAccountNames() {
			return accountNames;
		}
	}
	private class TransactionDetails {
		
		private String fromName;
		private String toName;
		private Double sum;
		private String actionName;

		public TransactionDetails(String fromName, String toName, String actionName, Double sum) {
			this.fromName = fromName;
			this.toName = toName;
			this.actionName = actionName;
			this.sum = sum;
		}

		public String getFromName() {
			return fromName;
		}

		public String getToName() {
			return toName;
		}

		public Double getSum() {
			return sum;
		}

		public String getActionName() {
			return actionName;
		}
		
		@Override
		public String toString() {
			return String.format("fromName:" + fromName + " toName:" + toName + " actionName:" + actionName + " sum:%.2f", sum);
		}
	}
}
