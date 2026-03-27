package com.john.webapp.views.estimates;

import com.john.webapp.components.AppGridContextMenu;
import com.john.webapp.components.GridContextMenuAction;
import com.john.webapp.dto.EstimateResponseDto;
import com.john.webapp.service.EstimateServiceClient;
import com.john.webapp.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Кошториси")
@Route(value = "estimates/:orderId", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "CLIENT"})
public class EstimatesView extends Div implements BeforeEnterObserver {

    private final EstimateServiceClient estimateService;
    private final AuthenticationContext authContext;

    private Long orderId;
    private Grid<EstimateResponseDto> grid;
    private EstimateForm form;
    private SplitLayout splitLayout;

    public EstimatesView(EstimateServiceClient estimateService,
                         AuthenticationContext authContext) {
        this.estimateService = estimateService;
        this.authContext = authContext;
        setSizeFull();
        getStyle().set("position", "relative");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        orderId = event.getRouteParameters().get("orderId")
                .map(Long::parseLong).orElse(null);
        if (orderId == null) { event.forwardTo(""); return; }
        buildUI();
        refreshGrid();
    }

    private void buildUI() {
        removeAll();
        boolean isAdmin = authContext.hasRole("ADMIN");
        String backRoute = isAdmin ? "orders" : "my-orders";

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        Button backBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT),
                e -> getUI().ifPresent(ui -> ui.navigate(backRoute)));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        H3 title = new H3("Кошториси замовлення #" + orderId);
        header.setFlexGrow(1, title);
        header.add(backBtn, title);

        Span hint = new Span("Подвійний клік — переглянути / редагувати позиції кошторису");
        hint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("padding-bottom", "8px");

        grid = new Grid<>(EstimateResponseDto.class, false);
        grid.addColumn(EstimateResponseDto::getId).setHeader("№").setWidth("80px").setFlexGrow(0);
        grid.addColumn(EstimateResponseDto::getName).setHeader("Назва").setAutoWidth(true);
        grid.addColumn(EstimateResponseDto::getDate).setHeader("Дата").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(dto -> {
            boolean fin = Boolean.TRUE.equals(dto.getIsFinal());
            Span badge = new Span(fin ? "Фінальний" : "Чернетка");
            badge.getStyle()
                    .set("background", fin
                            ? "var(--lumo-success-color-10pct)"
                            : "var(--lumo-contrast-5pct)")
                    .set("color", fin
                            ? "var(--lumo-success-text-color)"
                            : "var(--lumo-secondary-text-color)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("font-size", "var(--lumo-font-size-s)");
            return badge;
        }).setHeader("Статус").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        // Подвійний клік → деталі кошторису (null-check: клік на порожній рядок)
        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() == null) return;
            getUI().ifPresent(ui ->
                    ui.navigate("estimate-details/" + e.getItem().getId()));
        });

        if (isAdmin) {
            form = new EstimateForm(estimateService, orderId);
            form.setOnClose(this::closeForm);

            splitLayout = new SplitLayout(buildWrapper(), form);
            splitLayout.setSplitterPosition(100);
            splitLayout.setSizeFull();

            grid.asSingleSelect().addValueChangeListener(e -> {
                if (e.getValue() != null) openEdit(e.getValue());
                else closeForm();
            });

            new AppGridContextMenu<>(grid, List.of(
                    new GridContextMenuAction<>("Деталі / позиції",
                            VaadinIcon.LIST, dto ->
                            getUI().ifPresent(ui ->
                                    ui.navigate("estimate-details/" + dto.getId()))),
                    new GridContextMenuAction<>("Редагувати назву",
                            VaadinIcon.EDIT, grid::select),
                    new GridContextMenuAction<>("Зробити фінальним",
                            VaadinIcon.CHECK_CIRCLE, false, this::finalizeEstimate),
                    new GridContextMenuAction<>("Видалити",
                            VaadinIcon.TRASH, true, this::deleteEstimate)
            ));

            Button addBtn = new Button(new Icon(VaadinIcon.PLUS));
            addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                    ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_LARGE);
            addBtn.setTooltipText("Новий кошторис");
            addBtn.getStyle()
                    .set("position", "absolute").set("bottom", "var(--lumo-space-xl)")
                    .set("right", "var(--lumo-space-m)").set("border-radius", "50%")
                    .set("width", "48px").set("height", "48px");
            addBtn.addClickListener(e -> openCreate());

            layout.add(header, hint, splitLayout);
            add(layout, addBtn);
        } else {
            layout.add(header, hint, buildWrapper());
            add(layout);
        }
    }

    private Div buildWrapper() {
        Div w = new Div(grid);
        w.setSizeFull();
        return w;
    }

    private void openCreate() {
        grid.asSingleSelect().clear();
        form.editEstimate(null);
        splitLayout.setSplitterPosition(65);
        form.setVisible(true);
    }

    private void openEdit(EstimateResponseDto dto) {
        if (Boolean.TRUE.equals(dto.getIsFinal())) {
            Notification.show("Фінальний кошторис не можна редагувати.", 3000,
                    Notification.Position.BOTTOM_END);
            return;
        }
        form.editEstimate(dto);
        splitLayout.setSplitterPosition(65);
        form.setVisible(true);
    }

    private void closeForm() {
        form.setVisible(false);
        splitLayout.setSplitterPosition(100);
        grid.asSingleSelect().clear();
        refreshGrid();
    }

    private void finalizeEstimate(EstimateResponseDto dto) {
        if (Boolean.TRUE.equals(dto.getIsFinal())) {
            Notification.show("Вже фінальний.", 2000, Notification.Position.BOTTOM_END);
            return;
        }
        ConfirmDialog d = new ConfirmDialog();
        d.setHeader("Зробити фінальним?");
        d.setText("Кошторис «" + dto.getName() + "» більше не зможе бути змінений.");
        d.setCancelable(true); d.setCancelText("Скасувати");
        d.setConfirmText("Підтвердити"); d.setConfirmButtonTheme("primary");
        d.addConfirmListener(e -> {
            try {
                estimateService.finalize(dto.getId());
                refreshGrid();
                Notification n = Notification.show("Фіналізовано.", 3000,
                        Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification n = Notification.show("Помилка: " + ex.getMessage(),
                        4000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        d.open();
    }

    private void deleteEstimate(EstimateResponseDto dto) {
        if (Boolean.TRUE.equals(dto.getIsFinal())) {
            Notification.show("Фінальний кошторис видалити неможливо.", 3000,
                    Notification.Position.BOTTOM_END);
            return;
        }
        ConfirmDialog d = new ConfirmDialog();
        d.setHeader("Видалити кошторис?");
        d.setText("«" + dto.getName() + "» буде видалено безповоротно.");
        d.setCancelable(true); d.setCancelText("Скасувати");
        d.setConfirmText("Видалити"); d.setConfirmButtonTheme("error primary");
        d.addConfirmListener(e -> {
            try {
                estimateService.delete(dto.getId());
                refreshGrid();
                Notification n = Notification.show("Видалено.", 3000,
                        Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification n = Notification.show("Помилка: " + ex.getMessage(),
                        4000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        d.open();
    }

    public void refreshGrid() {
        if (grid != null && orderId != null)
            grid.setItems(estimateService.getByOrderId(orderId));
    }
}