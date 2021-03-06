package ninja.mbedded.ninjaterm.view.mainWindow;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ninja.mbedded.ninjaterm.model.Model;
import ninja.mbedded.ninjaterm.model.terminal.Terminal;
import ninja.mbedded.ninjaterm.util.appInfo.AppInfo;
import ninja.mbedded.ninjaterm.util.loggerUtils.LoggerUtils;
import ninja.mbedded.ninjaterm.view.mainWindow.aboutDialogue.AboutDialogueViewController;
import ninja.mbedded.ninjaterm.view.mainWindow.statusBar.StatusBarViewController;
import ninja.mbedded.ninjaterm.view.mainWindow.terminal.TerminalViewController;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the main window of NinjaTerm.
 *
 * @author Geoffrey Hunter <gbmhunter@gmail.com> (www.mbedded.ninja)
 * @since 2016-07-08
 * @last-modified 2016-11-08
 */
public class MainWindowViewController {

    //================================================================================================//
    //========================================== FXML BINDINGS =======================================//
    //================================================================================================//

    @FXML
    public VBox mainVBox;

    //==============================================//
    //================ MENU OBJECTS ================//
    //==============================================//

    //==================== FILE ====================//

    @FXML
    public MenuItem newTerminalMenuItem;

    @FXML
    public MenuItem alwaysOnTopMenuItem;

    @FXML
    public MenuItem exitMenuItem;

    //==================== HELP ====================//

    @FXML
    public MenuItem helpAboutMenuItem;

    @FXML
    public TabPane terminalTabPane;

    @FXML
    private Tab newTerminalTab;

    @FXML
    public StatusBarViewController statusBarViewController;

    //================================================================================================//
    //=========================================== CLASS FIELDS =======================================//
    //================================================================================================//

    public List<TerminalViewController> terminalViewControllers = new ArrayList<>();

    private GlyphFont glyphFont;

    private Model model;

    private Stage stage;

    private Logger logger = LoggerUtils.createLoggerFor(getClass().getName());

    //================================================================================================//
    //========================================== CLASS METHODS =======================================//
    //================================================================================================//

    public MainWindowViewController() {

    }

    /**
     * Initialises everything that cannot be done in the constructor (due to JavaFX creating the
     * object automatically).
     *
     * @param model
     * @param glyphFont
     */
    public void init(Model model, GlyphFont glyphFont, Stage stage) {

        logger.debug("init() called.");

        this.model = model;
        this.glyphFont = glyphFont;
        this.stage = stage;

        //==============================================//
        //=================== MENU SETUP ===============//
        //==============================================//

        Glyph glyph;

        //==================== FILE ====================//

        glyph = glyphFont.create(FontAwesome.Glyph.TERMINAL);
        glyph.setColor(Color.BLACK);
        newTerminalMenuItem.setGraphic(glyph);
        newTerminalMenuItem.setOnAction(event -> {
            logger.debug("newTerminalMenuItem clicked.");
            model.createTerminal();
        });

        glyph = glyphFont.create(FontAwesome.Glyph.CHECK);
        glyph.setColor(Color.BLACK);
        alwaysOnTopMenuItem.setGraphic(glyph);
        alwaysOnTopMenuItem.setOnAction(event -> {
            logger.debug("alwaysOnTopMenuItem clicked.");
            // Perform toggle
            model.alwaysOnTop.set(!model.alwaysOnTop.get());
        });

        glyph = glyphFont.create(FontAwesome.Glyph.SIGN_OUT);
        glyph.setColor(Color.BLACK);
        exitMenuItem.setGraphic(glyph);
        exitMenuItem.setOnAction(event -> {
            // Quit the application
            Platform.exit();
        });

        //==================== HELP ====================//

        helpAboutMenuItem.setOnAction(event -> handleAboutMenuItemClicked());

        //==============================================//
        //================ TERMINAL SETUP ==============//
        //==============================================//

        // Install listener for when a new terminal object is created
        model.terminalCreatedListeners.add(terminal -> {
            handleTerminalCreated(terminal);
        });

        // Install handler for when the model emits a terminal
        // closed event
        model.closedTerminalListeners.add(terminal -> {
            handleCloseTerminal(terminal);
        });

        // This makes the "+" sign more visible
        newTerminalTab.setStyle("-fx-font-weight: bold;");

        // Install click handler for the "new terminal" tab
        terminalTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Firstly, check if newly selected tab is the "new terminal tab" (the "+" sign)
            if (newValue == newTerminalTab) {
                logger.debug("\"New terminal\" tab clicked.");
                model.createTerminal();
                return;
            }

            // Selected tab must of been an existing terminal, tell the model about it
            for (TerminalViewController terminalViewController : terminalViewControllers) {
                if (terminalViewController.getTerminalTab() == newValue) {
                    model.newTerminalSelected(terminalViewController.getTerminal());
                    return;
                }
            }

            throw new RuntimeException("Selected tab was not recognised!");

        });

        //==============================================//
        //================== STATUS BAR ================//
        //==============================================//

        statusBarViewController.init(model, glyphFont);

        //==============================================//
        //===== ATTACH LISTENER TO BLUR MAIN WINDOW ====//
        //==============================================//

        model.isPrimaryStageBlurred.addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                // Blur main window
                mainVBox.setEffect(new BoxBlur());
            } else {
                // Make sure main window is not blurred
                mainVBox.setEffect(null);
            }
        });

        //==============================================//
        //==== SETUP "ALWAYS ON TOP" FUNCTIONALITY =====//
        //==============================================//

        model.alwaysOnTop.addListener((observable, oldValue, newValue) -> {
            alwaysOnTopListener();
        });

        // Configure default
        alwaysOnTopListener();

    }

    public void alwaysOnTopListener() {
        if(model.alwaysOnTop.getValue()) {
            stage.setAlwaysOnTop(true);
            alwaysOnTopMenuItem.getGraphic().setVisible(true);
            model.status.addMsg("\"Always On Top\" mode enabled.");
        } else {
            stage.setAlwaysOnTop(false);
            alwaysOnTopMenuItem.getGraphic().setVisible(false);
            model.status.addMsg("\"Always On Top\" mode disabled.");
        }
    }

    /**
     * Adds a new terminal tab view to the main window view. Called by a terminalCreatedListener.
     */
    public void handleTerminalCreated(Terminal terminal) {

        logger.debug("handleTerminalCreated() called.");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("terminal/TerminalView.fxml"));

        Tab terminalTab;
        try {
            terminalTab = (Tab) loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        terminalTab.setOnCloseRequest(event -> {
            logger.debug("terminalTab.setOnCloseRequest() called.");
            model.closeTerminal(terminal);
        });

        // Extract controller from this view and perform setup on it
        TerminalViewController terminalViewController = loader.getController();
        terminalViewController.init(model, terminal, glyphFont);

        // Save a reference to this TerminalView controller
        terminalViewControllers.add(terminalViewController);

        // Peform a scan of the COM ports on start-up
        //terminalViewController.comSettingsViewController.scanComPorts();
        terminal.comPortSettings.scanComPorts();

        // Add the Tab view to the TabPane
        // NOTE: The last tab is the static "new tab" button, so always insert a new tab
        // before this one
        terminalTabPane.getTabs().add(terminalTabPane.getTabs().size() - 1, terminalTab);

        // Select this newly created terminal tab
        terminalTabPane.getSelectionModel().select(terminalTab);
    }

    /**
     * Handler for a CloseTerminal event sent from the model.
     *
     * @param terminal
     */
    public void handleCloseTerminal(Terminal terminal) {

        // Find the terminal controller associated with this terminal
        for (TerminalViewController terminalViewController : terminalViewControllers) {
            if (terminalViewController.getTerminal() == terminal) {
                // We have found the correct terminal to close
                // Remove both the Tab from the TabPane and the TerminalController
                // from the list
                terminalTabPane.getTabs().remove(terminalViewController.getTerminalTab());
                terminalViewControllers.remove(terminalViewController);
                return;
            }
        }

        // If we get here, we didn't find the terminal!
        throw new RuntimeException("Terminal close request received, but could not find the requested terminal to close.");

    }

    public void handleAboutMenuItemClicked() {
        String versionNumber = AppInfo.getVersionNumber();

        // Sometimes the version number will be null, but this should only occur when running from IntelliJ
        // in a development environment (install4j will add the appropriate .dll when a .exe is built)
        if (versionNumber == null) {
            versionNumber = "?.?.?";
        }

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);
        Parent parent;
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            parent = fxmlLoader.load(getClass().getResource("aboutDialogue/AboutDialogueView.fxml").openStream());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        AboutDialogueViewController aboutDialogueViewController = fxmlLoader.getController();

        aboutDialogueViewController.init(model, glyphFont, dialog);

        Scene dialogScene = new Scene(parent, 400, 300);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
