package ru.winstonobyman;



import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;


/**Можно было делать проект на maven или gradle, но это как стрелять из пушки по воробьям.
 * Поэтому просто добавляю джарник с Gson в Project Structure в IntelliJ IDEA
 * jar отсюда: https://search.maven.org/artifact/com.google.code.gson/gson/2.8.6/jar
 */
public class Main {

    final static int CONNECTION_TIMEOUT = 120; // параметр таймаута

    public static void main(String[] args) throws IOException {
        // Получаем запрос от пользователя
        String question = getUserRequest();
        // На основе запроса польщователя делаем запрос в виде url
        String urlString = generateQuery(question);
        // получаем результат запроса в формате json
        String jsonQueryResult = getQueryResult(urlString);
        // берем необходимую часть запроса с помощью класса Parser из Gson
        JsonElement body = new JsonParser().parse(jsonQueryResult).getAsJsonObject().get("query");
        // проводим валидацию - является ли поиск успешным (найдена ли хоть одна статья)
        if (validateQuery(body)) {
            // если статья есть, выводим отрывок статьи и предлагаем
            // получить полную в папку проекта в файл pages.txt
            System.out.println("Фрагмент статьи: ");
            System.out.println(getSnippet(body));
            System.out.println("Вы желаете получить полную статью по данному фрагменту (да/нет)?" +
                    " Будет проведена запись в файл page.txt в корне проекта.");
            if (new Scanner(System.in).nextLine().replaceAll("\\s", "").equals("да")) {
                // если пользователь хочет статью, полуаем страницу по pageID как рещультат дугого запроса
                int pageId = getPageID(body);
                String page = getPageById(pageId);
                // записываем в файл
                FileWriter fw = new FileWriter("page.txt", false);
                fw.write(page);
                fw.flush();
            }
        }
    }


    public static String getPageById(int pageId) throws IOException {
        // строка запроса.
        // StringBuilder для построения запроса  (удобнее добавлять и можно легче изменить)
        StringBuilder urlString = new StringBuilder("https://ru.wikipedia.org/w/api.php?");
        urlString.append("action=parse") // делаем запрос на получение данных
                .append("&format=json") // выдаем данные как json
                .append("&utf8=")        //  ставим кодировку
                .append("&prop=text")   // выводим только один элемент - текст
                .append("&formatversion=2")
                .append("&pageid=" + pageId) // выбираем id страницы для парсинга
                ;
        // вызываем созданный ранее метод
        String jsonPage = getQueryResult(urlString.toString());
        // Получаем и редактируем текст статьи для записи в файл
        jsonPage = new JsonParser().parse(jsonPage).getAsJsonObject().get("parse").
                getAsJsonObject().get("text").toString().replaceAll("<[^>]*>","")
                .replaceAll("\\[править \\| править код]", "")
                .replaceAll("\n", " ")
                .replaceAll("&#\\w*;", " ")
                ;
        // возвращаем тело статьи
        return jsonPage;
    }

    // метод для получения отрывка статьи из тела json ответа от сервера
    public static String getSnippet(JsonElement body) {
        JsonElement pageSnippetRaw = body.getAsJsonObject().get("search")
                .getAsJsonArray().get(0).getAsJsonObject().get("snippet");
        return pageSnippetRaw.toString().replaceAll("<[^>]*>","");
    }

    // метод для получения id статьи из тела json ответа от сервера
    public static int getPageID(JsonElement body) {
        JsonElement pageIdRaw = body.getAsJsonObject().get("search")
                .getAsJsonArray().get(0).getAsJsonObject().get("pageid");
        return pageIdRaw.getAsInt();

    }

    // метод проверки того, нашёлся ли хоть один резуьтат по запросу
    public static boolean validateQuery(JsonElement body) {
        JsonElement totalHits = body.getAsJsonObject().get("searchinfo")
                                    .getAsJsonObject().get("totalhits");
        if (totalHits.getAsInt() == 0) {
            System.out.println("Не найдено ни одной статьи.");
            return false;
        } else {
            System.out.printf("Найдено %d статей. Вывод первой.\n", totalHits.getAsInt());
            return true;
        }

    }

    // метод получения резултата запроса по ulr-строке, которую получаем от метода ниже
    public static String getQueryResult(String urlString) throws IOException {
        // создаем класс url
        URL url = new URL(urlString);
        // приводим url-класс к нужному типу и открываем соединение
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // настраиваем параметры запроса
        connection.setRequestMethod("GET"); // метод запроса
        connection.setRequestProperty("Content-Type", "application/json"); // свойства запроса
        connection.setConnectTimeout(CONNECTION_TIMEOUT); // тамаут
        // превращаем инпутстрим в строку и возвращаем
        Scanner sc = new Scanner(new InputStreamReader(connection.getInputStream())).useDelimiter("\\A");
        String jsonString = sc.hasNext() ? sc.next() : "";
        return jsonString;
        }

    // получаем запрос поиска от пользователя
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

    // метод генерации запроса на поиск статьи
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