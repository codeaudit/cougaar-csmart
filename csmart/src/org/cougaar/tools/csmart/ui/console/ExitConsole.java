package org.cougaar.tools.csmart.ui.console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * org.cougaar.tools.csmart.ui.console
 *
 */
public class ExitConsole extends JPanel implements ActionListener {

  public static final String SOCIETY_DETACH = "Detach from Society but leave running (default)";
  public static final String KILL_NODES = "Stop all Attached Nodes (old behavior)";

  private String action = SOCIETY_DETACH;

  public ExitConsole() {
  }

  public String getResult() {
    return this.action;
  }

  public boolean reallyExit() {
    int x = 0;
    int y = 0;
    this.setLayout(new GridBagLayout());
    JLabel instr = new JLabel("Exit Console: Detach from Nodes (leaving them running), kill the Nodes, or Cancel?\n");
    JRadioButton detachButton = new JRadioButton(SOCIETY_DETACH);
    detachButton.setActionCommand(SOCIETY_DETACH);
    detachButton.setSelected(true);
    JRadioButton killButton = new JRadioButton(KILL_NODES);
    killButton.setActionCommand(KILL_NODES);
    killButton.setSelected(false);

    ButtonGroup exitButtonGroup = new ButtonGroup();
    exitButtonGroup.add(detachButton);
    exitButtonGroup.add(killButton);

    detachButton.addActionListener(this);
    killButton.addActionListener(this);

    add(instr,
        new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(10, 0, 5, 5),
                               0, 0));
    add(detachButton,
        new GridBagConstraints(x, y++, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(10, 0, 5, 5),
                               0, 0));
    add(killButton,
        new GridBagConstraints(x, y, 1, 1, 0.0, 0.0,
                               GridBagConstraints.WEST,
                               GridBagConstraints.NONE,
                               new Insets(0, 0, 5, 5),
                               0, 0));

    int result = JOptionPane.showConfirmDialog(null,
                                               this,
                                               "Exit Console",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.QUESTION_MESSAGE,
                                               null);
    if (result == JOptionPane.OK_OPTION) {
      return true;
    }
    return false;
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(SOCIETY_DETACH)) {
      this.action = SOCIETY_DETACH;
    } else if(e.getActionCommand().equals(KILL_NODES)) {
      this.action = KILL_NODES;
    }
  }

  public static void main(String[] args) {
    ExitConsole ec = new ExitConsole();
    ec.reallyExit();
  }
}
