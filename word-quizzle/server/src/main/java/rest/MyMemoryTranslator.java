package rest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MyMemoryTranslator {

	public static final String DICT_PATH = ".\\src\\main\\resources\\dictionary.txt";
	public static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get?q=X&langpair=it|en";

	private static volatile MyMemoryTranslator instance = null;
	private static HashMap<String, ArrayList<String>> cache;

	//costruttore
	private MyMemoryTranslator() throws IOException {	
		if (instance != null) {
			throw new RuntimeException(
					"Use getInstance() method to get the single instance of this class.");
		}

		cache = new HashMap<String, ArrayList<String>>();

	}


	public static HashMap<String, ArrayList<String>> getRandomTranslations(int numWords) throws IOException {
		if(instance==null) { //Check for the first time 
			synchronized(MyMemoryTranslator.class) {
				if( instance == null) {//Check for the second time.
					instance = new MyMemoryTranslator();
				}
			}
		}

		HashMap<String, ArrayList<String>> words = new HashMap<String, ArrayList<String>>(numWords);

		List<String> list = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(DICT_PATH));
		list = br.lines().collect(Collectors.toList());   

		while(numWords-- > 0) {
			int random = new Random().nextInt(list.size());
			String word = list.get(random);
			words.put(word, getTranslationsOf(word));
		}

		br.close();
		return words;
	}


	public static ArrayList<String> getTranslationsOf(String word) throws IOException{
		if(instance==null) { //Check for the first time 
			synchronized(MyMemoryTranslator.class) {
				if( instance == null) {//Check for the second time.
					instance = new MyMemoryTranslator();
				}
			}
		}

		word = word.toLowerCase();

		if(cache.containsKey(word)) return cache.get(word);

		String myMemoryUrl = MYMEMORY_URL.replaceFirst("X", word);

		URL url = new URL(myMemoryUrl);

		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		JsonElement jsonParser = JsonParser.parseReader(in);

		JsonObject jsonObject = jsonParser.getAsJsonObject();		

		JsonArray matches = jsonObject.getAsJsonArray("matches");

		ArrayList<String> translations = new ArrayList<String>();

		Iterator<JsonElement> it = matches.iterator();

		while(it.hasNext()) {
			JsonObject matchOj = (JsonObject) it.next();
			String translation = matchOj.get("translation").getAsString();
			translation = translation.toLowerCase();
			translations.add(translation);
		}

		cache.put(word, translations);

		return translations;

	}

	public static void eraseCache() throws IOException {
		if(instance==null) { //Check for the first time 
			synchronized(MyMemoryTranslator.class) {
				if( instance == null) {//Check for the second time.
					instance = new MyMemoryTranslator();
				}
			}
		}
		cache.clear();
	}


}
