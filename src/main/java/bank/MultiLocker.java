package bank;


public class MultiLocker  {

	private static final Object tieBreakerLock = new Object();
	
	public static <T,R>  R lock(T objectA, T objectB, Action<T, R> action) {
		int hashA = System.identityHashCode(objectA);
		int hashB = System.identityHashCode(objectB);
		
		if (hashA < hashB) {
			synchronized (objectA) {
				synchronized (objectB) {
					return action.doAction();
				}
			}
		} else if (hashB < hashA) {
			synchronized (objectB) {
				synchronized (objectA) {
					return action.doAction();
				}
			}
		} else {
			synchronized (tieBreakerLock) {
				synchronized (objectA) {
					synchronized (objectB) {
						return action.doAction();
					}
				}			
			}
		}
	}

	//------------------------
	public interface Action<T, R> {

		R doAction();
		
	}
}
