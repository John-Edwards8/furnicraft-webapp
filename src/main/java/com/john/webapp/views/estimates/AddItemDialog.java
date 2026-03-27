package com.john.webapp.views.estimates;

import com.john.webapp.dto.CatalogItemDto;
import com.john.webapp.service.EstimateServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.List;

public class AddItemDialog extends Dialog {

    private final EstimateServiceClient estimateService;
    private final Long estimateId;
    private final Runnable onSaved;

    private final Select<CatalogItemDto.Type> typeSelect   = new Select<>();
    private final ComboBox<CatalogItemDto>    itemCombo    = new ComboBox<>("Позиція");
    private final IntegerField                amountField  = new IntegerField("Кількість");
    private final Span                        priceHint    = new Span();

    public AddItemDialog(EstimateServiceClient estimateService,
                         Long estimateId, Runnable onSaved) {
        this.estimateService = estimateService;
        this.estimateId      = estimateId;
        this.onSaved         = onSaved;

        setHeaderTitle("Додати позицію до кошторису");
        setWidth("480px");

        // Тип
        typeSelect.setLabel("Тип позиції");
        typeSelect.setItems(CatalogItemDto.Type.values());
        typeSelect.setItemLabelGenerator(t -> switch (t) {
            case MATERIAL  -> "Матеріал";
            case ACCESSORY -> "Фурнітура";
            case PROCESS   -> "Процес / робота";
        });
        typeSelect.setWidthFull();

        // Вибір конкретної позиції з каталогу
        itemCombo.setItemLabelGenerator(item ->
                (item.getVendorCode() != null ? item.getVendorCode() + " — " : "")
                + item.getName()
                + " (" + item.getPrice() + " грн.)");
        itemCombo.setWidthFull();
        itemCombo.setEnabled(false);

        // Ціна
        priceHint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Кількість
        amountField.setMin(1);
        amountField.setValue(1);
        amountField.setStepButtonsVisible(true);
        amountField.setWidthFull();

        // Завантаження каталогу при зміні типу
        typeSelect.addValueChangeListener(e -> loadCatalog(e.getValue()));

        // Підказка з ціною при виборі позиції
        itemCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                priceHint.setText("Ціна за одиницю: " + e.getValue().getPrice() + " грн.");
            } else {
                priceHint.setText("");
            }
        });

        FormLayout form = new FormLayout(typeSelect, itemCombo, amountField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout content = new VerticalLayout(form, priceHint);
        content.setPadding(false);
        content.setSpacing(false);
        add(content);

        // Кнопки
        Button saveBtn   = new Button("Додати",    e -> save());
        Button cancelBtn = new Button("Скасувати", e -> close());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footer = new HorizontalLayout(saveBtn, cancelBtn);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    private void loadCatalog(CatalogItemDto.Type type) {
        if (type == null) { itemCombo.setEnabled(false); return; }

        List<CatalogItemDto> items = switch (type) {
            case MATERIAL  -> estimateService.getMaterials();
            case ACCESSORY -> estimateService.getAccessories();
            case PROCESS   -> estimateService.getProcesses();
        };

        itemCombo.setItems(items);
        itemCombo.setEnabled(true);
        itemCombo.clear();
        priceHint.setText("");
    }

    private void save() {
        if (typeSelect.isEmpty()) {
            Notification.show("Оберіть тип позиції", 2000,
                    Notification.Position.MIDDLE);
            return;
        }
        if (itemCombo.isEmpty()) {
            Notification.show("Оберіть позицію з каталогу", 2000,
                    Notification.Position.MIDDLE);
            return;
        }
        if (amountField.getValue() == null || amountField.getValue() < 1) {
            Notification.show("Вкажіть кількість (мін. 1)", 2000,
                    Notification.Position.MIDDLE);
            return;
        }

        try {
            estimateService.addItem(
                    estimateId,
                    itemCombo.getValue().getId(),
                    typeSelect.getValue(),
                    amountField.getValue()
            );
            Notification n = Notification.show("Позицію додано.", 3000,
                    Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
            if (onSaved != null) onSaved.run();

        } catch (Exception ex) {
            Notification n = Notification.show(
                    "Помилка: " + ex.getMessage(), 5000,
                    Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}