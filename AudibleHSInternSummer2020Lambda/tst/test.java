import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        List<String> test = new ArrayList<>();
        test.add("Test1");
        test.add("Test2");
        test.add("Test3");
        test.add("Test4");
        int currBook = 0;
        System.out.println(test.size());
        System.out.println(test.get(test.size() - 1));
        for (String t: test) {
            if (test.size() == 1) {
                System.out.println("FIRST");
            }
            else if (currBook == test.size() - 1) {
                System.out.println("LAST RUN " + test.get(currBook));
            } else {
                System.out.println("ELSE");
            }
            System.out.println(currBook);
            System.out.println(test.get(currBook));
            currBook++;
        }

    }
}
