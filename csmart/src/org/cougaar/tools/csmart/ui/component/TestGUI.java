/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui.component;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.lang.reflect.Array;

public class TestGUI
    extends JPanel
    implements PropertyListener, TreeSelectionListener
{
    static final long serialVersionUID = 148379972397960896L;

    public static final int LABEL_COL = 0;
    public static final int VALUE_COL = 1;
    public static final int FULLNAME_COL = 2;
    public static final int DEFAULT_COL = 3;
    public static final int ALLOWED_COL = 4;
    public static final int CLASS_COL = 5;
    public static final int NCOL = 6;
    private MyTableModel model = new MyTableModel();
    private JTable view = new JTable(model);
    private JScrollPane detail = new JScrollPane(view);
    private TreeNode root;
    private DefaultTreeModel treeModel = null;
    private JTree tree = new JTree();

    private JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private Map nodeMap = new HashMap();
    private TreeNode getTreeNodeFor(ConfigurableComponent comp) {
        if (comp == null) return null;
        TreeNode result = (TreeNode) nodeMap.get(comp);
        if (result == null) {
            result = new MyTreeNode(comp);
            nodeMap.put(comp, result);
        }
        return result;
    }

    private class MyTreeNode implements TreeNode, ChildConfigurationListener {
        private ConfigurableComponent component;

        public MyTreeNode(ConfigurableComponent component) {
            this.component = component;
            component.addChildConfigurationListener(this);
        }
        public void finalize() {
            component.removeChildConfigurationListener(this);
        }

        public void childConfigurationChanged() {
            nodeMap.clear();
            if (treeModel != null) treeModel.nodeStructureChanged(this);
        }
        public String toString() {
            return component.getFullName().toString();
        }
        public Enumeration children() {
            return new Enumeration() {
                int i = 0;
                public boolean hasMoreElements() {
                    return i < component.getChildCount();
                }
                public Object nextElement() {
                    return getTreeNodeFor(component.getChild(i++));
                }
            };
        }
        public boolean getAllowsChildren() {
            return true;
        }
        public TreeNode getChildAt(int childIndex) {
            return getTreeNodeFor(component.getChild(childIndex));
        }
        public int getChildCount() {
            return component.getChildCount();
        }
        public int getIndex(TreeNode node) {
            ConfigurableComponent child = ((MyTreeNode) node).component;
            for (int i = 0, n = getChildCount(); i < n; i++) {
                if (component.getChild(i) == child) return i;
            }
            return -1;
        }
        public TreeNode getParent() {
            return getTreeNodeFor(component.getParent());
        }
        public boolean isLeaf() {
            return getChildCount() == 0;
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        if (path == null) return;
        MyTreeNode node =
            (MyTreeNode) path.getLastPathComponent();
        setDetailComponent(node.component);
    }

    private static interface ArrayElementRenderer {
        String render(Object ary, int ix);
    }

    private static ArrayElementRenderer intRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((int[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer longRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((long[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer shortRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((short[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer byteRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((byte[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer charRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((char[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer doubleRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((double[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer floatRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((float[]) ary)[ix]);
            }
        };
    private static ArrayElementRenderer booleanRenderer =
        new ArrayElementRenderer() {
            public String render(Object ary, int ix) {
                return String.valueOf(((boolean[]) ary)[ix]);
            }
        };
    private static class ObjectArrayElementRenderer implements ArrayElementRenderer {
        Class cls;
        public ObjectArrayElementRenderer(Class cls) {
            this.cls = cls;
        }
        public String render(Object ary, int ix) {
            return renderValue(cls, ((Object[]) ary)[ix]);
        }
    }

    private static String renderValue(Class cls, Object val) {
        if (val == null) return "<not set>";
        if (cls.isArray()) {
            Class ccls = cls.getComponentType();
            if (ccls.isPrimitive()) {
                if (ccls == Integer.TYPE)   return renderArray(val, intRenderer);
                if (ccls == Long.TYPE)      return renderArray(val, longRenderer);
                if (ccls == Short.TYPE)     return renderArray(val, shortRenderer);
                if (ccls == Byte.TYPE)      return renderArray(val, byteRenderer);
                if (ccls == Character.TYPE) return renderArray(val, charRenderer);
                if (ccls == Double.TYPE)    return renderArray(val, doubleRenderer);
                if (ccls == Float.TYPE)     return renderArray(val, floatRenderer);
                if (ccls == Boolean.TYPE)   return renderArray(val, booleanRenderer);
            } else {
                return renderArray(val, new ObjectArrayElementRenderer(ccls));
            }
        }
        if (cls == Range.class) {
            return renderRange((Range) val);
        }
        return val.toString();
    }

    private static String renderRange(Range range) {
        return range.getMinimumValue() + ".." + range.getMaximumValue();
    }

    private static String renderAllowed(Class cls, Object allowed) {
        return renderValue(cls, allowed);
    }

    private static String renderObjectArray(Class cls, Object ary) {
        return renderArray(ary, new ObjectArrayElementRenderer(cls));
    }

    private static String renderArray(Object ary, ArrayElementRenderer renderer) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, n = Array.getLength(ary); i < n; i++) {
            if (i == 0) {
                buf.append("{");
            } else {
                buf.append(",");
            }
            buf.append(renderer.render(ary, i));
        }
        buf.append("}");
        return buf.toString();
    }

    public TestGUI() {
        super(new BorderLayout());
        pane.setLeftComponent(tree);
        pane.setRightComponent(detail);
        tree.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        JPanel buttons = new JPanel();
        JButton[] testButtons = getTestButtons();
        for (int i = 0; i < testButtons.length; i++) {
            buttons.add(testButtons[i]);
        }
        add(pane);
        add(buttons, BorderLayout.SOUTH);
    }

    protected JButton[] getTestButtons() {
        return new JButton[0];
    }

    public JFrame displayInFrame(ConfigurableComponent c) {
        setConfigurableComponent(c);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(this);
        frame.pack();
        frame.show();
        return frame;
    }

    public void setConfigurableComponent(ConfigurableComponent c) {
        TreeNode rootNode = getTreeNodeFor(c);
        if (treeModel == null) {
            treeModel = new DefaultTreeModel(rootNode);
            tree.setModel(treeModel);
        } else {
            treeModel.setRoot(rootNode);
        }
        setDetailComponent(c);
    }

    private void setDetailComponent(ConfigurableComponent c) {
        List rows = new ArrayList();
        view.removeAll();
        model.clear();
        for (Iterator i = c.getPropertyNames(); i.hasNext(); ) {
            CompositeName name = (CompositeName) i.next();
            Property prop = c.getProperty(name);
            if (prop == null) {
                System.err.println("Property "
                                   + name
                                   + " not found in "
                                   + c.getFullName());
                continue;
            }
            prop.addPropertyListener(this);
            rows.add(prop);
        }
        model.addAll(rows);
    }

    public void propertyValueChanged(PropertyEvent e) {
        propertyChanged(e);
    }

    public void propertyOtherChanged(PropertyEvent e) {
        propertyChanged(e);
    }

    public void propertyChanged(PropertyEvent e) {
        Property prop = e.getProperty();
        model.updateRow(prop);
    }

    private static Comparator propComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            Property p1 = (Property) o1;
            Property p2 = (Property) o2;
            return p1.getName().compareTo(p2.getName());
        }
    };

    private static class MyTableModel extends AbstractTableModel {
        private SortedSet rowSet = new TreeSet(propComparator);
        private List rowList = new ArrayList();

        public void clear() {
            int sz = rowSet.size();
            rowSet.clear();
            rowList.clear();
            if (sz > 0) fireTableRowsDeleted(0, sz - 1);
        }
        public void add(Property prop) {
            int insertionRow = rowSet.headSet(prop).size();
            rowSet.add(prop);
            rowList.clear();
            fireTableRowsInserted(insertionRow, insertionRow);
        }
        public void addAll(Collection c) {
            rowSet.addAll(c);
            rowList.clear();
            fireTableDataChanged();
        }
        public void updateRow(Property prop) {
            int row = rowSet.headSet(prop).size();
            fireTableRowsUpdated(row, row);
        }

        public int getColumnCount() {
            return NCOL;
        }
        public String getColumnName(int col) {
            switch (col) {
                case LABEL_COL: return "Label";
                case VALUE_COL: return "Value";
                case FULLNAME_COL: return "Full Name";
                case DEFAULT_COL: return "Default";
                case ALLOWED_COL: return "Allowed";
                case CLASS_COL: return "Class";
            }
            return null;
        }
        public Class getColumnClass(int col) {
            return String.class;
        }
        public int getRowCount() {
            return rowSet.size();
        }

        public Object getValueAt(int row, int col) {
            if (rowList.size() == 0) rowList.addAll(rowSet);
            Property prop = (Property) rowList.get(row);
            Class cls = prop.getPropertyClass();
            if (cls == null) {
                System.err.println("getPropertyClass is null for " + prop.getName());
                Object value = prop.getValue();
                if (value == null) {
                    cls = Object.class;
                } else {
                    cls = value.getClass();
                }
            }
            switch (col) {
            case LABEL_COL: return prop.getLabel();
            case VALUE_COL:
                return renderValue(cls, prop.getValue());
            case FULLNAME_COL: return prop.getName();
            case DEFAULT_COL: return renderValue(cls, prop.getDefaultValue());
            case ALLOWED_COL:
                return renderAllowed(cls, prop.getAllowedValues());
            case CLASS_COL:
                if (cls.isArray())
                    return "Array of " + cls.getComponentType().getName();
                else
                    return cls.getName();
            }
            return null;
        }
    }
}
