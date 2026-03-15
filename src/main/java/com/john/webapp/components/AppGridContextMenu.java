package com.john.webapp.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.List;

/**
 * Універсальне контекстне меню (ПКМ) для будь-якого Grid<T>.
 * Використовує GridContextMenu — єдиний спосіб отримати item рядка через getItem().
 */
public class AppGridContextMenu<T> {

    public AppGridContextMenu(Grid<T> grid, List<GridContextMenuAction<T>> actions) {
        GridContextMenu<T> menu = new GridContextMenu<>(grid);

        for (GridContextMenuAction<T> action : actions) {
            menu.addItem(
                buildItemContent(action.getIcon(), action.getLabel(), action.isDanger()),
                event -> event.getItem().ifPresent(action.getHandler()::accept)
            );
        }
    }

    private Div buildItemContent(
            com.vaadin.flow.component.icon.VaadinIcon icon,
            String label,
            boolean danger) {

        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-s)");

        var iconEl = icon.create();
        iconEl.getStyle().set("color", danger
                ? "var(--lumo-error-color)"
                : "var(--lumo-secondary-text-color)");

        Span text = new Span(label);
        if (danger) text.getStyle().set("color", "var(--lumo-error-text-color)");

        row.add(iconEl, text);
        return row;
    }
}