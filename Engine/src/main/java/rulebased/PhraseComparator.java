package rulebased;

import java.util.Comparator;

/**
 * helper class to combine the splitted words in correct order.
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class PhraseComparator implements Comparator<String> {

	public int compare(String o1, String o2) {
		int number1 = Integer.parseInt(o1.split("-")[1]);
		int number2 = Integer.parseInt(o2.split("-")[1]);

		if (number1 > number2) {
			return 1;
		} else {
			return -1;
		}
	}

}
