package ru.winstonobyman;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.text.html.HTML;
import java.io.*;
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
        if (validateQuery(body)) {
            int pageId = getPageID(body);
            String page = getPageById(pageId);
            FileWriter fw = new FileWriter("page.txt", false);
            fw.write(page);
            fw.flush();
        }


    }

    //https://en.wikipedia.org/w/api.php?action=parse&pageid=3276454&prop=wikitext&formatversion=2

    public static String getPageById(int pageId) throws IOException {
        // строка запроса.
        // StringBuilder для построения запроса  (удобнее добавлять и можно легче изменить)
        StringBuilder urlString = new StringBuilder("https://ru.wikipedia.org/w/api.php?");
        urlString.append("action=parse") // делаем запрос
                .append("&format=json") // выдаем данные как json
                .append("&utf8=")        //  ставим кодировку
                .append("&prop=text")   // выводим только один элемент
                .append("&formatversion=2") // среди свойств нужен отрывок из статьи
                .append("&pageid=" + pageId) // начало для запроса от нашего клиента
                ;
        String jsonPage = getQueryResult(urlString.toString());
//        System.out.println(jsonPage);
        jsonPage = new JsonParser().parse(jsonPage).getAsJsonObject().get("parse").
                getAsJsonObject().get("text").toString().replaceAll("\\<[^>]*>","")
                .replaceAll("\\[править \\| править код\\]", "")
                .replaceAll("\\n", "\r\n");
        return jsonPage;
    }

    public static int getPageID(JsonElement body) {
        JsonElement pageIdRaw = body.getAsJsonObject().get("search")
                .getAsJsonArray().get(0).getAsJsonObject().get("pageid");
        System.out.println("Page ID: " + pageIdRaw);
        return pageIdRaw.getAsInt();

    }

    public static boolean validateQuery(JsonElement body) {
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