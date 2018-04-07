package musicFriend.db;

public interface DBWriter {
	
	void insert(int table, Object... request);
	void delet(int table, Object... request);
	void update(int table, Object... request);
}
