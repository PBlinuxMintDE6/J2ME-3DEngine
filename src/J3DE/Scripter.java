/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package J3DE;

import java.io.*;
import java.util.Vector;

/**
 *
 * @author c
 */
public class Scripter {

    private Vector files;
    public int scriptErrors = 0;

    public Vector globalVars = new Vector();

    private static String readLine(InputStream in) {
        StringBuffer sb = new StringBuffer();
        try {
            int c;

            while ((c = in.read()) != -1) {
                if (c == '\n') {
                    break;
                }
                if (c != '\r') { // ignore CR
                    sb.append((char) c);
                }
            }

            if (c == -1 && sb.length() == 0) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return sb.toString();
    }

    private void error(String text, boolean fatal) {
        if ((fatal || Configuration.FORCE_ALL_SCRIPT_ERRORS_FATAL) && Configuration.CRITICAL_SCRIPT_ERRORS) {
            throw new ScriptException(text);
        } else {
            System.out.println(text);
            scriptErrors++;
        }
    }

    private void error(String text) {
        if (Configuration.FORCE_ALL_SCRIPT_ERRORS_FATAL && Configuration.CRITICAL_SCRIPT_ERRORS) {
            throw new ScriptException(text);
        } else {
            System.out.println(text);
            scriptErrors++;
        }
    }

    private Vector splitManifest(InputStream data) {
        if (data == null) {
            return null;
        }
        Vector lines = new Vector();
        while (true) {
            String line = readLine(data);
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0 || line.startsWith("//")) {
                continue;
            }
            int hash = line.indexOf("#");
            if (hash == -1) {
                error("Invalid manifest line: " + line);
            }

            String script = line.substring(0, hash);
            String target = line.substring(hash + 1);
            lines.addElement(new String[]{script, target});
        }
        return lines;
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isLetterOrDigit(char c) {
        return isLetter(c) || isDigit(c);
    }

    private Vector tokenize(String line) {
        Vector tokens = new Vector();
        int i = 0;

        while (i < line.length()) {
            char c = line.charAt(i);

            // Skip whitespace
            if (c == ' ' || c == '\t') {
                i++;
                continue;
            }

            // String literal
            if (c == '"') {
                i++;
                StringBuffer sb = new StringBuffer();
                while (i < line.length() && line.charAt(i) != '"') {
                    sb.append(line.charAt(i++));
                }
                if (i >= line.length()) {
                    error("Unterminated string literal", true);
                }
                i++; // closing quote
                tokens.addElement(new Token(Token.STRING, sb.toString()));
                continue;
            }

            // Number
            if ((c >= '0' && c <= '9') || c == '-') {
                StringBuffer sb = new StringBuffer();
                while (i < line.length()) {
                    char n = line.charAt(i);
                    if ((n >= '0' && n <= '9') || n == '.' || n == '-') {
                        sb.append(n);
                        i++;
                    } else {
                        break;
                    }
                }
                tokens.addElement(new Token(Token.NUMBER, sb.toString()));
                continue;
            }

            // Identifier
            if (isLetter(c) || c == '_') {
                StringBuffer sb = new StringBuffer();
                sb.append(c); // first character
                i++;

                while (i < line.length()) {
                    char n = line.charAt(i);
                    if (isLetterOrDigit(n) || n == '_' || n == '.') {
                        sb.append(n);
                        i++;
                    } else {
                        break;
                    }
                }

                tokens.addElement(new Token(Token.IDENT, sb.toString()));
                continue;
            }

            // Symbols (= + etc)
            tokens.addElement(new Token(Token.SYMBOL, String.valueOf(c)));
            i++;
        }

        return tokens;
    }

    private String getVar(String name, Vector localVars) {
        for (int i = 0; i < localVars.size(); i++) {
            ScriptVariable v = (ScriptVariable) localVars.elementAt(i);
            if (v.name.equals(name)) {
                return v.value;
            }
        }

        for (int i = 0; i < globalVars.size(); i++) {
            ScriptVariable v = (ScriptVariable) globalVars.elementAt(i);
            if (v.name.equals(name)) {
                return v.value;
            }
        }

        error("Undefined variable: " + name, true);
        return null;
    }

    private void setVar(String name, String value, boolean isGlobal, Vector localVars) {
        Vector table = isGlobal ? globalVars : localVars;
        for (int i = 0; i < table.size(); i++) {
            ScriptVariable v = (ScriptVariable) table.elementAt(i);
            if (v.name.equals(name)) {
                v.value = value;
                return;
            }
        }
        table.addElement(new ScriptVariable(name, value));
    }

    private void ePrint(Vector tokens, Vector localVars) {
        if (tokens.size() != 2) {
            error("print expects 1 argument got " + Integer.toString(tokens.size() - 1), true);
        }

        Token t = (Token) tokens.elementAt(1);

        if (t.type == Token.STRING) {
            System.out.println(t.text);
        } else if (t.type == Token.IDENT) {
            System.out.println(getVar(t.text, localVars));
        } else {
            error("print expects a string or variable", true);
        }
    }

    private void eVar(Vector tokens, Vector localVars) {
        if (tokens.size() < 4) {
            error("Malformed var declaration", true);
            return;
        } else {
        }

        Token nameT = (Token) tokens.elementAt(1);
        Token eqT = (Token) tokens.elementAt(2);
        Token valueT = (Token) tokens.elementAt(3);

        if (!"=".equals(eqT.text)) {
            error("Invalid variable declaration: No =", true);
            return;
        }

        boolean isGlobal = false;
        String varName = nameT.text;

        if (varName.startsWith("G.")) {
            isGlobal = true;
            varName = varName.substring(2);
        }

        setVar(varName, valueT.text, isGlobal, localVars);
    }

    private void eCrash(Vector tokens, Vector localVars, int lineNum, String scriptName) {
        Token message = null;
        Token fatal = null;
        if (tokens.size() > 1) {
            message = (Token) tokens.elementAt(1);
            if (tokens.size() > 2) {
                fatal = (Token) tokens.elementAt(2);
            }
        }
        boolean fatalCrash = (fatal != null);
        String crashMessage = "Unknwn error";
        if (message != null) {
            if (message.type == Token.STRING) {
                crashMessage = message.text;
            } else if (message.type == Token.IDENT) {
                crashMessage = getVar(message.text, localVars);
            }
        }
        error(scriptName + ":" + lineNum + " - " + crashMessage, fatalCrash);
    }

    public Scripter() {
        InputStream is = getClass().getResourceAsStream(
                "/J3DE/Scripts/manifest.J3M"
        );
        Vector parsed = splitManifest(is);
        if (parsed != null) {
            files = parsed;
        }
    }

    public void Execute(Camera camera) {
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.size(); i++) {
            String[] entry = (String[]) files.elementAt(i);
            String scriptFile = entry[0];

            InputStream is = getClass().getResourceAsStream("/J3DE/Scripts/" + scriptFile);
            if (is != null) {
                try {
                    String line;
                    Vector localVars = new Vector();
                    setVar("scriptName", scriptFile, false, localVars);
                    int lineNum = 0;
                    while ((line = readLine(is)) != null) {
                        line = line.trim();
                        lineNum++;
                        Vector tokens = tokenize(line);
                        if (tokens.isEmpty() || line.startsWith("//")) {
                            continue;
                        }

                        Token cmd = (Token) tokens.elementAt(0);

                        if (cmd.type != Token.IDENT) {
                            error("Expected command", true);
                        }

                        if ("print".equals(cmd.text)) {
                            ePrint(tokens, localVars);
                        } else if ("var".equals(cmd.text)) {
                            eVar(tokens, localVars);
                        } else if ("crash".equals(cmd.text)) {
                            eCrash(tokens, localVars, lineNum, scriptFile);
                        } else {
                            error(scriptFile + ":" + lineNum + " - Uknwn cmd: " + line);
                        }

                        /*
                         String text = line.substring(firstQuote + 1, lastQuote);
                         System.out.println(text);
                         } else if (line.startsWith("teleport ")) {
                         // Remove "teleport " and split manually by spaces
                         String coords = line.substring(9).trim();
                         int firstSpace = coords.indexOf(' ');
                         int secondSpace = coords.indexOf(' ', firstSpace + 1);

                         if (firstSpace == -1 || secondSpace == -1) {
                         error("Error parsing teleport command: " + line);
                         continue;
                         }

                         try {
                         float x = Float.parseFloat(coords.substring(0, firstSpace));
                         float y = Float.parseFloat(coords.substring(firstSpace + 1, secondSpace));
                         float z = Float.parseFloat(coords.substring(secondSpace + 1));
                         camera.setPosition(x, y, z);
                         } catch (NumberFormatException e) {
                         error("Malformed numbers in teleport: " + line);
                         scriptErrors++;
                         }
                         } else {
                         error(scriptFile + ":" + Integer.toString(lineNum) + " - Unkn cmd: " + line);
                         }
                         */
                    }
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
