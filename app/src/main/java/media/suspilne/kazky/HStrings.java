package media.suspilne.kazky;

public class HStrings {
    public static String startFromCapital(String line, boolean eachWord){
        String result = "";

        boolean first = true;
        String[] words = line.split(" ");
        for (String word: words) {
            result += (eachWord || first ? startFromCapital(word) : word.toLowerCase()) + " ";
            first = false;
        }

        return result.trim();
    }

    public static String startFromCapital(String word){
        String first = word.substring(0, 1).toUpperCase();
        String rest = word.substring(1).toLowerCase();

        return first + rest;
    }
}
