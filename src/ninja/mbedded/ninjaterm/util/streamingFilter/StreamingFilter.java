package ninja.mbedded.ninjaterm.util.streamingFilter;

import ninja.mbedded.ninjaterm.util.debugging.Debugging;
import ninja.mbedded.ninjaterm.util.streamedText.StreamedText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Class contains a static method for shifting a provided number of characters from one input
 * <code>{@link StreamedText}</code> object to another output <code>{@link StreamedText}</code>
 * object.</p>
 *
 * @author Geoffrey Hunter <gbmhunter@gmail.com> (www.mbedded.ninja)
 * @since 2016-09-28
 * @last-modified 2016-10-03
 */
public class StreamingFilter {

    private boolean releaseTextOnCurrLine = false;

    private String filterPattern = "";

    Pattern regexPattern;

    public void setFilterPatten(String filterPattern) {

        this.filterPattern = filterPattern;
        regexPattern = Pattern.compile(filterPattern);

        // Reset filter engine
        releaseTextOnCurrLine = false;

    }

    /**
     * This method provides a filtering function based on an incoming stream of data.
     *
     */
    public void parse(
            StreamedText inputStreamedText,
            StreamedText outputStreamedText) {

        System.out.println(getClass().getSimpleName() + ".parse() called with:");
        System.out.println("inputStreamedText { " + Debugging.convertNonPrintable(inputStreamedText.toString()) + "}.");
        System.out.println("outputStreamedText { " + Debugging.convertNonPrintable(outputStreamedText.toString()) + "}.");

        if(filterPattern.equals("")) {
            System.out.println("Filter text empty. Not performing any filtering.");

            // Shift all input to output
            outputStreamedText.shiftCharsIn(inputStreamedText, inputStreamedText.getText().length());
            return;
        }

        if(inputStreamedText.getText().equals("")) {
            System.out.println("No filtering to perform. Returning...");
            return;
        }

        // Search for new line characters
        String lines[] = inputStreamedText.getText().split("(?<=[\\n])");

        for (String line : lines) {

            // Check to see if we can release all text on this line without even bothering
            // to check for a match. This will occur if a match has already occurred on this line.
            if(releaseTextOnCurrLine) {
                System.out.println("releaseTextOnCurrLine = true. Releasing text " + Debugging.convertNonPrintable(line));
                outputStreamedText.shiftCharsIn(inputStreamedText, line.length());

                if(hasNewLineChar(line)) {
                    releaseTextOnCurrLine = false;
                }

                // Jump to next iteration of for loop
                System.out.println("Going to next iteration of loop.");
                continue;
            }


            Matcher matcher = regexPattern.matcher(line);

            if (matcher.find()) {
                // Match in line found!
                System.out.println("Match in line found. Line = " + Debugging.convertNonPrintable(line));

                // We can release all text/nodes up to the end of this line
                int numCharsToRelease = line.length();
                System.out.println("numCharsToRelease = " + numCharsToRelease);
                outputStreamedText.shiftCharsIn(inputStreamedText, numCharsToRelease);

                // Check to see if this is the last line. If so, set the releaseTextOnCurrLine to true
                // so that next time this function is called, any other text which is also on this line
                // will be released without question

                if(line == lines[lines.length - 1] && !hasNewLineChar(line)) {
                    releaseTextOnCurrLine = true;
                }
            } else {
                // No match found on this line. If this line is completed, then we know there can never be a match,
                // and it can be deleted from the heldStreamedText
                System.out.println("No match found on line = " + Debugging.convertNonPrintable(line));

                if(hasNewLineChar(line)) {
                    System.out.println("Deleting line.");
                    inputStreamedText.removeChars(line.length());
                }

            }
        } // for (String line : lines)

        System.out.println(getClass().getSimpleName() + ".parse() finished. Variables are now:");
        System.out.println("inputStreamedText { " + Debugging.convertNonPrintable(inputStreamedText.toString()) + "}.");
        System.out.println("outputStreamedText { " + Debugging.convertNonPrintable(outputStreamedText.toString()) + "}.");

    }

    public static boolean hasNewLineChar(String line) {
        Pattern pattern = Pattern.compile("\n");
        Matcher matcher = pattern.matcher(line);

        if(matcher.find())
            return true;
        else
            return false;
    }

}
