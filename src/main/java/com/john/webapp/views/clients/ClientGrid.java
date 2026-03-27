package com.john.webapp.views.clients;

import com.john.webapp.components.AppGridContextMenu;
import com.john.webapp.components.GridContextMenuAction;
import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.service.ClientServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientGrid extends Grid<ClientResponseDto> {

    private final ClientServiceClient clientService;
    private final CheckboxGroup<String> group;

    private static final List<String> COLUMN_KEYS = List.of(
            "name", "surname", "patronymic", "phoneNumber", "email"
    );

    public ClientGrid(ClientServiceClient clientService) {
        super(ClientResponseDto.class);
        this.clientService = clientService;

        setColumns(COLUMN_KEYS.toArray(new String[0]));
        getColumnByKey("name").setHeader("First name");
        getColumnByKey("surname").setHeader("Last name");
        getColumnByKey("patronymic").setHeader("Patronymic");
        getColumnByKey("phoneNumber").setHeader("Phone");
        getColumnByKey("email").setHeader("Email");
        getColumns().forEach(c -> c.setAutoWidth(true));

        group = new CheckboxGroup<>();
        group.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        group.setItems(COLUMN_KEYS);
        group.setItemLabelGenerator(item -> {
            String label = StringUtils.join(
                    StringUtils.splitByCharacterTypeCamelCase(item), " ");
            return StringUtils.capitalize(label.toLowerCase());
        });
        group.addValueChangeListener(e ->
                COLUMN_KEYS.forEach(key ->
                        getColumnByKey(key).setVisible(e.getValue().contains(key))
                )
        );

        // Завантажуємо тільки клієнтів (роль != 1)
        setItems(loadClients());
        addThemeVariants(GridVariant.LUMO_NO_BORDER);
    }

    /**
     * Завантажити тільки клієнтів — відфільтрувати адміністраторів (role = 1).
     */
    private List<ClientResponseDto> loadClients() {
        return clientService.getAllClients().stream()
                .filter(c -> c.getRole() == null || c.getRole() != 1)
                .collect(Collectors.toList());
    }

    public void attachAdminContextMenu(AuthenticationContext authContext) {
        if (!authContext.hasRole("ADMIN")) return;

        new AppGridContextMenu<>(this, List.of(
                new GridContextMenuAction<>(
                        "Редагувати",
                        VaadinIcon.EDIT,
                        this::select
                ),
                new GridContextMenuAction<>(
                        "Видалити клієнта",
                        VaadinIcon.TRASH,
                        true,
                        this::openDeleteConfirm
                )
        ));
    }

    private void openDeleteConfirm(ClientResponseDto client) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Видалити клієнта?");
        dialog.setText("Клієнт «" + client.getName() + " " + client.getSurname()
                + "» буде видалений безповоротно.");
        dialog.setCancelable(true);
        dialog.setCancelText("Скасувати");
        dialog.setConfirmText("Видалити");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                clientService.deleteClient(client.getId());
                refreshGrid();
                Notification n = Notification.show("Клієнта видалено.",
                        3000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification n = Notification.show("Помилка: " + ex.getMessage(),
                        5000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    public void refreshGrid() {
        select(null);
        setItems(loadClients());
    }

    public void createGridLayout(SplitLayout splitLayout) {
        Button button = new Button(VaadinIcon.GRID_H.create());
        button.addThemeVariants(ButtonVariant.LUMO_ICON);
        button.setAriaLabel("Show / hide columns");

        H3 title = new H3("Clients");

        HorizontalLayout headerLayout = new HorizontalLayout(title, button);
        headerLayout.setPadding(true);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setFlexGrow(1, title);

        Popover popover = new Popover();
        popover.setModal(true);
        popover.setBackdropVisible(true);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.setTarget(button);

        Div heading = new Div("Configure columns");
        heading.getStyle().set("font-weight", "600");
        heading.getStyle().set("padding", "var(--lumo-space-xs)");

        Set<String> defaultColumns = Set.of("name", "surname", "patronymic", "email");
        group.setValue(defaultColumns);

        Button showAll = new Button("Show all",
                e -> group.setValue(new HashSet<>(COLUMN_KEYS)));
        showAll.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button reset = new Button("Reset", e -> group.setValue(defaultColumns));
        reset.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout footer = new HorizontalLayout(showAll, reset);
        footer.setSpacing(false);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        popover.add(heading, group, footer);

        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(headerLayout, this, popover);
    }
}