package musicFriend;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import musicFriend.db.DBController;
import musicFriend.db.DBUtils;
import musicFriend.db.DBWriter;

public class LinkUtils {

	private static final int SCORE = 1;
	private static final int UP = 1; 
	private static final int DOWN = -1;

	/**
	 * <p> 기존 음악과 바뀐 음악을 매개변수로 받아 링크를 정리하는 메소드
	 * @param original 기존에 좋아요 눌렀던 음악들의 ID
	 * @param update 새롭게 좋아요를 눌른 음악들의 ID
	 * @param controller DB 입출력을 위한 컨트롤러 객체
	 */
	public static void update(int[] original, int[] update, DBController controller) {
		Set<Integer> ori = new HashSet<>(); // original 
		Set<Integer> up = new HashSet<>(); // update
		System.out.println();

		for(int i : original)
			ori.add(i);
		for(int i : update)
			up.add(i);

		update(ori, up, controller);
	}

	/**
	 * <p> 기존 음악과 바뀐 음악을 매개변수로 받아 링크를 정리하는 메소드
	 * @param original 기존에 좋아요 눌렀던 음악들의 ID
	 * @param update 새롭게 좋아요를 눌른 음악들의 ID
	 * @param controller DB 입출력을 위한 컨트롤러 객체
	 */
	public static void update(List<Integer> original, List<Integer> update, DBController controller) {
		Set<Integer> ori = new HashSet<>(original); // original 
		Set<Integer> up = new HashSet<>(update); // update

		update(ori, up, controller);
	}

	/**
	 * <p> 클래스 내부적으로 생성된 Set을 받아서 링크 테이블 최신화
	 * @param original 기존에 좋아요 눌렀던 음악들의 ID
	 * @param update 새롭게 좋아요를 눌른 음악들의 ID
	 * @param controller DB 입출력을 위한 컨트롤러 객체
	 */
	private static void update(Set<Integer> original, Set<Integer> update, DBController controller) {
		Set<Integer> intersection = Sets.intersection(original, update);
		Set<Integer> removeGroup = Sets.difference(original, intersection);
		Set<Integer> addGroup = Sets.difference(update, intersection);

		deletGroupLink(removeGroup, controller);
		addGroupLink(addGroup, controller);

		for(Integer i : intersection) {
			for(Integer j : addGroup) {
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, i, j);

				if(score == null)
					makeLink(i, j, controller.getWriter());
				else
					updateLink(UP, score, i, j, controller.getWriter());
			}
		}
	}

	
	/**
	 * <p> 삭제할 집합을 받아서 그 집합들 간의 링크를 제거 하는 메소드
	 * @param set 삭제할 그룹들의 집합
	 * @param controller DB 입출력을 위한 컨트롤러
	 */
	private static void deletGroupLink(Set<Integer> set, DBController controller) {
		Integer[] keys = (Integer[])set.toArray();

		for(int i = 0; i < keys.length - 1; i++) {
			for(int j = i + 1; j < keys.length - 1; j++) {
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, i, j);

				if(score == 1)
					deletLink(i, j, controller.getWriter());
				else
					updateLink(DOWN, score, i, j, controller.getWriter());
			}
		}
	}

	/**
	 * <p> 추가할 집합을 받아서 그 집합들 간의 링크를 추가 하는 메소드
	 * @param set 추가할 그룹들의 집합
	 * @param controller DB 입출력을 위한 컨트롤러
	 */
	private static void addGroupLink(Set<Integer> set, DBController controller) {
		Integer[] keys = (Integer[])set.toArray();

		for(int i = 0; i < keys.length - 1; i++) {
			for(int j = i + 1; j < keys.length; j++) {
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, i, j);

				if(score == null)
					makeLink(i, j, controller.getWriter());
				else
					updateLink(UP, score, i, j, controller.getWriter());
			}
		}
	}

	//parameter mID = music ID, oID = other music ID
	private static void updateLink(int order, int score,int mID, int oID, DBWriter writer) {

		if(order == UP) {
			writer.update(DBUtils.LINK_TABLE, mID, oID, score + SCORE);
			writer.update(DBUtils.LINK_TABLE, oID, mID, score + SCORE);
		} else {
			writer.update(DBUtils.LINK_TABLE, mID, oID, score - SCORE);
			writer.update(DBUtils.LINK_TABLE, oID, mID, score - SCORE);
		}
	}

	private static void makeLink(int mID, int oID, DBWriter writer) {
		writer.insert(DBUtils.LINK_TABLE, mID, oID, 1);
		writer.insert(DBUtils.LINK_TABLE, oID, mID, 1);
	}

	private static void deletLink(int mID, int oID, DBWriter writer) {
		writer.delet(DBUtils.LINK_TABLE, mID, oID);
		writer.delet(DBUtils.LINK_TABLE, oID, mID);
	}

}
