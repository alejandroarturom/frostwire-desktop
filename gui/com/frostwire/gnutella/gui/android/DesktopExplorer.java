package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class DesktopExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362861227107918643L;

	private JList _list;
	
	private LocalFileListModel _model;
	
	public DesktopExplorer() {
		
		_model = new LocalFileListModel();
		
		_list = new JList(_model);
		_list.setCellRenderer(new LocalFileRenderer());
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		_list.setVisibleRowCount(-1);
		
		JScrollPane scrollPane = new JScrollPane(_list);
		
		add(scrollPane);
		
		_model.setRoot("/Users/atorres");
	}
}
