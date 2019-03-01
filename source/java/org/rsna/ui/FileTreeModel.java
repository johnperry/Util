/*---------------------------------------------------------------
*  Copyright 20054 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*
*  Adapted from code by Christian Kaufhold (ch-kaufhold@gmx.de)
*----------------------------------------------------------------*/

package org.rsna.ui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.EventListenerList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * A simple static TreeModel containing a java.io.File directory structure.
 */
public class FileTreeModel
	implements TreeModel, Serializable, Cloneable {

	protected EventListenerList listeners;
	private static final Object LEAF = new Serializable() { };
	private Map<File,Object> map;
	private File root;
	private FileFilter filter;

	public FileTreeModel(File root, GeneralFileFilter filter) {
		this.root = root;
		this.filter = filter;
		this.map = new HashMap<File,Object>();
		if (!root.isDirectory()) map.put(root, LEAF);
		this.listeners = new EventListenerList();
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		return map.get(node) == LEAF;
	}

	public int getChildCount(Object node) {
		List<Object> children = children(node);
		if (children == null) return 0;
		return children.size();
	}

	public Object getChild(Object parent, int index) {
		return children(parent).get(index);
	}

	public int getIndexOfChild(Object parent, Object child) {
		return children(parent).indexOf(child);
	}

	@SuppressWarnings("unchecked")
	protected List<Object> children(Object node) {
		File f = (File)node;
		Object value = map.get(f);
		if (value == LEAF) return null;
		List<Object> children = (List<Object>)value; //offending unchecked cast
		if (children == null) {
			File[] c = f.listFiles(filter);
			if (c != null) {
				children = new ArrayList<Object>(c.length);
				for (int len = c.length, i = 0; i < len; i++) {
					children.add(c[i]);
					if (!c[i].isDirectory()) map.put(c[i], LEAF);
				}
			}
			else children = new ArrayList<Object>(0);
			map.put(f, children);
		}
		return children;
	}

	public void valueForPathChanged(TreePath path, Object value) { }

	public void addTreeModelListener(TreeModelListener l){
		listeners.add(TreeModelListener.class, l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(TreeModelListener.class, l);
	}

	public Object clone() {
		try {
			FileTreeModel clone = (FileTreeModel)super.clone();
			clone.listeners = new EventListenerList();
			clone.map = new HashMap<File,Object>(map);
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

}