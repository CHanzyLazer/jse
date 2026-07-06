package jse.code.io;

import jse.code.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;

public interface IPrompter {
    boolean confirm(boolean aDefault, String aMsg) throws IOException;
    
    IPrompter CONSOLE = new IPrompter() {
        final BufferedReader mReader;
        {
            try {
                mReader = IO.toReader(System.in, Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override public boolean confirm(boolean aDefault, String aMsg) throws IOException {
            if (aDefault) {
                System.out.print(IO.Text.yellow(aMsg+" (Y/n): "));
                String tLine = mReader.readLine();
                while (true) {
                    if (tLine == null) {
                        System.out.println();
                        System.out.println(IO.Text.yellow("Auto selected Yes."));
                        return true;
                    }
                    tLine = tLine.trim();
                    if (tLine.isEmpty() || tLine.equalsIgnoreCase("y")) {
                        return true;
                    }
                    if (tLine.equalsIgnoreCase("n")) {
                        return false;
                    }
                    System.out.print(IO.Text.yellow(aMsg+" (Y/n): "));
                    tLine = mReader.readLine();
                }
            } else {
                System.out.print(IO.Text.yellow(aMsg+" (y/N): "));
                String tLine = mReader.readLine();
                while (true) {
                    if (tLine == null) {
                        System.out.println();
                        System.out.println(IO.Text.yellow("Auto selected No."));
                        return false;
                    }
                    tLine = tLine.trim();
                    if (tLine.isEmpty() || tLine.equalsIgnoreCase("n")) {
                        return false;
                    }
                    if (tLine.equalsIgnoreCase("y")) {
                        return true;
                    }
                    System.out.print(IO.Text.yellow(aMsg+" (y/N): "));
                    tLine = mReader.readLine();
                }
            }
        }
    };
    IPrompter YES = (aDefault, aMsg) -> {
        System.out.println(IO.Text.yellow("Auto yes for '"+aMsg+"'"));
        return true;
    };
}
