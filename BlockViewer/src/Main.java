import connection.BitcoinVersionRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Main {
    public static void main(String... args) {
    //    int[] ip = {51,195,28,51}; // 185.197.160.61
       int[] ip = {185,197,160,61}; // 185.197.160.61


        var req = new BitcoinVersionRequest();
        req.connect(ip);

        System.out.println("Hey");
    }
}
