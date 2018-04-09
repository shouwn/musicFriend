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
		Set<Integer> intersection = Sets.intersection(original, update); // 교집합
		Set<Integer> removeGroup = Sets.difference(original, intersection); // original - 교집함
		Set<Integer> addGroup = Sets.difference(update, intersection); // update - 교집합

		deletGroupLink(removeGroup, controller); // 삭제될 노래들의 링크는 모두 삭제
		addGroupLink(addGroup, controller); // 추가될 노래들의 링크는 모두 추가 

		for(Integer i : intersection) { // 교집합을 돌면서
			
			for(Integer j : addGroup) { // 추가할 노래들은 교집합에 추가하고
				//DB 에서 입력 받은 Set 에 들어있는 노래 id 들의
				//링크를 돌아가면서 그 두 노래 간의 링크 점수를 select 하는 문장
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, i, j);

				// 만약 score를 얻어낼 수 없었다면 링크가 없었다는 것으로
				if(score == null)
					makeLink(i, j, controller.getWriter());// 링크를 새로 만든다
				else
					updateLink(UP, score, i, j, controller.getWriter());// 아니면 링크 점수를 상수 만큼 증가
			}
			for(Integer k : removeGroup) { // 삭제할 노래들은 교집합과의 링크 삭제
				//DB 에서 입력 받은 Set 에 들어있는 노래 id 들의
				//링크를 돌아가면서 그 두 노래 간의 링크 점수를 select 하는 문장
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, i, k);

				// 만약 상수치랑 같다면 링크가 1개만 존재하는 것이므로
				if(score == SCORE) // 링크 레코드 삭제
					deletLink(i, k, controller.getWriter());
				else // 아니면 링크 레코드 업데이트
					updateLink(DOWN, score, i, k, controller.getWriter());
			}
		}
	}

	
	/**
	 * <p> 삭제할 집합을 받아서 그 집합들 간의 링크를 제거 하는 메소드
	 * @param set 삭제할 그룹들의 집합
	 * @param controller DB 입출력을 위한 컨트롤러
	 */
	private static void deletGroupLink(Set<Integer> set, DBController controller) {
		Integer[] keys = (Integer[])set.toArray(); // set을 선형 탐색하기 위해 toArray로 배열로 만듬

		for(int i = 0; i < keys.length - 1; i++) {
			for(int j = i + 1; j < keys.length - 1; j++) {
				//DB 에서 입력 받은 Set 에 들어있는 노래 id 들의
				//링크를 돌아가면서 그 두 노래 간의 링크 점수를 select 하는 문장
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, keys[i], keys[j]);

				// 만약 상수치랑 같다면 링크가 1개만 존재하는 것이므로
				if(score == SCORE) // 링크 레코드 삭제
					deletLink(keys[i], keys[j], controller.getWriter());
				else // 아니면 링크 레코드 없데이트
					updateLink(DOWN, score, keys[i], keys[j], controller.getWriter());
			}
		}
	}

	/**
	 * <p> 추가할 집합을 받아서 그 집합들 간의 링크를 추가 하는 메소드
	 * @param set 추가할 그룹들의 집합
	 * @param controller DB 입출력을 위한 컨트롤러
	 */
	private static void addGroupLink(Set<Integer> set, DBController controller) {
		Integer[] keys = (Integer[])set.toArray(); // set을 선형 탐색하기 위해 toArray로 배열로 만듬

		for(int i = 0; i < keys.length - 1; i++) {
			for(int j = i + 1; j < keys.length; j++) {
				//DB 에서 입력 받은 Set 에 들어있는 노래 id 들의
				//링크를 돌아가면서 그 두 노래 간의 링크 점수를 select 하는 문장
				Integer score = controller.getReader().select(DBUtils.LINK_TABLE, keys[i], keys[j]);

				// 만약 score를 얻어낼 수 없었다면 링크가 없었다는 것으로
				if(score == null)
					makeLink(keys[i], keys[j], controller.getWriter()); // 링크를 새로 만든다
				else
					updateLink(UP, score, keys[i], keys[j], controller.getWriter()); // 아니면 링크 점수를 상수 만큼 증가
			}
		}
	}

	//parameter mID = music ID, oID = other music ID
	//order 매개변수를 통해 해당 두 music id - mID와 oID 사이의 링크를 업데이트 하는 메소드
	//SCORE 상수 값 만큼 점수를 더하고 빼준다.
	private static void updateLink(int order, int score,int mID, int oID, DBWriter writer) {

		if(order == UP) {
			writer.update(DBUtils.LINK_TABLE, mID, oID, score + SCORE); //sql 문으로 update
			writer.update(DBUtils.LINK_TABLE, oID, mID, score + SCORE); //sql 문으로 update
		} else {
			writer.update(DBUtils.LINK_TABLE, mID, oID, score - SCORE); //sql 문으로 update
			writer.update(DBUtils.LINK_TABLE, oID, mID, score - SCORE); //sql 문으로 update
		}
	}

	// sql 문으로 두 링크 간의 링크 레코드를 insert 하는 메소드
	private static void makeLink(int mID, int oID, DBWriter writer) {
		writer.insert(DBUtils.LINK_TABLE, mID, oID, 1);
		writer.insert(DBUtils.LINK_TABLE, oID, mID, 1);
	}

	// sql 문으로 두 링크 간의 링크 레코드를 delete 하는 메소드
	private static void deletLink(int mID, int oID, DBWriter writer) {
		writer.delet(DBUtils.LINK_TABLE, mID, oID);
		writer.delet(DBUtils.LINK_TABLE, oID, mID);
	}

}
