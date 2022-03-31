import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class SevenDaysOfCodeJavaDay1 {

	public static void main(String[] args) throws Exception {

		String apiKey = "<sua chave>";
		URI apiIMDB = URI.create("https://imdb-api.com/en/API/Top250TVs/" + apiKey);
		
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(apiIMDB).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String json = response.body();

		System.out.println("Resposta: " + json);
	}

}