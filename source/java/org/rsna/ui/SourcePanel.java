/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * A JPanel to display a DirectoryPane and accompanying GUI components
 * to allow directory browsing and file selection.
 */
public class SourcePanel extends JPanel implements FileListener {

	DirectoryPane directoryPane;
	HeaderPanel headerPanel;
	FooterPanel footerPanel;
	GeneralFileFilter filter;
	ApplicationProperties properties;
	Color background;

	/**
	 * Class constructor providing a default heading of "Source".
	 * @param properties the Properties object containing the initial values
	 * of the extensions list and the path to be displayed. This object is
	 * updated in response to root changes, extension changes,
	 * and file selections. If any property is missing, a suitable one is
	 * supplied by default.
	 */
	public SourcePanel(ApplicationProperties properties) {
		this(properties,"Source");
	}

	/**
	 * Class constructor alloowing specification of the heading text.
	 * @param properties the Properties object containing the initial values
	 * of the extensions list and the path to be displayed. This object is
	 * updated in response to root changes, extension changes,
	 * and file selections. If any property is missing, a suitable one is
	 * supplied by default.
	 * @param heading the text to appear in the heading of the JPanel.
	 */
	public SourcePanel(ApplicationProperties properties, String heading) {
		this(properties,heading,null);
	}

	/**
	 * Class constructor alloowing specification of the heading text.
	 * @param properties the Properties object containing the initial values
	 * of the extensions list and the path to be displayed. This object is
	 * updated tin response to root changes, extension changes,
	 * and file selections. If any property is missing, a suitable one is
	 * supplied by default.
	 * @param heading the text to appear in the heading of the JPanel.
	 * @param background the background color or null if the default is to be used.
	 */
	public SourcePanel(ApplicationProperties properties, String heading, Color background) {
		super();
		this.properties = properties;
		if (background == null)
			this.background = Color.getHSBColor(0.58f, 0.17f, 0.95f);
		else
			this.background = background;

		// Make the file filter
		filter = new GeneralFileFilter();
		String extensions = properties.getProperty("extensions");
		if (extensions != null) filter.setExtensions(extensions);
		else {
			filter.addExtension(".dcm");
			properties.setProperty("extensions",filter.getExtensionString());
		}

		// Get the starting directory path from the properties.
		// If it is missing, start in the directory containing the program.
		String currentDirectoryPath = properties.getProperty("directory");
		if (currentDirectoryPath == null) currentDirectoryPath = System.getProperty("user.dir");

		// Create the UI components
		this.setLayout(new BorderLayout());
		directoryPane = new DirectoryPane(filter,currentDirectoryPath);
		directoryPane.addFileListener(this);
		headerPanel = new HeaderPanel(heading);
		footerPanel = new FooterPanel();
		this.add(headerPanel,BorderLayout.NORTH);
		this.add(directoryPane,BorderLayout.CENTER);
		this.add(footerPanel,BorderLayout.SOUTH);
	}

	/**
	 * Add a FileListener to the DirectoryPane. Higher level objects that
	 * have access only to this object can register with the DirectoryPane through
	 * this method, but the DirectoryPane will send the events.
	 * @param listener the object listening for FileEvents.
	 */
	public void addFileListener(FileListener listener) {
		directoryPane.addFileListener(listener);
	}

	/**
	 * Get the current filter used to select files for display in the directory tree.
	 * @return the current file filter.
	 */
	public GeneralFileFilter getFileFilter() {
		return filter;
	}

	/**
	 * Get the selection value of the "Include subdirectories" checkbox in the footer panel.
	 * @return true if the "Include subdirectories" checkbox is selected; false otherwise.
	 */
	public boolean getSubdirectories() {
		return footerPanel.subdirectories.isSelected();
	}

	/**
	 * Set the selection value of the "Include subdirectories" checkbox in the footer panel.
	 * @param selected the selection value for the chexckbox
	 */
	public void setSubdirectories(boolean selected) {
		footerPanel.subdirectories.setSelected(selected);
		properties.setProperty("subdirectories", (selected ? "yes" : "no"));
	}

	/**
	 * Get the current filter used to select files for display in the directory tree.
	 * @return the DirectoryPane containing the directory tree.
	 */
	public DirectoryPane getDirectoryPane() {
		return directoryPane;
	}

	/**
	 * The FileEvent listener.
	 * @param event the event.
	 */
	public void fileEventOccurred(FileEvent event) {
		File file = event.getFile();
		if (file != null) {
			String dirPath;
			if (file.isDirectory()) dirPath = file.getAbsolutePath();
			else dirPath = file.getParentFile().getAbsolutePath();
			properties.setProperty("directory", dirPath);
		}
		else properties.remove("directory");
	}

	class HeaderPanel extends JPanel implements ActionListener {
		public JComboBox<String> root;
		public JButton refresh;
		public HeaderPanel(String heading) {
			super();
			this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
			this.setBackground(background);
			JLabel panelLabel = new JLabel(" "+heading,SwingConstants.LEFT);
			Font labelFont = new Font("Dialog", Font.BOLD, 18);
			panelLabel.setFont(labelFont);
			this.add(panelLabel);
			this.add(Box.createHorizontalGlue());
			refresh = new JButton("Refresh");
			refresh.addActionListener(this);
			this.add(refresh);
			this.add(Box.createHorizontalStrut(5));
			root = new JComboBox<String>();
			Dimension d = root.getPreferredSize();
			d.width = 90;
			root.setPreferredSize(d);
			root.setMaximumSize(d);
			File[] roots = directoryPane.getRoots();
			for (int i=0; i<roots.length; i++) root.addItem(roots[i].getAbsolutePath());
			this.add(root);
			root.setSelectedIndex(directoryPane.getCurrentRootIndex());
			root.addActionListener(this);
			this.add(Box.createHorizontalStrut(17));
		}
		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();
			if (source.equals(root)) {
				root.setSelectedIndex(directoryPane.changeRoot(root.getSelectedIndex()));
			}
			else if (source.equals(refresh)) {
				directoryPane.reloadTree();
			}
		}
	}

	class FooterPanel extends JPanel implements ActionListener {
		public JCheckBox subdirectories;
		public JButton extensionButton;
		public FooterPanel() {
			super();
			this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
			this.setBackground(Color.getHSBColor(0.58f, 0.17f, 0.95f));
			subdirectories = new JCheckBox("Include subdirectories");
			subdirectories.addActionListener(this);
			subdirectories.setSelected(properties.getProperty("subdirectories", "yes").equals("yes"));
			subdirectories.setBackground(background);
			this.add(subdirectories);
			this.add(Box.createHorizontalGlue());
			extensionButton = new JButton(filter.getDescription());
			this.add(extensionButton);
			this.add(Box.createHorizontalStrut(17));
			extensionButton.addActionListener(this);
		}
		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource().equals(extensionButton)) {
				Component parent = extensionButton.getParent();
				while ((parent != null) && !(parent instanceof Frame)) parent = parent.getParent();
				String newExtensions = JOptionPane.showInputDialog(
							parent,
							"Edit the extension list.\nSeparate extensions by commas.\n\n",
							filter.getExtensionString());
				if ((newExtensions != null) && !newExtensions.trim().equals("")) {
					newExtensions = newExtensions.replaceAll("\\s","");
					filter.setExtensions(newExtensions);
					extensionButton.setText(filter.getDescription());
					properties.setProperty("extensions",filter.getExtensionString());
					directoryPane.reloadTree();
				}
			}
			else if (evt.getSource().equals(subdirectories)) {
				boolean sel = subdirectories.isSelected();
				properties.setProperty("subdirectories", (sel ? "yes" : "no"));
			}
		}
	}

}
