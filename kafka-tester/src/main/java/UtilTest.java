import java.util.function.Function;

public class UtilTest {
    public static void main(String[] args) {
        Function<String, String> fn = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return "test";
            }
        };
        System.out.println(fn);
    }
}
