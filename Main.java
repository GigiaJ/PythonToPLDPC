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

    private static void beginTranslation() {
        output("\n\n\n");
        //Start the translation
        output("start");
        output("\t Declarations");
    }

    private static void endTranslation() {
        //End the translation
        output("stop");
    }

    private static void translateDeclarations(String code) {
        /**Generates the declarations from the python code**/
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
        output("\n");
    }

    private static String translateOperands(String code) {
        code = code.replaceAll(" and ", " AND ");
        code = code.replaceAll(" or ", " OR ");
        code = code.replaceAll(" != ", " <> ");
        return code;
    }

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


            String unmodifiedLine = line; //For regex/matcher use later down and cleaner code

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

    private static boolean translateOutput(String string) {
        if (string.startsWith("output(")) {
            /*Replace + with commas and remove non-quote spaces*/
            string = string.replaceAll("\\+", ",");

            //This pattern will match the outer most parenthesis
            Matcher betweenParanthesis = Pattern.compile("\\((.*)\\)").matcher(string);
            if (betweenParanthesis.find()) {
                //Remove type casting here
                output("output " + betweenParanthesis.group(1));
            }
            return true;
        }
        return false;
    }

    private static boolean translateInput(String string) {
        if (string.endsWith("input())") || string.endsWith("input()")) {
            output("input " + string.substring(0, string.indexOf("=")));
            return true;
        }
        return false;
    }

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
            output(Keyword.FOR + " " + loopControlVariable + " = " + startingValue + " to " + finalValue + " step " + stepValue);
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
     * Allows the storing of the tab value in an integer for a particular active keyword
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
