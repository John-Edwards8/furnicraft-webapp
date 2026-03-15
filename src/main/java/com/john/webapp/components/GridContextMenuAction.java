package com.john.webapp.components;

import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.function.Consumer;

/**
 * Описує один пункт контекстного меню для будь-якого Grid<T>.
 *
 * Використання:
 * <pre>
 *   new GridContextMenuAction<>(
 *       "Видалити",
 *       VaadinIcon.TRASH,
 *       true,          // небезпечна дія (червоний колір)
 *       item -> doDelete(item)
 *   )
 * </pre>
 *
 * @param <T> тип рядка Grid
 */
public class GridContextMenuAction<T> {

    private final String label;
    private final VaadinIcon icon;
    private final boolean danger;
    private final Consumer<T> handler;

    public GridContextMenuAction(String label, VaadinIcon icon,
                                 boolean danger, Consumer<T> handler) {
        this.label  = label;
        this.icon   = icon;
        this.danger = danger;
        this.handler = handler;
    }

    /** Ярлик для звичайних (безпечних) дій */
    public GridContextMenuAction(String label, VaadinIcon icon, Consumer<T> handler) {
        this(label, icon, false, handler);
    }

    public String    getLabel()   { return label;   }
    public VaadinIcon getIcon()   { return icon;    }
    public boolean   isDanger()   { return danger;  }
    public Consumer<T> getHandler() { return handler; }
}