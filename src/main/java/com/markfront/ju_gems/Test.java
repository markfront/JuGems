package com.markfront.ju_gems;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileReader;
import java.io.StringReader;
import java.util.*;

public class Test {
    public static void main (String[] args) {
        String page_url = args[0]; // "https://www.wonderslist.com/10-most-amazing-places-on-earth"

        System.out.println(page_url);

        String text = downloadPage(page_url);

        System.out.println("text.length() = " + text.length() );

        System.out.println(text);

        System.out.println();

        List<CoreSentence> sentences = parseSentences(text);

        int valid_sentence_count = 0;
        for (CoreSentence sentence : sentences) {
            if (isValidSentence(sentence)) {
                System.out.println(sentence);
                valid_sentence_count++;
            }
        }
        System.out.println("valid_sentence_count = " + valid_sentence_count);
    }

    public static String downloadPage(String page_url) {
        String text = "";
        try {
            Document document = Jsoup.connect(page_url).get();

            text = document.body().text();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return text;
    }

    public static List<CoreSentence> parseSentences(String text) {

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = new CoreDocument(text);
        // annnotate the document
        pipeline.annotate(document);

        List<CoreLabel> tokens = document.tokens();
        System.out.println("tokens.size() = " + tokens.size());
        System.out.println();

        List<CoreSentence> sentences = document.sentences();
        System.out.println("sentences.size() = " + sentences.size());
        System.out.println();

        return sentences;
    }

    private static Set<String> subject_words = new HashSet<>(Arrays.asList(new String[] {
            "you", "i", "he", "she", "we", "they",
            "it", "this", "that", "these", "those",
            "my", "your", "his", "her", "their", "its",
            "mine", "hers", "yours", "ours", "theirs"
    }));

    private static Set<String> object_words = new HashSet<>(Arrays.asList(new String[] {
            "me", "you", "him", "her", "it", "them", "us"
    }));

    private static Set<String> clause_words = new HashSet<>(Arrays.asList(new String[] {
            "who", "what", "why", "how", "which", "where", "whose", "whom"
    }));

    private static int sentence_words_min = 5;
    private static  int sentence_words_max = 30;
    private static double capital_words_ratio = 0.2;

    public static boolean isValidSentence(CoreSentence sentence) {
        boolean result = false;
        List<CoreLabel> tokens = sentence.tokens();
        List<String> postags = sentence.posTags();
        if (tokens.size() >= sentence_words_min && tokens.size() <= sentence_words_max) {
            boolean has_subject = false;
            boolean has_object = false;
            boolean has_clause = false;
            for(CoreLabel token : tokens) {
                String word = token.toString().toLowerCase();
                if (subject_words.contains(word)) {
                    has_subject = true;
                    break;
                }
            }
            for(CoreLabel token : tokens) {
                String word = token.toString().toLowerCase();
                if (object_words.contains(word)) {
                    has_object = true;
                    break;
                }
            }
            for(CoreLabel token : tokens) {
                String word = token.toString().toLowerCase();
                if (clause_words.contains(word)) {
                    has_clause = true;
                    break;
                }
            }

            boolean has_noun = false;
            boolean has_verb = false;
            for(String postag : postags) {
                if (postag.startsWith("N")) {
                    has_noun = true;
                    break;
                }
            }
            for(String postag : postags) {
                if (postag.startsWith("V")) {
                    has_verb = true;
                    break;
                }
            }

            int uppercase_count = 0;
            int lowercase_count = 0;
            for(CoreLabel token : tokens) {
                String word = token.toString();
                if (Character.isUpperCase(word.charAt(0))) {
                    uppercase_count++;
                } else {
                    lowercase_count++;
                }
            }

            int totalcase_count = uppercase_count + lowercase_count;

            double capital_word_ratio = (uppercase_count * 1.0) / (totalcase_count!=0? totalcase_count : 0.1);

            result = ((has_subject || has_object || has_clause) || (has_noun && has_verb))
                    && capital_word_ratio <= capital_word_ratio;
        }
        return result;
    }
}
