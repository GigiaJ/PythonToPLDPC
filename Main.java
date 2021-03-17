package com.company;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String code = "";
        while (s.hasNextLine() == true){
            String line = s.nextLine();
            if ("    start()".equals(line)) {
                break;
            }
            code += "\n" + line;
        }

        output("\n\n\n");

        //Start the translation
        output("start");
        output("\t Declarations");

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

        /**Program logic**/
        code = code.substring(code.indexOf("def start():") + 12, code.indexOf("def output(string):"));

        String[] lines = code.split("\n");

        for (String line : lines) {
            Matcher lineTabulation = Pattern.compile("(\\t)").matcher(line);
            while (lineTabulation.find()) {
                System.out.print("\t");
            }

            String unmodifiedLine = line; //For regex/matcher use later down and cleaner code

            line = line.replaceAll("\\s+", "");;
            if (line.startsWith("output(")) {
                /*Replace + with commas and remove non-quote spaces*/
                line = line.replaceAll("\\+", ",");
                Matcher betweenParanthesis = Pattern.compile("\\(([^()]*)\\)").matcher(unmodifiedLine);
                if (betweenParanthesis.find()) {
                    output("output " + betweenParanthesis.group(1));
                }
            }
            else if (line.endsWith("input())") || line.endsWith("input()")) {
                output("input " + line.substring(0, line.indexOf("=")));
            }

            else if (line.startsWith("for")) {
                output(line + " *************TODO*******");
                //Check the tab gap and compare each line until no longer in the loop
                //Probably create a boolean or something to indicate within a loop
            }

            else if (line.startsWith("while")) {
                output(line + " *************TODO*******"); //placeholder
                //Check the tab gap and compare each line until no longer in the loop
                //Probably create a boolean or something to indicate within a loop
            }

            else if (line.startsWith("if")) {
                output(line + " *************TODO*******"); //placeholder
            }

            else if (line.startsWith("else if")) {
                output(line + " *************TODO*******"); //placeholder
            }

            else if (line.startsWith("else")) {
                output(line + " *************TODO*******"); //placeholder
            }

            else {
                /**Add regular program logic handling**/
                output(line);
            }
        }
        
        //End the translation
        output("stop");
    }

    private static void output(String string) {
        System.out.println(string);
    }
}
