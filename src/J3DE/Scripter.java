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

    private static String readLine(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
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
        try {
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
        } catch (IOException e) {
            return null;
        }
    }

    private String loadScript(String filename) {
        InputStream is = getClass().getResourceAsStream("/J3DE/Scripts/" + filename);
        if (is == null) {
            error("Script not found: " + filename);
            return null;
        }

        StringBuffer sb = new StringBuffer();
        try {
            String line;
            while ((line = readLine(is)) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return null;
        }
        return sb.toString();
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
                    int lineNum = 0;
                    while ((line = readLine(is)) != null) {
                        line = line.trim();
                        lineNum ++;
                        if (line.length() == 0 || line.startsWith("//")) {
                            continue;
                        }

                        if (line.startsWith("print ")) {
                            int firstQuote = line.indexOf('"');
                            int lastQuote = line.lastIndexOf('"');
                            if (firstQuote == -1 || lastQuote == -1 || firstQuote == lastQuote) {
                                error("Error parsing print command: " + line);
                                continue;
                            }
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
                    }
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
