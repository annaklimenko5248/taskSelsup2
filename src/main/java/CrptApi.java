import java.util.concurrent.*;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Semaphore;

public class CrptApi {

    private final Semaphore semaphore;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);

        // Запускаем задачу, которая регулярно освобождает все разрешения
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delay = timeUnit.toMillis(1);
        scheduler.scheduleAtFixedRate(() -> semaphore.drainPermits(), delay, delay, TimeUnit.MILLISECONDS);
    }

    public String createDocument(Document doc, String signature) throws Exception {
        try {
            semaphore.acquire();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("URL_API_ЧЕСТНОГО_ЗНАКА"))  // Замените на актуальный URL API
                    .POST(HttpRequest.BodyPublishers.ofString(doc.toJSON()))
                    .header("Signature", signature)
                    .timeout(Duration.ofMinutes(1))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Здесь можно добавить обработку ответа, если это необходимо
            return response.body();

        } finally {
            semaphore.release();
        }
    }

    // Вложенный класс для представления документа
    private static class Document {
        private String data;

        public Document(String data) {
            this.data = data;
        }

        public String toJSON() {
            // Преобразуем данные документа в JSON
            // Замените на реальную логику сериализации, если необходимо
            return "{\"data\": \"" + data + "\"}";
        }
    }
}
