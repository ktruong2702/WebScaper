import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadedWebCrawler {
    private static final int MAX_DEPTH = 2;
    private static final int MAX_THREADS = 5; // Adjust the number of threads as needed

    private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
    private static int linkCount = 0; // Counter for the number of links processed

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the starting URL: ");
        String startingUrl = scanner.nextLine();
        startCrawling(1, startingUrl, new HashSet<>());
        scanner.close();
    }

    private static void startCrawling(int level, String url, Set<String> visitedLinks) {
        if (level <= MAX_DEPTH && !visitedLinks.contains(url)) {
            executorService.execute(() -> {
                if (!url.startsWith("http")) {
                    return;
                }
                Document document = makeRequest(url, visitedLinks);

                if (document != null) {
                    for (Element link : document.select("a[href]")) {
                        try {
                            String nextLink = link.absUrl("href");
                            if (!nextLink.isEmpty()) {
                                linkCount++; // Increment link count
                                System.out.println("Link " + linkCount + ": " + nextLink); // Print link with count
                                System.out.println("Title: " + document.title()); // Print page title
                                startCrawling(level + 1, nextLink, visitedLinks);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
                System.out.println("Visited Link: " + url);
                System.out.println("Title: " + document.title()); // Print page title

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
