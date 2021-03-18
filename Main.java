package com.company;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String code = readInput();
        beginTranslation();
        translateDeclarations(code);
        code = translateOperands(code);
        /**Program logic**/
        code = code.substring(code.indexOf("def start():") + 12, code.indexOf("def output(string):"));
        translateProgramLogic(code);
        endTranslation();
    }

    /**
     * Reads the python code to a point to prevent translating required methods from input and returns it as a string
     * @return the python code
     */
    private static String readInput() {
        String string = "";
        Scanner s = new Scanner(System.in);
        while (s.hasNextLine()){
            String line = s.nextLine();
            if ("    start()".equals(line)) {
                break;
            }
            string += "\n" + line;
        }
        s.close();
        return string;
    }

    /**
     * The code at the beginning of the pseudo code programs
     */
    private static void beginTranslation() {
        output("\n\n\n");
        //Start the translation
        output("start");
        output("\t Declarations");
    }

    /**
     * The code at the end of the pseudo code programs
     */
    private static void endTranslation() {
        //End the translation
        output("stop");
    }

    /**
     * Translates the declaration section of python code from the last S of declarations to the start of def start()
     * Does so by substringing the code from the above ranges and then splitting it by newlines then matching the
     * each string in the created string array to certain constraints depending on whether the line is a number
     * variable or a string variable
     * @param code              the entire python code received as input to the program
     */
    private static void translateDeclarations(String code) {
        String[] declarations = code.substring(code.indexOf("s"), code.indexOf("def")).split("\n");
        for (String string : declarations) {
            if (string.matches(".*(\\d)")) {
                //Is a number variable
                Matcher declMatch= Pattern.compile("(.*\\w).(= .*)").matcher(string);
                if (declMatch.find()) {
                    if (declMatch.group(1).equals("= 0")) {
                        output("\t\tnum " + string);
                    } else {
                        output("\t\tnum " + declMatch.group(0));
                    }
                }
            }
            else {
                //Is a string variable
                Matcher declMatch= Pattern.compile("(.*\\w).(= .*)").matcher(string);
                if (declMatch.find()) {
                    if (declMatch.group(1).equals("= \"\"")) {
                        output("\t\tstring " + string);
                    } else {
                        output("\t\tstring " + declMatch.group(0));
                    }
                }
            }
        }
    }

    /**
     * Converts all the python operands that aren't the same as the pseudo code's we are translating to
     * @param code              the entire python code received as input to the program
     * @return                  the modified python code
     */
    private static String translateOperands(String code) {
        code = code.replaceAll(" and ", " AND ");
        code = code.replaceAll(" or ", " OR ");
        code = code.replaceAll(" != ", " <> ");
        return code;
    }

    /**
     * Performs the large portion of translating program logic. An array is made to store keywords when they appear
     * so that they can be closed appropriately be closed in an activeKeyword object which holds the respective
     * keyword and tab. The tab is found by simply matching the tabulation character, printing it, and the incrementing
     * the tab variable. Similarly closing the keywords compares this tab to the list of keywords' tabs and if any are
     * smaller than the variable the keywords are closed and the tabs for the next line are made.
     * @param code              the entire python code received as input to the program
     */
    private static void translateProgramLogic(String code) {
        String[] lines = code.split("\n");
        ArrayList<ActiveKeyword> activeKeywords = new ArrayList<>();
        for (String line : lines) {
            boolean handleAnything = false; //Used to determine whether any handling has occurred on this line
            int tabs = 0;
            Matcher lineTabulation = Pattern.compile("(\\t)").matcher(line);
            while (lineTabulation.find()) {
                System.out.print("\t");
                tabs++;
            }
            for (int i = 0; i < activeKeywords.size(); i++) {
                if (activeKeywords.get(i).tabValue >= tabs) {
                    System.out.println(activeKeywords.get(i).getClosingKeyword());
                    activeKeywords.remove(i);
                    for (int x = 0; x < tabs; x++) {
                        System.out.print("\t");
                    }
                    break;
                }
            }

            line = line.replaceAll("\\t+", "");;

            handleAnything = translateOutput(line);
            handleAnything = (handleAnything) || translateInput(line);
            handleAnything = (handleAnything) || translateFor(line, activeKeywords, tabs);
            handleAnything = (handleAnything) || translateWhile(line, activeKeywords, tabs);
            handleAnything = (handleAnything) || translateIf(line, activeKeywords, tabs);
            handleAnything = (handleAnything) || translateElse(line, activeKeywords, tabs);
            //If no keyword or method call
            if (!handleAnything) {
                output(line);
            }

        }
    }

    /**
     * Takes a string which is a line of code and then checks to see if it starts with the python output method and
     * if so then uses pattern matching to find the contents of the output method from the string and then performs
     * a general replacement of plus signs to commas as the pseudo code at hand uses them instead to join strings
     * and then for type casted parts of the output in the code finds matches and replaces them with the inner
     * contents of parenthesis as well as if they contain a plus sign it replaces that with place holder value
     * until all plus signs that denote the joining of strings are handled and then it replaces the placeholder
     * strings in the output (which should normally never ever match another string) with plus signs. Finally
     * returning a true if all of such has occurred and a false if the string did not start with the corresponding
     * if check
     * @param string            the line of code passed to this method
     * @return                  a boolean that is true if the string passes the corresponding if statement
     */
    private static boolean translateOutput(String string) {
        if (string.startsWith("output(")) {
            /*Replace + with commas and remove non-quote spaces*/
            //string = string.replaceAll("\\+", ",");

            //This pattern will match the outer most parenthesis
            Matcher betweenParanthesis = Pattern.compile("\\((.*)\\)").matcher(string);
            if (betweenParanthesis.find()) {
                String outputLine = betweenParanthesis.group(1);
                final String TYPECAST_PATTERN = "((\\w*)(\\((.*)\\)))";
                final String PLACEHOLDER_STRING = "ewqe21ewer158ABE}"; //A random string to prevent incorrect replacement
                Matcher typeMatch = Pattern.compile(TYPECAST_PATTERN).matcher(outputLine);
                while (typeMatch.find()) {
                    String match = typeMatch.group(1);
                    String replacementMatch = typeMatch.group(4);
                    if (replacementMatch.contains("+")) {
                        replacementMatch = replacementMatch.replaceAll("\\+", PLACEHOLDER_STRING);
                    }
                    outputLine = outputLine.replace(match, replacementMatch);
                    typeMatch = Pattern.compile(TYPECAST_PATTERN).matcher(outputLine);
                }
                outputLine = outputLine.replaceAll("\\+", ",");
                outputLine = outputLine.replaceAll(PLACEHOLDER_STRING, "+");
                output("output " + outputLine);
            }
            return true;
        }
        return false;
    }

    /**
     * Takes a string which is a line of code and then checks to see if it ends with the python input method
     * and if so outputs the translated line and returns true
     * @param string            the line of code passed to this method
     * @return                  a boolean that is true if the string passes the corresponding if statement
     */
    private static boolean translateInput(String string) {
        if (string.endsWith("input())") || string.endsWith("input()")) {
            output("input " + string.substring(0, string.indexOf("=")));
            return true;
        }
        return false;
    }

    /**
     * Keyword translations.
     * Take a string which is a line of code, the arraylist containing the active keywords (meaning they are accounting
     * for them to keep track of tab changes to apply the end keyword), and the number of tabs the keyword is at
     * to use for creating a new active keyword object containing the type and number of tabs to store in the
     * active keywords array list. These methods return whether or not they have taken effect by checking if the
     * string passed starts with the respective keyword.
     *
     * This comment applies to translateFor, translateWhile, translateIf, and translateElse
     *
     * @param string            the line of code passed to this method
     * @param activeKeywords    the array list containing the active keywords
     * @param tabs              the number of tabs this keyword is offset by
     * @return                  a boolean denoting whether or not the keyword has taken effect
     */

    private static boolean translateFor(String string, ArrayList<ActiveKeyword> activeKeywords, int tabs) {
        if (string.startsWith("for")) {
            activeKeywords.add(new ActiveKeyword(Keyword.FOR, tabs));
            Matcher matcher = Pattern.compile("\\((.*)\\)").matcher(string);
            String rangeParameters = "";
            while(matcher.find()) {
                rangeParameters = matcher.group(1);
            }
            String loopControlVariable = string.substring(4, string.indexOf(" in "));
            String stepValue = (rangeParameters.contains(",")) ? rangeParameters.substring(rangeParameters.lastIndexOf(",")) : "1";
            String startingValue = (rangeParameters.contains(",")) ? rangeParameters.substring(0, rangeParameters.indexOf(",")) : "0";
            String finalValue = (rangeParameters.contains(",")) ? rangeParameters.substring(rangeParameters.indexOf(","), rangeParameters.lastIndexOf(",")) : rangeParameters;
            output(Keyword.FOR.getName() + " " + loopControlVariable + " = " + startingValue + " to " + finalValue + " step " + stepValue);
            return true;
        }
        return false;
    }

    private static boolean translateWhile(String string, ArrayList<ActiveKeyword> activeKeywords, int tabs) {
        if (string.startsWith("while")) {
            activeKeywords.add(new ActiveKeyword(Keyword.WHILE, tabs));
            output(string.replaceAll("==", "="));
            return true;
        }
        return false;
    }

    private static boolean translateIf(String string, ArrayList<ActiveKeyword> activeKeywords, int tabs) {
        if (string.startsWith("if")) {
            activeKeywords.add(new ActiveKeyword(Keyword.IF, tabs));
            output(string.replaceAll("==", "="));
            return true;
        }
        return false;
    }

    private static boolean translateElse(String string, ArrayList<ActiveKeyword> activeKeywords, int tabs) {
        if (string.startsWith("else")) {
            activeKeywords.add(new ActiveKeyword(Keyword.IF, tabs)); //Else in the pseudo code is ended with endif not endelse
            output(string.replaceAll("==", "="));
            return true;
        }
        return false;
    }

    /**
     * A quicker method to use System.out.println(string) assuming most or all output is a string
     * @param string            the string to output
     */
    private static void output(String string) {
        System.out.println(string);
    }

    /**
     * Possible keywords that require action
     */
    enum Keyword {
        FOR("for"), WHILE("while"),  IF("if"), ELSE("else"), END("end");

        private String name;

        Keyword(String keyword) {
            this.name = keyword;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * A class which allows the storing of the tab value in an integer for a particular active keyword
     */
    static class ActiveKeyword {
        Keyword keyword;
        int tabValue;

        ActiveKeyword(Keyword keyword, int tabValue) {
            this.keyword = keyword;
            this.tabValue = tabValue;
        }

        public int getTabValue() {
            return tabValue;
        }

        public String getOpeningKeyword() {
            return keyword.getName();
        }

        public String getClosingKeyword() {
            return Keyword.END.getName() + keyword.getName();
        }
    }
}
