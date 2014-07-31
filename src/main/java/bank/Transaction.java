package bank;

public class Transaction {
	private Account from;
	private Account to;
	private Double  sum;
	private boolean success;
	public Transaction(Account from, Account to, Double sum, boolean success) {
		if (from != null)
		  this.from = new Account(from.getName(), from.getBalance());
		if (to != null)
		  this.to = new Account(to.getName(), to.getBalance());;
		this.sum = sum;
		this.success = success;
	}
	
	public Account getFrom() {
		return from;
	}
	public Account getTo() {
		return to;
	}
	public Double getSum() {
		return sum;
	}
	public boolean getSuccess() {
		return success;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if (from != null)
			   sb.append("from: ").append(from.getName()).append(" (").append(String.format("%.2f", from.getBalance())).append(") ");
		
		if (to != null)
			   sb.append("to: ").append(to.getName()).append(" (").append(String.format("%.2f", to.getBalance())).append(") ");
		
		if (sum != null)
			   sb.append("sum: ").append(String.format("%.2f", sum)).append(" ");
		
		sb.append("succeeded: ").append(success);
		return sb.toString();
	}
}
