package ru.winstonobyman;

import com.sun.org.apache.xpath.internal.objects.XString;
import org.omg.CORBA.TIMEOUT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {

    final static int CONNECTION_TIMEOUT = 120;

    public static void main(String[] args) throws IOException {

        String urlString = "https://ru.wikipedia.org/w/api.php" +
                "?action=query&amp%3Blist=search&amp%3Butf8=&amp%3Bformat=json&amp%3Bsrsearch=Java&format=json";

        String result = getQueryResult(urlString);
        System.out.println(result);

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
}
