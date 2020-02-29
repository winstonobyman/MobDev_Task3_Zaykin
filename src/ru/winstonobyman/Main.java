package ru.winstonobyman;


import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**Можно было делать проект на maven или gradle, но это как стрелять из пушки по воробьям.
 * Поэтому просто добавляю джарник с Gson в Project Structure в IntelliJ IDEA
 * jar отсюда: https://search.maven.org/artifact/com.google.code.gson/gson/2.8.6/jar
 */
public class Main {

    final static int CONNECTION_TIMEOUT = 120;

    public static void main(String[] args) throws IOException {

        String urlString = "https://ru.wikipedia.org/w/api.php" +
                "?action=query&amp%3Blist=search&amp%3Butf8=&amp%3Bformat=json&amp%3Bsrsearch=Java&format=json";

        urlString = getUserQuery();

        String result = getQueryResult(urlString);
        System.out.println(result);

//        Gson gson = new Gson().fromJson(result, )

        }

    public static String getQueryResult(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        Scanner sc = new Scanner(new InputStreamReader(connection.getInputStream())).useDelimiter("\\A");
        String result = sc.hasNext() ? sc.next() : "";
        return result;
        }

    public static void readInput() {
        String userQuery;
        while (true) {
            userQuery = getUserQuery();
            if (userQuery.equals(""))
                break;
            System.out.println(userQuery);
        }
    }

    public static String getUserQuery() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите ваш запрос: ");
        String userQuery = sc.nextLine();

        return userQuery;
    }

    public static String generateQuery(String userInput) throws UnsupportedEncodingException {
        String urlString = "https://ru.wikipedia.org/w/api.php?action=query&list=search" +
                "&format=json&utf8=&srlimit=1&srsearch=Java";

        String input = URLEncoder.encode(getUserQuery(), "UTF-8");

        return urlString + input;
    }
}
