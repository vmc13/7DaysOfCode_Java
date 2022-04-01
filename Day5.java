import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class SevenDaysOfCodeJavaDay5 {

	public static void main(String[] args) throws Exception {

		System.out.println("Chamando API");
		String apiKey = "<sua chave>";
		String json =  new ImdbApiClient(apiKey).getBody();
		
		System.out.println("Parsing do JSON");
		List<Movie> movies  = new ImdbMovieJsonParser(json).parse();
		
		System.out.println("Gerando HTML");
		PrintWriter writer = new PrintWriter("content.html");
		new HtmlGenerator(writer).generate(movies);
		writer.close();
	}
}

record Movie(String title, String urlImage, String rating, String year) {
}

class ImdbApiClient {

	private String apiKey;

	public ImdbApiClient(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBody() {

		try {
			URI apiIMDB = URI.create("https://imdb-api.com/en/API/Top250TVs/" + this.apiKey);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(apiIMDB).build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.body();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

}

class ImdbMovieJsonParser {

	private String json;

	public ImdbMovieJsonParser(String json) {
		this.json = json;
	}

	public List<Movie> parse() {
		String[] moviesArray = parseJsonMovies(json);

		List<String> titles = parseTitles(moviesArray);
		List<String> urlImages = parseUrlImages(moviesArray);
		List<String> ratings = parseRatings(moviesArray);
		List<String> years = parseYears(moviesArray);
		
		List<Movie> movies = new ArrayList<>();
		
		for (int i =0; i < titles.size(); i++) {
			movies.add(new Movie(titles.get(i), urlImages.get(i) , ratings.get(i), years.get(i)));
		}
		return movies;
	}

	private  String[] parseJsonMovies(String json) {
		Matcher matcher = Pattern.compile(".*\\[(.*)\\].*").matcher(json);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("no match in " + json);
		}

		String[] moviesArray = matcher.group(1).split("\\},\\{");
		moviesArray[0] = moviesArray[0].substring(1);
		int last = moviesArray.length - 1;
		String lastString = moviesArray[last];
		moviesArray[last] = lastString.substring(0, lastString.length() - 1);
		return moviesArray;
	}
	
	private  List<String> parseTitles(String[] moviesArray) {
		return parseAttribute(moviesArray, 3);
	}
	
	private  List<String> parseUrlImages(String[] moviesArray) {
		return parseAttribute(moviesArray, 5);
	}
	
	private  List<String> parseRatings(String[] moviesArray) {
		return parseAttribute(moviesArray, 7);
	}

	private  List<String> parseYears(String[] moviesArray) {
		return parseAttribute(moviesArray, 4);
	}
	
	
	private  List<String> parseAttribute(String[] jsonMovies, int pos) {
		return Stream.of(jsonMovies)
			.map(e -> e.split("\",\"")[pos]) 
			.map(e -> e.split(":\"")[1]) 
			.map(e -> e.replaceAll("\"", ""))
			.collect(Collectors.toList());
	}
}

class HtmlGenerator {

	private final PrintWriter writer;

	public HtmlGenerator(PrintWriter writer) {
		this.writer = writer;
	}

	public void generate(List<Movie> movies) {
		writer.println(
	"""
	<html>
		<head>
			<meta charset=\"utf-8\">
			<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">
			<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/css/bootstrap.min.css\" 
						+ "integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">
						
		</head>
		<body>
	""");

		for (Movie movie : movies) {
			String div =
			"""
			<div class=\"card text-white bg-dark mb-3\" style=\"max-width: 18rem;\">
				<h4 class=\"card-header\">%s</h4>
				<div class=\"card-body\">
					<img class=\"card-img\" src=\"%s\" alt=\"%s\">
					<p class=\"card-text mt-2\">Nota: %s - Ano: %s</p>
				</div>
			</div>
			""";
			
			writer.println(String.format(div, movie.title(), movie.urlImage(), movie.title(), movie.rating(), movie.year()));
		}

				
		writer.println(
		"""
			</body>
		</html>
		""");
	}

}