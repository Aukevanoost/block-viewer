import connection.BitcoinVersionRequest;

public class Main {
    public static void main(String... args) {
    //    int[] ip = {51,195,28,51}; // 185.197.160.61


        var req = new BitcoinVersionRequest();
        req.connect("185.197.160.61");
    }
}
