package ninja.mbedded.ninjaterm.util.rxProcessing.streamedText;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ninja.mbedded.ninjaterm.JavaFXThreadingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the <code>shiftToTextNodes()</code> method of the <code>{@link StreamedData}</code> class.
 *
 * @author          Geoffrey Hunter <gbmhunter@gmail.com> (www.mbedded.ninja)
 * @since           2016-10-02
 * @last-modified   2016-10-16
 */
public class ShiftToTextNodeTests {

    /**
     * Including this variable in class allows JavaFX objects to be created in tests.
     */
    @Rule
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

    private StreamedData streamedData;
    private ObservableList<Node> textNodes;

    @Before
    public void setUp() throws Exception {
        streamedData = new StreamedData();
        textNodes = FXCollections.observableArrayList();

        Text text = new Text();
        text.setFill(Color.RED);
        textNodes.add(text);
    }

    @Test
    public void basicTest() throws Exception {

        streamedData.append("1234");

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(1, textNodes.size());
        assertEquals("1234", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());
    }

    @Test
    public void colorTest() throws Exception {

        streamedData.append("1234");
        streamedData.addColour(2, Color.GREEN);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(2, textNodes.size());

        assertEquals("12", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());

        assertEquals("34", ((Text)textNodes.get(1)).getText());
        assertEquals(Color.GREEN, ((Text)textNodes.get(1)).getFill());
    }

    @Test
    public void addColorToNextNodeTest() throws Exception {

        streamedData.append("123456");
        streamedData.addColour(2, Color.GREEN);
        streamedData.addColour(3, Color.RED);
        streamedData.addColour(5, Color.BLUE);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(4, textNodes.size());

        assertEquals("12", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());
        assertEquals("3", ((Text)textNodes.get(1)).getText());
        assertEquals(Color.GREEN, ((Text)textNodes.get(1)).getFill());
        assertEquals("45", ((Text)textNodes.get(2)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(2)).getFill());
        assertEquals("6", ((Text)textNodes.get(3)).getText());
        assertEquals(Color.BLUE, ((Text)textNodes.get(3)).getFill());
    }

    @Test
    public void colourToBeInsertedOnNextCharTest() throws Exception {

        streamedData.setColorToBeInsertedOnNextChar(Color.GREEN);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(null, streamedData.getColorToBeInsertedOnNextChar());

        assertEquals(2, textNodes.size());

        assertEquals("", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());
        assertEquals("", ((Text)textNodes.get(1)).getText());
        assertEquals(Color.GREEN, ((Text)textNodes.get(1)).getFill());
    }

    @Test
    public void newLineAtStartTest() throws Exception {

        streamedData.append("1234");
        streamedData.addNewLineMarkerAt(0);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(1, textNodes.size());
        assertEquals("\n1234", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());
    }

    @Test
    public void newLineInMiddleTest() throws Exception {

        streamedData.append("1234");
        streamedData.addNewLineMarkerAt(2);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(1, textNodes.size());
        assertEquals("12\n34", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());
    }

    @Test
    public void newLineAtEndTest() throws Exception {

        streamedData.append("1234");
        streamedData.addNewLineMarkerAt(4);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(1, textNodes.size());
        assertEquals("1234\n", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());
    }

    @Test
    public void colorsAndNewLinesTest() throws Exception {

        streamedData.append("1234");
        streamedData.addColour(2, Color.GREEN);
        streamedData.addNewLineMarkerAt(3);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(2, textNodes.size());

        assertEquals("12", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());

        assertEquals("3\n4", ((Text)textNodes.get(1)).getText());
        assertEquals(Color.GREEN, ((Text)textNodes.get(1)).getFill());
    }

    @Test
    public void colorsAndNewLineAtEndOfSecondNodeTest() throws Exception {

        streamedData.append("1234");
        streamedData.addColour(2, Color.GREEN);
        streamedData.addNewLineMarkerAt(4);

        streamedData.shiftToTextNodes(textNodes, textNodes.size());

        assertEquals(2, textNodes.size());

        assertEquals("12", ((Text)textNodes.get(0)).getText());
        assertEquals(Color.RED, ((Text)textNodes.get(0)).getFill());

        assertEquals("34\n", ((Text)textNodes.get(1)).getText());
        assertEquals(Color.GREEN, ((Text)textNodes.get(1)).getFill());
    }

}