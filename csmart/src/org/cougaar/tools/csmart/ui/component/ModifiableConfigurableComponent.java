/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.component;

import javax.swing.event.EventListenerList;

/**
 * Base class for ConfigurableComponents implementing the
 * ModifiableComponent interface
 **/
public abstract class ModifiableConfigurableComponent
  extends ConfigurableComponent
  implements ModifiableComponent
{
//    private static final long serialVersionUID = -7727291298618568087L;

    protected ModifiableConfigurableComponent(String name) {
        super(name);
    }

    public void addModificationListener(ModificationListener l) {
        getEventListenerList().add(ModificationListener.class, l);
    }

    public void removeModificationListener(ModificationListener l) {
        getEventListenerList().remove(ModificationListener.class, l);
    }

    protected void fireModification() {
        fireModification(new ModificationEvent(this, ModificationEvent.SOMETHING_CHANGED));
    }

    protected void fireModification(ModificationEvent event) {
        ModificationListener[] ls =
            (ModificationListener[]) getEventListenerList()
            .getListeners(ModificationListener.class);
        for (int i = 0; i < ls.length; i++) {
            try {
                ls[i].modified(event);
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
        }
    }
}
