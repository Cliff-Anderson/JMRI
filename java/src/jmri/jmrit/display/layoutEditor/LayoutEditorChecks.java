package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of tools to check various things on the layout editor panel.
 */
public class LayoutEditorChecks {

    private final LayoutEditor layoutEditor;
    private JMenu checkMenu = new JMenu(Bundle.getMessage("CheckMenuTitle"));
    private JMenuItem checkInProgressMenuItem = new JMenuItem(Bundle.getMessage("CheckInProgressMenuItemTitle"));
    private JMenuItem checkNoResultsMenuItem = new JMenuItem(Bundle.getMessage("CheckNoResultsMenuItemTitle"));

    // Check Un-Connected Tracks
    private JMenu checkUnConnectedTracksMenu = new JMenu(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));

    // Check Un-Blocked Tracks
    private JMenu checkUnBlockedTracksMenu = new JMenu(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));

    // Check Non-Contiguous Blocks
    private JMenu checkNonContiguousBlocksMenu = new JMenu(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));

    /**
     * The constructor for this class
     *
     * @param layoutEditor the layout editor that uses this class
     */
    public LayoutEditorChecks(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    /**
     * set the layout editor checks menu (in the tools menu)
     *
     * @param toolsMenu where to add our "Check" menu and sub-menus
     */
    protected void setupChecksMenu(@Nonnull JMenu toolsMenu) {
        toolsMenu.add(checkMenu);
        checkMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                boolean enabled = layoutEditor.isEditable();
                checkUnConnectedTracksMenu.setEnabled(enabled);
                checkUnBlockedTracksMenu.setEnabled(enabled);
                checkNonContiguousBlocksMenu.setEnabled(enabled);
            }

            @Override

            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        }
        );
        checkMenu.setEnabled(layoutEditor.isEditable());
        checkMenu.setToolTipText(Bundle.getMessage("CheckMenuToolTip"));

        checkNoResultsMenuItem.setToolTipText(Bundle.getMessage("CheckNoResultsMenuItemToolTip"));
        checkNoResultsMenuItem.setEnabled(false);
        checkInProgressMenuItem.setToolTipText(Bundle.getMessage("CheckInProgressMenuItemToolTip"));
        checkInProgressMenuItem.setEnabled(false);

        //
        //  check for tracks with free connections
        //
        checkUnConnectedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
        checkUnConnectedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
        checkUnConnectedTracksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkUnConnectedTracksMenu);

        checkUnConnectedTracksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckUnConnectedTracksMenu();
            }

            @Override
            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });

        //
        //  check for tracks without assigned blocks
        //
        checkUnBlockedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnBlockedTracksMenuToolTip"));
        checkUnBlockedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnBlockedTracksMenuToolTip"));
        checkUnBlockedTracksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkUnBlockedTracksMenu);

        checkUnBlockedTracksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckUnBlockedTracksMenu();
            }

            @Override
            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });

        //
        // check for non-contiguous blocks
        //
        checkNonContiguousBlocksMenu.setToolTipText(Bundle.getMessage("CheckNonContiguousBlocksMenuToolTip"));
        checkNonContiguousBlocksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkNonContiguousBlocksMenu);

        checkNonContiguousBlocksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckNonContiguousBlocksMenu();
            }

            @Override
            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });
    }

    //
    // run the un-connected tracks check and populate the checkUnConnectedTracksMenu
    //
    private void setupCheckUnConnectedTracksMenu() {
        log.debug("setupcheckUnConnectedTracksMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkUnConnectedTracksMenu);

        // mark our menu as "in progress..."
        checkUnConnectedTracksMenu.removeAll();
        checkUnConnectedTracksMenu.add(checkInProgressMenuItem);

        // check all tracks for free connections
        List<String> trackNames = new ArrayList<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            //if (layoutTrack.getName().equals("EC5")) {
            //    log.debug("Stop here!");
            //}
            List<Integer> connections = layoutTrack.checkForFreeConnections();
            if (!connections.isEmpty()) {
                // add this track's name to the list of track names
                trackNames.add(layoutTrack.getName());
            }
        }

        // clear the "in progress..." menu item
        checkUnConnectedTracksMenu.removeAll();

        // for each un-connected track we found...
        if (trackNames.size() > 0) {
            for (String trackName : trackNames) {
                // create a menu item for it
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(trackName);
                checkUnConnectedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
//                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
//                    String menuItemName = menuItem.getText();
                    doCheckUnConnectedTracksMenuItem(trackName);
                });

                // if it's in the check marked set then (re-)checkmark it
                for (String item : checkMarkedMenuItemNamesSet) {
                    if (item.equals(trackName)) {
                        jmi.setSelected(true);
                        break;
                    }
                }
            }
        } else {
            checkUnConnectedTracksMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckUnConnectedTracksMenu

    //
    // action to be performed when checkUnConnectedTracksMenu item is clicked
    //
    private void doCheckUnConnectedTracksMenuItem(@Nonnull String menuItemName) {
        log.debug("docheckUnConnectedTracksMenuItem({})", menuItemName);
        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            Rectangle2D trackBounds = layoutTrack.getBounds();
            double minScale = Math.min(trackBounds.getWidth(), trackBounds.getHeight());
            if (minScale < 3.0) {
                trackBounds = MathUtil.scale(trackBounds, 3.0 / minScale);
            }
            layoutEditor.setSelectionRect(trackBounds);

            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }   // doCheckUnConnectedTracksMenuItem

    //
    // run the un-blocked tracks check and populate the checkUnBlockedTracksMenu
    //
    private void setupCheckUnBlockedTracksMenu() {
        log.debug("setupCheckUnBlockedTracksMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkUnBlockedTracksMenu);

        // mark our menu as "in progress..."
        checkUnBlockedTracksMenu.removeAll();
        checkUnBlockedTracksMenu.add(checkInProgressMenuItem);

        // check all tracks for un-assigned blocks
        List<String> trackNames = new ArrayList<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            if (!layoutTrack.checkForUnAssignedBlocks()) {
                // add this track to the list of un-assigned track names
                trackNames.add(layoutTrack.getName());
            }
        }

        // clear the "in progress..." menu item
        checkUnBlockedTracksMenu.removeAll();

        // for each tracks with un-assigned blocks that we found...
        if (trackNames.size() > 0) {
            for (String trackName : trackNames) {
                // create a menu item for it
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(trackName);
                checkUnBlockedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
//                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
//                    String menuItemName = menuItem.getText();
                    doCheckUnBlockedTracksMenuItem(trackName);
                });

                // if it's in the check marked set then (re-)checkmark it
                for (String item : checkMarkedMenuItemNamesSet) {
                    if (item.equals(trackName)) {
                        jmi.setSelected(true);
                        break;
                    }
                }
            }
        }
    }   // setupCheckUnBlockedTracksMenu

    //
    // action to be performed when checkUnBlockedTracksMenuItem is clicked
    //
    private void doCheckUnBlockedTracksMenuItem(@Nonnull String menuItemName) {
        log.debug("doCheckUnBlockedTracksMenuItem({})", menuItemName);

        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);

            layoutEditor.getLayoutTrackEditors().editLayoutTrack(layoutTrack);
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }   // doCheckUnBlockedTracksMenuItem

    //
    // run the non-contiguous blocks check and populate the checkNonContiguousBlocksMenu
    //
    private void setupCheckNonContiguousBlocksMenu() {
        log.debug("setupCheckNonContiguousBlocksMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkNonContiguousBlocksMenu);

        // mark our menu as "in progress..."
        checkNonContiguousBlocksMenu.removeAll();
        checkNonContiguousBlocksMenu.add(checkInProgressMenuItem);

        // check all tracks for non-contiguous blocks
        HashMap<String, Set<String>> blocksMap = new HashMap<>();
        Set<String> badBlocks = new TreeSet<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            //if (layoutTrack.getName().equals("EC5")
            //        || layoutTrack.getName().equals("T20")
            //        || layoutTrack.getName().equals("TO9")) {
            //    log.debug("Stop here!");
            //}
            layoutTrack.checkForNonContiguousBlocks(blocksMap, badBlocks);
        }

        // clear the "in progress..." menu item
        checkNonContiguousBlocksMenu.removeAll();

        // for each bad block we found...
        if (badBlocks.size() > 0) {
            for (String blockName : badBlocks) {
                // create a menu item for it
                Set<String> trackSet = blocksMap.get(blockName);
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(blockName + " (" + trackSet.size() + ")");
                checkNonContiguousBlocksMenu.add(jmi);

                // we currently don't have anything to do for non-contiguous 
                // blocks other than to just add them to the menu.
                jmi.addActionListener((ActionEvent event) -> {
                    //log.info("event: {}", event.paramString());
                    //JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
                    //String menuItemName = menuItem.getText();
                    //String blockName = menuItemName.replaceAll("\\s\\([^)]+\\)$", "");
                    doCheckNonContiguousBlocksMenuItem(blockName, blocksMap.get(blockName));
                });

                // if it's in the check marked set then (re-)checkmark it
                for (String item : checkMarkedMenuItemNamesSet) {
                    if (item.startsWith(blockName + " (")) {
                        jmi.setSelected(true);
                        break;
                    }
                }
            }
        } else {
            checkNonContiguousBlocksMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckNonContiguousBlocksMenu

    // collect the names of all checkbox menu items with checkmarks
    private Set<String> getCheckMarkedMenuItemNames(@Nonnull JMenu menu) {
        Set<String> results = new HashSet<>();
        for (int idx = 0; idx < menu.getMenuComponentCount(); idx++) {
            Component menuComponent = menu.getMenuComponent(idx);
            if (menuComponent instanceof JCheckBoxMenuItem) {
                JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) menuComponent;
                if (checkBoxMenuItem.isSelected()) {
                    results.add(checkBoxMenuItem.getText());
                }
            }
        }
        return results;
    }   // getCheckMarkedMenuItemNames

// action to be performed when checkNonContiguousBlocksMenu item is clicked
    private void doCheckNonContiguousBlocksMenuItem(
            @Nonnull String blockName,
            @Nullable Set<String> trackNamesSet) {
        log.debug("doCheckNonContiguousBlocksMenuItem({})", blockName);

        if (trackNamesSet != null) {
            // collect all the bounds...
            Rectangle2D bounds = null;
            for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
                if (trackNamesSet.contains(layoutTrack.getName())) {
                    Rectangle2D trackBounds = layoutTrack.getBounds();
                    if (bounds == null) {
                        bounds = trackBounds.getBounds2D();
                    } else {
                        bounds.add(trackBounds);
                    }
                }
            }
            layoutEditor.setSelectionRect(bounds);

            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();

            // amend all tracks in this block to the layout editor selection group
            for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
                if (trackNamesSet.contains(layoutTrack)) {
                    layoutEditor.amendSelectionGroup(layoutTrack);
                }
            }
        } else {
            layoutEditor.setSelectionRect(MathUtil.zeroRectangle2D);
        }
    }   // doCheckNonContiguousBlocksMenuItem

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorChecks.class
    );
}   // class LayoutEditorChecks
