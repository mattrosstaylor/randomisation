package uk.ac.soton.ecs.lifeguide.randomisation.app;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The initial steps towards a GUI for managing trials. Unused,
 * will be finished if time allows.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class TrialGUI {

    private static final Logger logger = LoggerFactory.getLogger(TrialGUI.class);

    public static final String NO_SELECTION_STRING = "None";
    public static final String LOAD_TRIAL_STRING = "Load a new trial...";

    private static final String DATA_DIR = "data/";
    private static final String LOCK_FILE_PATH = DATA_DIR + "viewer.lock";
    private static final String ICON_URL = "/logo.png";

    public static ErrorPanel errorPanel;

    private JFrame frame;
    private JPanel mainPanel;
    private LocalDBConnector database;

    public void init() {

        database = new LocalDBConnector();
        database.connect();

        // Main frame/panel
        this.frame = new JFrame("Trial Viewer");
        mainPanel = (JPanel) frame.getContentPane();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(new EmptyBorder(2, 2, 2, 0));

        this.setUIStyle();
        this.loadFrameIcon(frame, ICON_URL);

        // Top area
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.X_AXIS));

        // Trial name text field
        JComboBox trialComboBox = new JComboBox();
        trialComboBox.setEditable(false);
        populateTrialList(trialComboBox);
        topBox.add(trialComboBox);

        // Set up listeners to update the GUI every time the trial changes.
        TrialChangeDistributor notifier = new TrialChangeDistributor(trialComboBox, database);
        trialComboBox.addActionListener(notifier);

        // Delete button
        JButton deleteButton = new JButton("Delete");
        topBox.add(deleteButton);
        topBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, trialComboBox.getHeight()));
        mainPanel.add(topBox);

        // Main area (tabbed panel)
        JTabbedPane tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane);

        // Trial information text area (tab 1)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.X_AXIS));
        JTextArea detailsText = new JTextArea();
        detailsText.setLineWrap(true);
        detailsText.setWrapStyleWord(true);
        detailsText.setMargin(new Insets(5, 5, 0, 5));
        detailsText.setEditable(false);
        detailsText.setFont(AppFonts.defaultFont);
        // Prevents the app from auto-scrolling to the bottom if the text extends outside of the window's size.
        ((DefaultCaret) detailsText.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        int vsp = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
        int hsp = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
        JScrollPane detailsScrollPane = new JScrollPane(detailsText, vsp, hsp);
        detailsPanel.add(detailsScrollPane);
        tabbedPane.addTab("Trial Details", detailsPanel);

        // Allocation information text area (tab 2)
        JPanel allocations = new JPanel();
        allocations.setLayout(new BoxLayout(allocations, BoxLayout.Y_AXIS));
        JTextArea allocationsText = new JTextArea();
        allocationsText.setLineWrap(true);
        allocationsText.setWrapStyleWord(true);
        allocationsText.setMargin(new Insets(5, 5, 0, 5));
        allocationsText.setEditable(false);
        allocationsText.setFont(AppFonts.defaultFont);
        // Prevents the app from auto-scrolling to the bottom if the text extends outside of the window's size.
        ((DefaultCaret) allocationsText.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        JScrollPane allocationsScrollPane = new JScrollPane(allocationsText, vsp, hsp);
        allocations.add(allocationsScrollPane);

        // Allocation button area (tab 2)
        JPanel optionsPanel = new JPanel();
        JButton allocateButton = new JButton("Random"); // Random allocation button
        optionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, allocateButton.getHeight()));
        optionsPanel.add(allocateButton);
        JButton importButton = new JButton("Import"); // CSV allocation button
        optionsPanel.add(importButton);
        JButton exportButton = new JButton("Export"); // CSV export button
        optionsPanel.add(exportButton);
        allocations.add(optionsPanel);
        tabbedPane.addTab("Allocations", allocations);

        // New allocation area (tab 3)
        JPanel manualAllocPanel = new JPanel();
        manualAllocPanel.setLayout(new BoxLayout(manualAllocPanel, BoxLayout.Y_AXIS));
        AllocationForm allocationForm = new AllocationForm(notifier);
        JScrollPane formScrollPane = new JScrollPane(allocationForm, vsp, hsp);
        manualAllocPanel.add(formScrollPane);

        // Allocation button area (tab 3) -- same as in tab 2, but no clone is provided for Swing components.
        // They are serializable, but serializing then deserializing is probably overkill, so this bit of code
        // is basically duplicated.
        JPanel optionsPanel2 = new JPanel();
        JButton allocateButton2 = new JButton("Random"); // Random allocation button
        optionsPanel2.setMaximumSize(new Dimension(Integer.MAX_VALUE, allocateButton2.getHeight()));
        optionsPanel2.add(allocateButton2);
        JButton importButton2 = new JButton("Import"); // CSV allocation button
        optionsPanel2.add(importButton2);
        JButton exportButton2 = new JButton("Export"); // CSV export button
        optionsPanel2.add(exportButton2);
        manualAllocPanel.add(optionsPanel2);
        tabbedPane.addTab("New Allocation", manualAllocPanel);

        // Delete confirmation action
        TrialDeleteButton deleteAction = new TrialDeleteButton(trialComboBox, database, notifier);
        deleteButton.addActionListener(deleteAction);

        // Allocation window setup
        AllocationDialogBox allocationBox = new AllocationDialogBox(mainPanel, notifier);
        allocateButton.addActionListener(allocationBox);
        allocateButton2.addActionListener(allocationBox);

        // Import button setup
        LoadCSVDialog csvLoadAction = new LoadCSVDialog(mainPanel, database, notifier);
        importButton.addActionListener(csvLoadAction);
        importButton2.addActionListener(csvLoadAction);

        SaveCSVDialog csvSaveAction = new SaveCSVDialog(mainPanel, database, notifier);
        exportButton.addActionListener(csvSaveAction);
        exportButton2.addActionListener(csvSaveAction);

        // Trial change listeners
        TrialTextArea trialText = new TrialTextArea(detailsText);    // Update trial details text
        FocusGrabber focusGrabber = new FocusGrabber(mainPanel);    // Set window focus to the main panel (removes selection box highlight).
        AllocationTextArea allocText = new AllocationTextArea(allocationsText); // Shows allocation statistics

        notifier.addObserver(trialText);
        notifier.addObserver(focusGrabber);
        notifier.addObserver(allocText);
        notifier.addObserver(allocationBox);
        notifier.addObserver(allocationForm);

        // Error panel
        errorPanel = new ErrorPanel(frame);
        //tabbedPane.addTab("erirs", errorPanel);

        // Finalise the setup, creating the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 500);
        frame.setMinimumSize(new Dimension(350, 350));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        detailsPanel.requestFocusInWindow();
    }

    public void setUIStyle() {
        try {
            boolean nimbusAvailable = false;

            // Check for the 'Nimbus' UI style.
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusAvailable = true;
                    break;
                }
            }

            boolean runningWindows = System.getProperty("os.name").toLowerCase().contains("windows");

            // Fall back on the Windows look and feel if Nimbus isn't found.
            // Don't do it with Linux, since the GTK file browser look and feel is worse than the Java default.
            if (!nimbusAvailable && runningWindows) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            // Do nothing, use the default look and feel.
        }
    }

    public void loadFrameIcon(JFrame frame, String path) {
        try {
            frame.setIconImage(new ImageIcon(ImageIO.read(getClass().getResourceAsStream(path))).getImage());
        } catch (Exception e) {
            // Image not found, use the default icon.
        }
    }

    public void populateTrialList(JComboBox comboBox) {
        comboBox.removeAllItems();
        comboBox.addItem(NO_SELECTION_STRING);

        for (String trialName : database.getFilePaths().keySet())
            comboBox.addItem(trialName);

        comboBox.addItem(LOAD_TRIAL_STRING);
    }

    public JFrame getFrame() {
        return frame;
    }

    public LocalDBConnector getDatabase() {
        return database;
    }

    public void releaseLock(File lockFile, RandomAccessFile raFile, FileLock lock) {
        try {
            lock.release();
            raFile.close();
            lockFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Create the data directory if it does not exist.
            File dir = new File(DATA_DIR);
            if (!dir.exists()) {
                dir.mkdir();
            }

            // Attempt to access the lock file, and lock on it.
            // If the lock fails, another instance of the application is already running.
            final File file = new File(LOCK_FILE_PATH);
            final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
            final FileLock lock = raFile.getChannel().tryLock();

            if (lock != null) {
                // Managed to get the lock, start the application. Ensure it releases the lock on close.
                final TrialGUI app = new TrialGUI();
                app.init();
                app.getFrame().addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        app.getDatabase().disconnect();
                        app.releaseLock(file, raFile, lock);
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, "You may only run one instance of the trial viewer at a time.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

