package tools;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by ckboss on 16-4-15.
 */
public class StopWords {

    private static final String stopwordslist = "data/stopwords-en.txt";

    public StopWords() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(StopWords.stopwordslist));

            Jedis jedis = JEdisSetting.jedisPool.getResource();

            while(br.ready()) {
                String word = br.readLine();
                jedis.sadd("stopword", word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        StopWords stop = new StopWords();
        String word = "your";
        Jedis jedis = JEdisSetting.jedisPool.getResource();
        System.out.println(jedis.sismember("stopword",word));
    }
}
