package musicFriend.db;

public interface DBReader {
	
	<T> T select(int table, Object... requset);
}