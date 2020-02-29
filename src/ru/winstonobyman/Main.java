package ru.winstonobyman;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;


/**Можно было делать проект на maven или gradle, но это как стрелять из пушки по воробьям.
 * Поэтому просто добавляю джарник с Gson в Project Structure в IntelliJ IDEA
 * jar отсюда: https://search.maven.org/artifact/com.google.code.gson/gson/2.8.6/jar
 */
public class Main {

    final static int CONNECTION_TIMEOUT = 120; // параметр таймаута

    public static void main(String[] args) throws IOException {

        String urlString;

        String question = getUserRequest();
        urlString = generateQuery(question);

        String jsonQueryResult = getQueryResult(urlString);
        System.out.println(jsonQueryResult);
        JsonElement body = new JsonParser().parse(jsonQueryResult).getAsJsonObject().get("query");
        System.out.println("body = " + body);
        if (validateQuery(body)) {
            getSnippet(body);
        }

    }

    public static String getSnippet(JsonElement body) {
        System.out.println("Main.getSnippet");
        JsonElement snippetRaw = body.getAsJsonObject().get("search")
                .getAsJsonArray().get(0).getAsJsonObject().get("snippet");
        System.out.println(snippetRaw);
        return snippetRaw.toString();
    }

    public static boolean validateQuery(JsonElement body) {
        System.out.println("Main.validateQuery");
        JsonElement totalHits = body.getAsJsonObject().get("searchinfo").getAsJsonObject().get("totalhits");
        System.out.println("totalHits = " + totalHits);
        if (totalHits.getAsInt() == 0) {
            System.out.println("Не найдено ни одной статьи.");
            return false;
        } else {
            System.out.printf("Найдено %d статей. Вывод первой.\n", totalHits.getAsInt());
            return true;
        }

    }

    public static String getQueryResult(String urlString) throws IOException {
        System.out.println("Main.getQueryResult");
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        Scanner sc = new Scanner(new InputStreamReader(connection.getInputStream())).useDelimiter("\\A");
        String jsonString = sc.hasNext() ? sc.next() : "";
        return jsonString;
        }

    public static String getUserRequest() {
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
        // строка запроса.
        // StringBuilder для построения запроса  (удобнее добавлять и можно легче изменить)
        StringBuilder urlString = new StringBuilder("https://ru.wikipedia.org/w/api.php?");
        urlString.append("action=query") // делаем запрос
                .append("&list=search") // поиск списком
                .append("&format=json") // выдаем данные как json
                .append("&utf8=") //  ставим кодировку
                .append("&srlimit=1") // выводим только один элемент
                .append("&srprop=snippet") // среди свойств нужен отрывок из статьи
                .append("&srsearch=") // начало для запроса от нашего клиента
        ;
        String input = URLEncoder.encode(userInput, "UTF-8"); // кодируем то, что ввел пользователь в PercentEncoding
        System.out.println(urlString + input); // Соединяем тело запроса и запрос пользователя
        return urlString + input;
    }
}