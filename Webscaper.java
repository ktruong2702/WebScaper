import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadedWebCrawler {
    private static final int MAX_DEPTH = 2;
    private static final int MAX_THREADS = 5; // Adjust the number of threads as needed

    private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) {
        String startingUrl = "https://www.reddit.com";
        startCrawling(1, startingUrl, new HashSet<>());
    }

    private static void startCrawling(int level, String url, Set<String> visitedLinks) {
        if (level <= MAX_DEPTH && !visitedLinks.contains(url)) {
            executorService.execute(() -> {
                if (!url.startsWith("http")) {
                    return;
                }
                Document document = makeRequest(url, visitedLinks);

                if (document != null) {
                    String nextLink = "";
                    for (Element link : document.select("a[href]")) {
                        try {
                            nextLink = link.absUrl("href");
                        } catch (Exception e) {
                        }
                        if (!nextLink.isEmpty()) {
                            startCrawling(level + 1, nextLink, visitedLinks);
                        }
                    }
                }
            });
        }
    }

    private static Document makeRequest(String url, Set<String> visitedLinks) {
        try {
            Connection connection = Jsoup.connect(url)
                    .timeout(5000); // 5 seconds timeout

            connection.followRedirects(true); // Follow redirects

            Document document = connection.get();

            if (connection.response().statusCode() == 200 && !visitedLinks.contains(url)) {
                System.out.println("Link: " + url);
                System.out.println(document.title());

                visitedLinks.add(url);

                return document;
            }
            return null;
        } catch (IOException e) {
            // Handle exception, log it, or ignore it based on your needs
//            e.printStackTrace();
            return null;
        }
    }

    // Call shutdown() after crawling is complete
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executorService.shutdown()));
    }
}
