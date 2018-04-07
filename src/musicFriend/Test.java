package musicFriend;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class Test {

	public static void main(String[] args) {
		
		Set<Integer> set1 = new HashSet<>();
		Set<Integer> set2 = new HashSet<>();
		
		for(int i = 0; i < 10; i++) {
			set1.add(i);
			set2.add(i + 5);
		}
		
		Set<Integer> intersection = Sets.intersection(set1, set2);
		Set<Integer> defference = Sets.difference(set1, intersection);

		System.out.println(set1);
		System.out.println(set2);
		System.out.println(intersection);
		System.out.println(defference);
	}

}
