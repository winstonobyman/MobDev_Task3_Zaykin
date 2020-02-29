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

        String urlString;

        String question = getUserQuestion();
        urlString = generateQuery(question);

        String result = getQueryResult(urlString);
        System.out.println(result);

//        Gson gson = new Gson().fromJson(result, )

        }

    public static String getQueryResult(String urlString) throws IOException {
        System.out.println("Main.getQueryResult");
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        Scanner sc = new Scanner(new InputStreamReader(connection.getInputStream())).useDelimiter("\\A");
        String result = sc.hasNext() ? sc.next() : "";
        return result;
        }

    public static String getUserQuestion() {
        System.out.println("Main.getUserQuestion");
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите ваш запрос: ");
        String userQuestion = sc.nextLine();
        // Очищаем строку, чтобы не было пробельных и им подобных символов
        userQuestion = userQuestion.replaceAll("\\s", "");
        if (userQuestion.equals("")) {
            throw new IllegalArgumentException("Был введён пустой запрос");
        }
        return userQuestion;
    }

    public static String generateQuery(String userInput) throws UnsupportedEncodingException {
        System.out.println("Main.generateQuery");
        String urlString = "https://ru.wikipedia.org/w/api.php?action=query&list=search" +
                "&format=json&utf8=&srlimit=1&srsearch=";

        String input = URLEncoder.encode(userInput, "UTF-8");
        System.out.println(urlString + input);
        return urlString + input;
    }
}
